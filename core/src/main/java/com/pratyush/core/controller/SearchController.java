package com.pratyush.core.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pratyush.core.model.Document;
import com.pratyush.core.model.DocumentEmbedding;
import com.pratyush.core.model.DocumentEmbeddingProjection;
import com.pratyush.core.model.SearchModel;
import com.pratyush.core.model.exchanges.embedding_service.EmbeddingRequest;
import com.pratyush.core.model.exchanges.embedding_service.EmbeddingResponse;
import com.pratyush.core.service.DocumentService;
import com.pratyush.core.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/search")
public class SearchController {

    private RestTemplate restTemplate;
    private String embeddingUri = "http://localhost:8000/embedding";

    @Autowired
    private SearchService searchService;

    @Autowired
    private DocumentService documentService;

    public SearchController() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("null")
    @GetMapping("")
    public List<DocumentEmbeddingProjection> getQueryResults(@RequestParam(required = true) String query,
                                          @RequestParam(required = true) Integer numResults,
                                          @RequestParam(required = false) String method) {

        if(method == null || method == "vector_ann") {
            EmbeddingRequest request = new EmbeddingRequest(query);
            EmbeddingResponse response = this.restTemplate.postForObject(this.embeddingUri,request,EmbeddingResponse.class);
            return searchService.getTopMatching(numResults,response.data);
        } else if(method == "tfidf" || method == "bm25") {
            List<Document> documents = documentService.getAllPapers();
            List<Long> documentIds = documents.stream()
                                            .map(doc -> doc.getId())
                                            .collect(Collectors.toList());

            SearchModel.Request.Builder searchRequest = SearchModel.Request.newBuilder();

            searchRequest.setSearchMethod(method);
            searchRequest.setSearchQuery(query);
            searchRequest.addAllDocumentIds(documentIds);
            searchRequest.build();

            // return searchService.getTopMatching(numResults,response.data);
        }
        return new ArrayList<>();
    }

    @GetMapping("/")
    public List<DocumentEmbedding> getAll() {
        return documentService.findAll();
    }
}
