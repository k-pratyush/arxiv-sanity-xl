from pypdf import PdfReader
from sentence_transformers import SentenceTransformer
from .models import Document, DocumentEmbedding
import datetime
from .database import SessionLocal
from sqlalchemy.exc import IntegrityError

model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')

def get_embedding(query):
    return model.encode(query).tolist()

def chunk_data(data, chunk_size=512):
    # average english word length
    character_window_length = chunk_size * 4.7
    chunked_data = []

    for chunk_id, idx in enumerate(range(0,len(data), int(character_window_length))):
        chunked_data.append((chunk_id + 1,data[idx: idx+int(character_window_length)]))

    return chunked_data

def extract_text(document_path):
    reader = PdfReader(document_path)
    text = ""
    for page_num, page_text in enumerate(reader.pages):
        text = text + f"<|{page_num + 1}|> {page_text.extract_text()}"
    return text

def bulk_data_load():
    pass

def test_data_load(document_path):
    try:
        data = {
        "name": "Attention Is All You Need",
        "url": "https://arxiv.org/abs/1706.03762",
        "created_date": datetime.date.today(),
        "document": extract_text(document_path)[:65535]
        }

        db = SessionLocal()
        doc = Document(**data)
        db.add(doc)
        db.flush()
        db.refresh(doc)

        chunked_data = chunk_data(data["document"])
        for (chunk_id, text) in chunked_data:
            DocEmbed = DocumentEmbedding(embedding=get_embedding(text),
                        document_id = doc.id, chunk=chunk_id, created_date = datetime.date.today())
            db.add(DocEmbed)
        db.commit()
        
        return {
            "success": True,
            "data": chunked_data,
            "message": "Data loaded"
        }
    except IntegrityError as e:
        return {
            "success": False,
            "message": "Unique constraint error"
        }

if __name__ == "__main__":
    test_data_load()
