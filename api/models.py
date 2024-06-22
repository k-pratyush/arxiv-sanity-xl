from sqlalchemy import Integer, String, Column, Vector, ForeignKey, LargeBinary, Date
from .database import Base

class Document(Base):
    __tablename__ = "documents"

    id = Column(Integer, primary_key=True, unique=True, autoincrement=True)
    name = Column(String, unique=True, index=True)
    url = Column(String, unique=True)
    blob = Column(LargeBinary)
    created_date = Column(Date)

class DocumentEmbedding(Base):
    __tablename__ = "documentembeddings"

    id = Column(Integer, primary_key=True, unique=True,autoincrement=True)
    embedding = Column(Vector, not_null=True)
    document_id = Column(Integer, ForeignKey("documents.id"))
    created_date = Column(Date)
