import celery
import config
from celery.result import AsyncResult
from tasks.embedding.tasks import generate_embedding

celery_app = celery.Celery()
celery_app.config_from_object(config.CeleryGeneralConfig)
celery_app.autodiscover_tasks()

class EmbeddingApi:
    def send_request(self, query: str) -> AsyncResult:
        result = generate_embedding.delay(query)
        return result

    def process_request(self, query: str):
        celery_result = self.send_request(query)
        results = {}
        try:
            results = celery_result.get(timeout=60)
            celery_result.forget()
            results = results or {}
        except celery.exceptions.TimeoutError:
            results = {}
        return results
