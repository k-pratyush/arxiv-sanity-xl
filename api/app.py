import logging
import datetime

from .schemas import Query, UserDetails
from .models import Users
from sqlalchemy import text, select, update
from fastapi import FastAPI
from .database import SessionLocal, engine
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

@app.get("/")
async def home():
    return {"success": True}

@app.get("/load")
async def load():
    result = test_data_load("papers/1706.03762v7.pdf")
    return result

@app.get("/bulk_load")
async def bulk_load():
    result = bulk_data_load("papers", 10)
    return {
            "success": True,
            "data": result
        }

@app.get("/search")
async def search(query: str, limit: int = 5):
    embedding = get_embedding(query)
    with engine.connect() as conn:
        result = conn.execute(text(f"""select distinct a.name, a.url from (select * from document doc
                                   inner join document_embedding emb on doc.id = emb.document_id
                                   order by emb.embedding <-> '{embedding}' limit {limit}) a"""))

    # TODO: Handle if no data is found in DB
    names, urls = list(zip(*[(row[0], row[1]) for row in result]))
    return {
        "success": True,
        "data": {
            "names": names,
            "urls": urls
        }
    }

# Move to Springboot app
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

        if user:
            with engine.connect() as conn:
                recommendations = conn.execute(text(f"""select distinct a.name, a.url from (select * from document doc
                                                    inner join document_embedding emb on doc.id = emb.document_id
                                                    order by emb.embedding <-> (select users.preference_vector from users
                                                    where user_id = '{user_id}') limit {limit}) a
                                                    """))
                # TODO: Handle if no data is found in DB
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
        embedding = get_embedding(query)
        return {
            "success": True,
            "data": embedding
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
