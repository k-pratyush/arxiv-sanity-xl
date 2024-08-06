import os
import json
import logging
import datetime

from fastapi import FastAPI

from services.embedding_api import EmbeddingApi
from .models import Users
from .database import SessionLocal
from .schemas import Query, UserDetails
from sqlalchemy import text, select, update
from .utils import get_embedding, test_data_load, bulk_data_load


logger = logging.getLogger('uvicorn.debug')
logger.setLevel(logging.DEBUG)

app = FastAPI()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def load_queries():
    with open("query.json", "r") as f:
        queries = json.loads(f.read())
    return queries

@app.get("/")
async def home():
    return {"success": True}

@app.get("/load")
async def load():
    result = test_data_load("papers/1706.03762v7.pdf")
    return result

@app.get("/bulk_load/{num_papers}")
async def bulk_load(num_papers: int = 10):
    try:
        fullpath = os.path.realpath(__file__)
        path, _ = os.path.split(fullpath)
        result = bulk_data_load(f"{path}/papers", num_papers)
        return {
                "success": True,
                "data": result
            }
    except Exception as e:
        print(e)
        return {
            "success": False,
            "data": {
                "message": e
            }
        }

@app.get("/search")
async def search(query: str, limit: int = 5):
    embedding = get_embedding(query)
    db = next(get_db())
    queries = load_queries()
    result = db.execute(text(queries["search"]
                             .format(embedding=embedding, limit=limit))).fetchall()
    # TODO: Handle if no data is found in DB
    names, urls = list(zip(*[(row[0], row[1]) for row in result]))
    print(*result)
    return {
        "success": True,
        "data": {
            "names": names,
            "urls": urls
        }
    }

# TODO: Move to Springboot app
@app.post("/users")
async def add_user(user_details: UserDetails):
    try:
        db = next(get_db())
        user = db.execute(select(Users).filter_by(user_id=user_details.user_id)).one_or_none()
        if user:
            db.close()
            return {
                "success": False,
                "data": {
                    "error": "User already exists"
                }
            }
        else:
            user = Users(user_id=user_details.user_id,
                         preferences=user_details.preferences,
                         preference_vector=get_embedding(user_details.preferences),
                         created_date=datetime.date.today())
            db.add(user)
            db.flush()
            db.refresh(user)
            db.commit()
            db.close()
        return {
            "success": True,
            "data": {
                "id": user.id
            }
        }

    except Exception as e:
        print(e)
        db.close()
        return {
            "success": False,
            "data": {
                "error": e
            }
        }

@app.get("/users/{user_id}")
async def get_user(user_id: str):
    try:
        db = next(get_db())
        user = db.execute(select(Users).filter_by(user_id=user_id)).scalars().first()
        db.close()

        if user:
            return {
                "success": True,
                "data": user.json()
            }
        else:
            return {
                "success": False,
                "data": {
                    "message": "User not found."
                }
            }
    except Exception as e:
        print(e)
        return {
            "success": False,
            "data": {
                "error": e
            }
        }

@app.post("/users/{user_id}/preferences")
async def save_user_preferences(user_id: str, preferences: str):
    try:
        db = next(get_db())
        user = db.execute(select(Users).filter_by(user_id=user_id)).scalars().first()

        if user:
            user.preferences = preferences
            user.preference_vector = get_embedding(preferences)
            db.execute(update(Users), [user.json()])
            db.commit()
            return {
                "success": True,
                "data": {
                    "message": "Preferences updated."
                }
            }
        else:
            return {
                "success": False,
                "data": {
                    "message": "User not found."
                }
            }
    except Exception as e:
        print(e)
        return {
            "success": False,
            "data": {
                "error": e
            }
        }

# TODO: Order by created date to recommend newer papers
@app.get("/users/{user_id}/recommendations")
async def get_recommendations(user_id: str, limit: int = 4):
    try:
        db = next(get_db())
        user = db.execute(select(Users).filter_by(user_id=user_id)).scalars().first()
        queries = load_queries()

        if user:
            # TODO: Handle if no data is found in DB
            recommendations = db.execute(text(queries["recommendations"].format(user_id=user_id, limit=limit))).fetchall()
            names, urls = list(zip(*[(row[0], row[1]) for row in recommendations]))
            return {
                "success": True,
                "data": {
                    "names": names,
                    "urls": urls
                }
            }
        else:
            return {
                "success": False,
                "data": {
                    "message": "User not found."
                }
            }

    except Exception as e:
        return {
            "success": False,
            "message": f"ERROR: {e}"
        }

@app.post("/embedding")
async def create_embedding(query: Query):
    try:
        result = EmbeddingApi().process_request(query.query)
        return {
            "success": True,
            "data": result
        }
    except Exception as e:
        print(e)
        logging.debug(e)
        return {
            "success": False,
            "data": {
                "error": e
            }
        }
