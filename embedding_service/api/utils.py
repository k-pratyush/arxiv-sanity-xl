import os
import json
import datetime

import arxiv
import sqlalchemy

from pypdf import PdfReader
from .database import SessionLocal
from sqlalchemy.exc import IntegrityError
from .models import Document, DocumentEmbedding
from .exceptions import InvalidPdfException
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
    try:
        reader = PdfReader(document_path)
        text = ""
        for page_num, page_text in enumerate(reader.pages):
            text = text + f"<|{page_num + 1}|> {page_text.extract_text().encode('UTF-8')}"
        return text
    except Exception as e:
        # Store paper description
        raise InvalidPdfException("Text Encoding Error", e)

def download_papers(documents_path, offset, paper_count, latest_papers=False):
    query = "cat:cs.CV OR cat:cs.AI OR cat:cs.NE OR cat:cs.PF"
    client = arxiv.Client()

    # If downloading latest papers, set DB offset to 0,
    # this downloads latests <paper_count> papers by submission date.
    if latest_papers:
        offset = 0

    search = arxiv.Search(
        query=query,
        max_results=offset + paper_count + 1,
        sort_by = arxiv.SortCriterion.SubmittedDate,
        sort_order=arxiv.SortOrder.Ascending
    )

    results = client.results(search, offset + 1)
    meta = {}

    for paper in results:
        paper_path = f"{documents_path}/{paper.title}.pdf"
        paper.download_pdf(dirpath=f"{documents_path}/", filename=f"{paper.title}.pdf")

        try:
            file_text = extract_text(paper_path)[:65535]
        except InvalidPdfException as e:
            file_text = paper.summary[:65535]

        meta[f"{paper.title}.pdf"] = {
            "file_name": paper.entry_id,
            "title": paper.title,
            "url": paper.pdf_url,
            "authors": paper.authors,
            "summary": paper.summary,
            "document": file_text,
            "paper_path": paper_path
        }

    return meta

# TODO: Fix bug - Duplicate loads when re-running bulk load API
def bulk_data_load(documents_path, paper_count, latest_papers=False):
    db = SessionLocal()

    try:
        with open("query.json", "r") as f:
            queries = json.loads(f.read())
    except FileNotFoundError:
        queries = None

    if queries:
        offset = db.execute(sqlalchemy.text(queries["last_load_id"])).scalar_one()
    else:
        offset = 0

    paper_meta = download_papers(documents_path, offset, paper_count, latest_papers)
    files = paper_meta.keys()

    try:
        for file in files:
            document_data = {
                "name": paper_meta[file]["title"],
                "url": paper_meta[file]["url"],
                "created_date": datetime.date.today(),
                "document": paper_meta[file]["document"]
            }

            doc = Document(**document_data)
            db.add(doc)
            db.flush()
            db.refresh(doc)

            chunked_document = chunk_data(document_data["document"])
            for (chunk_id, text) in chunked_document:
                DocEmbed = DocumentEmbedding(embedding=get_embedding(text),
                                            document_id = doc.id, chunk=chunk_id,
                                            created_date = datetime.date.today())
                db.add(DocEmbed)

        db.commit()
        clear_directory(documents_path)

        return {
            "message": "Data loaded"
        }

    except IntegrityError as e:
        clear_directory(documents_path)
        return {
            "message": "Unique constraint error"
        }
    except Exception as e:
        clear_directory(documents_path)
        raise e
    finally:
        db.close()

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

def clear_directory(directory):
    print(directory)
    for file in os.listdir(directory):
        print(file)
        if ".pdf" in file and os.path.exists(f"{directory}/{file}"):
            os.remove(f"{directory}/{file}")

if __name__ == "__main__":
    test_data_load()
