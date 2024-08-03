from sqlalchemy import Integer, String, Column, ForeignKey, Date
from pgvector.sqlalchemy import Vector
from .database import Base

class Document(Base):
    __tablename__ = "document"

    id = Column(Integer, primary_key=True, unique=True, autoincrement=True)
    name = Column(String, unique=True, index=True)
    url = Column(String, unique=True)
    created_date = Column(Date)
    document = Column(String)

class DocumentEmbedding(Base):
    __tablename__ = "document_embedding"

    id = Column(Integer, primary_key=True, unique=True, autoincrement=True)
    embedding = Column(Vector(384))
    document_id = Column(Integer, ForeignKey("document.id"))
    chunk = Column(Integer)
    created_date = Column(Date)

class Users(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, unique=True, autoincrement=True)
    user_id = Column(String, unique=True)
    preferences = Column(String)
    preference_vector = Column(Vector(384))
    created_date = Column(Date)

    def json(self):
        return {
            "id": self.id,
            "user_id": self.user_id,
            "preferences": self.preferences,
            "created_date": self.created_date
        }
