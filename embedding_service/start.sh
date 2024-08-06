# fastapi server
fastapi run api/app.py

# celery worker (need to pass '--pool solo' for MacOS, see: https://groups.google.com/g/celery-users/c/KQyy1rdnWi4)
celery -A tasks.embedding.tasks worker -E --loglevel=DEBUG --pool solo
