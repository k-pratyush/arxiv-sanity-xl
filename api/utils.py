import os
import datetime

import arxiv

from pypdf import PdfReader
from .database import SessionLocal
from sqlalchemy.exc import IntegrityError
from .models import Document, DocumentEmbedding
from sentence_transformers import SentenceTransformer

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

def download_papers(documents_path, paper_count):
    query = "cat:cs.CV OR cat:cs.AI OR cat:cs.NE OR cat:cs.PF"
    client = arxiv.Client()
    search = arxiv.Search(
        query=query,
        max_results=paper_count
    )
    results = client.results(search)
    meta = {}
    for paper in results:
        meta[f"{paper.title}.pdf"] = {
            "file_name": paper.entry_id,
            "title": paper.title,
            "url": paper.pdf_url,
            "authors": paper.authors,
            "summary": paper.summary
        }
        paper.download_pdf(dirpath=f"./{documents_path}/", filename=f"{paper.title}.pdf")
    return meta

def bulk_data_load(documents_path, paper_count):
    db = SessionLocal()

    paper_meta = download_papers(documents_path, paper_count)
    files = [file for file in os.listdir(documents_path) if ".pdf" in file]
    try:
        for file in files:
            file_text = extract_text(f"{documents_path}/{file}")
            document_data = {
                "name": paper_meta[file]["title"],
                "url": paper_meta[file]["url"],
                "created_date": datetime.date.today(),
                "document": file_text[:65535]
            }
            doc = Document(**document_data)
            db.add(doc)
            db.flush()
            db.refresh(doc)
            chunked_document = chunk_data(document_data["document"])

            for (chunk_id, text) in chunked_document:
                DocEmbed = DocumentEmbedding(embedding=get_embedding(text),
                            document_id = doc.id, chunk=chunk_id, created_date = datetime.date.today())
                db.add(DocEmbed)
            file_path = f"{documents_path}/{file}"
            if os.path.exists(file_path):
                os.remove(file_path)

        db.commit()
        return {
            "message": "Data loaded"
        }
    except IntegrityError as e:
        return {
            "message": "Unique constraint error"
        }

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
            "message": "Data loaded"
        }
    except IntegrityError as e:
        return {
            "success": False,
            "message": "Unique constraint error"
        }

if __name__ == "__main__":
    test_data_load()
