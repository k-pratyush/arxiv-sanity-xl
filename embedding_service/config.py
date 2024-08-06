import os
from dotenv import load_dotenv
load_dotenv()

class RedisConfig:
    HOST = os.environ.get("REDIS_HOST", None)
    PORT = os.environ.get("REDIS_PORT", None)

class CeleryGeneralConfig:
    broker_connection_retry_on_startup = False
    broker_url = os.environ.get("CELERY_BROKER_URL", None)
    result_backend = os.environ.get("CELERY_RESULT_BACKEND", None)

    # Embedding task
    task_queue = "tasks.embedding"
    task_embedding_prefix = "tasks.embedding.tasks"
    task_generate_embedding = f"{task_embedding_prefix}.generate_embedding"
