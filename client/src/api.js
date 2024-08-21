import axios from 'axios';

export default function getDocuments(num) {
    if(num !== undefined) {
        return axios.get(`http://localhost:8081/sanity?numPapers=${num}`);
    } else {
        return axios.get("http://localhost:8081/sanity");
    }
}

export function searchDocuments(num, searchQuery, searchMethod) {
    return axios.get(`http://localhost:8081/search?query=${searchQuery.replace(" ", "%20")}&numResults=${num}&method=${searchMethod}`);
}
