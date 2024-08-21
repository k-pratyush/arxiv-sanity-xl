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

- proto compile: /Users/pratyushkerhalkar/Downloads/protoc-27-2/bin/protoc -I src/main/java/ --java_out=src/main/java/com/pratyush/docsearch/model/ src/main/java/com/pratyush/docsearch/model/search_cluster.proto
- proto compile: /Users/pratyushkerhalkar/Downloads/protoc-27-2/bin/protoc -I src/main/java/ --java_out=src/main/java/ src/main/java/com/pratyush/core/model/search_cluster.proto

#### TODO
- [x] ~~Routing Server - Process & Recommendations~~
- [x] ~~Model worker queue~~
- [x] ~~UI~~
- [ ] Improve UI (xD)
- [ ] Scraper Daemon
- [ ] Dockerize project
- [ ] API, Setup Docs
- [ ] Setup Github Actions
- [ ] Deploy (Oracle cloud?)

#### Future Improvements
- [ ] Implement pagination
- [ ] Paper recommendations
- [ ] Dynamically fetch search coordinator from registry
- [ ] Query sanitization

#### Credits
- Michael Pogrebinsky - Course on distributed systems
- Andrej Karpathy - Arxiv Sanity

-----------
