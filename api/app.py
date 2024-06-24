from fastapi import FastAPI
from .database import SessionLocal, engine
from .utils import get_embedding, test_data_load
from sqlalchemy import text
import logging
from .schemas import Query

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

@app.get("/match")
async def match(query: str, limit: int = 5):
    embedding = get_embedding(query)
    with engine.connect() as conn:
        result = conn.execute(text(f"""select distinct a.name, a.url from (select * from document doc
                                   inner join document_embedding emb on doc.id = emb.document_id
                                   order by emb.embedding <-> '{embedding}' limit {limit}) a"""))
    names, urls = list(zip(*[(row[0], row[1]) for row in result]))
    return {
        "success": True,
        "data": {
            "names": names,
            "urls": urls
        }
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
