from fastapi import FastAPI, Depends
from .database import SessionLocal
from .utils import get_embedding
# import schemas

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

@app.post("/document")
async def create_embedding():
    # get_embedding
    pass

@app.get("/document")
async def get_document():
    pass
