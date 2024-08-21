import React, { useState, useEffect } from 'react';
import SearchBar from "../components/Searchbar";
import SearchResult from "../components/SearchResult";
import getDocuments from "../api";


// // TODO: Fix API definition ;(
function fixDocsKeys(docs) {
    console.log(docs)
    if(docs && docs[0] && "documentName" in docs[0]) {
        return docs.map(doc => {
            doc["name"] = doc["documentName"]
            doc["url"] = doc["documentUrl"]
            delete doc["documentName"]
            delete doc["documentUrl"]
            return doc
        });
    }
}

function HomeScreen() {
    const [documents, setDocuments] = useState([]);

    const setQueryResults = (data) => {
        setDocuments(data?fixDocsKeys(data):data)
    }

    useEffect(() => {
        getDocuments(10)
        .then(response => {setDocuments(response.data)})
        .catch(err => console.log(err))
    }, []);

    return (
        <div>
            <SearchBar setQueryResults={setQueryResults} />
            {documents.map((doc, idx) => (
                <SearchResult key={idx} score={2.1} name={doc.name} url={doc.url}  />
                )
                )}
        </div>
    );
}

export default HomeScreen;
