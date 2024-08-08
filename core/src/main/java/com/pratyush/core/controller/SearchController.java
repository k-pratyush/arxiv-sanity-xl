package com.pratyush.core.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pratyush.core.model.DocumentEmbedding;
import com.pratyush.core.model.DocumentEmbeddingProjection;
import com.pratyush.core.model.exchanges.embedding_service.EmbeddingRequest;
import com.pratyush.core.model.exchanges.embedding_service.EmbeddingResponse;
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

    public SearchController() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("null")
    @GetMapping("")
    public List<DocumentEmbeddingProjection> getQueryResults(@RequestParam(required = true) String query,
                                          @RequestParam(required = true) Integer numResults,
                                          @RequestParam(required = false) String method) {

        EmbeddingRequest request = new EmbeddingRequest(query);
        EmbeddingResponse response = this.restTemplate.postForObject(this.embeddingUri,request,EmbeddingResponse.class);

        return searchService.getTopMatching(numResults,response.data);
    }

    @GetMapping("/")
    public List<DocumentEmbedding> getAll() {
        return searchService.findAll();
    }
    
}
