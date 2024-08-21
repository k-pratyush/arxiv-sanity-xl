Rough Design

Search Service:
  Search with Query:
   options
   - Vector: Query DB
   - tfidf, bm25:
     Steps:
       - Send search query to coordinator
       - Coordinator identifies document count in DB, and number of active workers
       - Creates Task for each worker with document ids to compute for
       - Collects and accumulates results from workers
       - Sends back to the search service

    searchcontroller -> coordinator:
        search query
        get all paper ids
        count of papers

    coordinator -> worker:
        split all ids into batches(tasks),
        send tasks to worker

    worker queries the DB for the given IDS, computes TFIDF

    worker -> coordinator:
        document name
        document url
        document id
        score for each id

    coordinator -> searchcontroller:
        IDs and their respective scores,
        document names
        document urls
        document ids


 * Pagination
 
Document Service:
 * Homepage:
 * 	- Get latest N papers
 * 	- Most liked/saved papers
 * 	- Recommended reads
 * 	- Custom section: top reading lists
 * 	- Share papers
 * When logged in:
 * 	- save papers

User Service:
 * Register as User: login, signup
 * While logged in:
 * - Preferences: add/update preferences
 * - User dashboard:
 * 		- get user recommendations
 * 		- Save papers to reading list
 * 		- Liked papers

----------------------------------------

