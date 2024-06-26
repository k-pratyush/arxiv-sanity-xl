import logging
from .schemas import Query
from sqlalchemy import text
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

@app.get("/recommendations/{user_id}")
async def get_recommendations(user_id: str):
    try:
        with engine.connect() as conn:
            recommendations = conn.execute(text(f"""select distinct a.name, a.url from (select * from document doc
                                                inner join document_embedding emb on doc.id = emb.document_id
                                                order by emb.embedding <-> (select user.preferences from user
                                                where user_id = {user_id}) u limit {20}) a
                                                """))

    except Exception as e:
        return {
            "success": False,
            "message": f"ERROR: {e}"
        }

@app.post("/embedding")
async def create_embedding(query: Query):
    try:
        embedding = get_embedding(query.query)
        return {
            "success": True,
            "data": embedding
        }
    except Exception as e:
        print(e)
        logging.debug(e)
        return {
            "success": False,
            "data": e
        }
