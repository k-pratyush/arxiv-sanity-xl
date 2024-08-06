import config
import celery

from sentence_transformers import SentenceTransformer

app = celery.Celery()
app.config_from_object(config.CeleryGeneralConfig)
app.autodiscover_tasks(["tasks.embedding"])

model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')

@app.task(name="generate_embedding")
def generate_embedding(query: str):
    return model.encode(query).tolist()
