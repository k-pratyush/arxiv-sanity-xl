# Arxiv Sanity XL

Arxiv Sanity with bells and whistles.

A distributed search engine that helps researchers find and get recommendations on latest arxiv research papers. Leverates TF-IDF, BM25, and Vector Approximate Nearest Neighbour matching algorithms to identify closest semantic match to user search query.

#### Components
- Backend:
  - Arxiv Sanity XL backend consists of 3 main modules:
      - core: Springboot+protobuf server, handles request routing across services.
      - embedding_service: FastAPI+Celery service, handling requests for sentence embedding generation.
      - backend: Zookeeper service, distributes TFIDF and BM25 computation tasks across workers.
- Client: React frontend web application.
- Databases:
    - Postgres, pgVector: Stores research papers, metadata, user preferences, and embeddings.
    - Redis: Acts as celery broker and backend for handling embedding api requests.

#### Architecture
![alt text](https://github.com/k-pratyush/arxiv-sanity-xl/blob/main/docs/architecture.png?raw=true)

#### Leader Election Protocol
- Coordinator/ Leader is decided based on:
    - start time - The first node to register to Zookeeper becomes the coordinator.
    - All backend nodes are Zookeeper watchers - Every node that gets registered to zookeeper watches and health-checks one previous node for downtime (like a daisy chain). If the node is down, the watcher notifies zookeeper for leader check and re-election if required.

#### Build Project [WIP]
```
- Requirements:
    - Redis (currently running on localhost)
    - Postgres
        - Start an instance
        - Run the file 'embedding_service/api/ddl/tables.sql' on postgres admin to setup tables
    - Zookeeper (v3.4.12)

- embedding_server:
    - create conda env with python 3.11: 'conda create -n sanity python=3.11'
    - activate env: 'conda activate sanity'
    - install dependencies: 'pip install -r requirements.txt'
    - Add environment variables to .env file under 'embedding_service' directory:
        - PG_USER=POSTGRES USER
        - PG_PASSWORD=POSTGRES PASSWORD
        - REDIS_HOST=localhost
        - REDIS_PORT=6379
        - CELERY_BROKER_URL=redis://localhost:6379/0
        - CELERY_RESULT_BACKEND=redis://localhost:6379/1

    - run backend: 'fastapi run api/app.py'
    - run celery worker: 'celery -A tasks.embedding.tasks worker -E --loglevel=DEBUG --pool solo'
    (need to pass '--pool solo' for MacOS, see: https://groups.google.com/g/celery-users/c/KQyy1rdnWi4)

- core:
    - add environment variables in application.properties file: 
        - spring.application.name=core
        - spring.datasource.url=POSTGRES URL
        - spring.datasource.username=POSTGRES USERNAME
        - spring.datasource.password=POSTGRES PASSWORD
        - server.port=8081
    - build the project using: 'mvn clean package'
    - run the springboot app

- backend:
    - add environment variables in application.properties file:
        - pg_url=POSTGRES URL
        - pg_user=POSTGRES USER
        - pg_password=POSTGRES PASSWORD
    - build the project using: 'mvn clean package'
    - run the springboot app
```

<!-- - proto compile: /Users/pratyushkerhalkar/Downloads/protoc-27-2/bin/protoc -I src/main/java/ --java_out=src/main/java/com/pratyush/docsearch/model/ src/main/java/com/pratyush/docsearch/model/search_cluster.proto
- proto compile: /Users/pratyushkerhalkar/Downloads/protoc-27-2/bin/protoc -I src/main/java/ --java_out=src/main/java/ src/main/java/com/pratyush/core/model/search_cluster.proto -->

#### TODO
- [x] ~~Routing Server - Process & Recommendations~~
- [x] ~~Model worker queue~~
- [x] ~~UI~~
- [ ] Improve UI (xD)
- [ ] Scraper Daemon
- [ ] Support for paper description, authors
- [ ] ~~API, Setup Docs~~
- [ ] Setup Github Actions
- [ ] Deploy (Oracle cloud?)

#### Future Improvements
- [ ] Dockerize project
- [ ] Implement pagination
- [ ] Paper recommendations
- [ ] Dynamically fetch search coordinator from registry
- [ ] Query sanitization

#### Credits
- Michael Pogrebinsky - Course on distributed systems
- Andrej Karpathy - Arxiv Sanity

-----------
