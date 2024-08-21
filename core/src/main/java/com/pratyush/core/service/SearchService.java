package com.pratyush.core.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pratyush.core.model.Document;
import com.pratyush.core.model.DocumentEmbedding;
import com.pratyush.core.model.DocumentEmbeddingProjection;
import com.pratyush.core.model.DocumentStats;
import com.pratyush.core.model.SearchModel;
import com.pratyush.core.repository.DocumentEmbeddingRepository;
import com.pratyush.core.repository.DocumentRepository;

@Service
public class SearchService {
    @Autowired
    DocumentEmbeddingRepository documentEmbeddingRepository;

    @Autowired
    DocumentRepository documentRepository;

    private final String clusterUri = "http://localhost:8080/search";
    private HttpClient client;

    public SearchService() {
        this.client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .build();
    }

    public List<DocumentEmbeddingProjection> getTopMatching(Integer topN, double[] embedding) {
        List<DocumentEmbedding>matches = documentEmbeddingRepository.findTopMatching(embedding);
        Set<Long> visited = new HashSet<Long>();
        List<DocumentEmbeddingProjection> results = new ArrayList<>();

        for(DocumentEmbedding match: matches) {
            Document docObj = match.getDocument();
            if(visited.contains(docObj.getId()) == false) {
                visited.add(docObj.getId());
                results.add(new DocumentEmbeddingProjection(docObj.getUrl(), docObj.getName()));
            }
        }
        return results.subList(0, topN);
    }

    private SearchModel.Request getSearchRequest(String query, String method, List<Long> documentIds) {
        SearchModel.Request searchRequest = SearchModel.Request.newBuilder()
                                                .setSearchQuery(query)
                                                .addAllDocumentIds(documentIds)
                                                .setSearchMethod(method)
                                                .build();
        return searchRequest;
    }

    private SearchModel.Response getSearchResponse(SearchModel.Request searchRequest) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(searchRequest.toByteArray()))
                    .uri(URI.create(this.clusterUri))
                    .header("Content-Type", "application/octet-stream")
                    .build();

            CompletableFuture<byte[]> responseBody = this.client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenApply(HttpResponse::body);

            return SearchModel.Response.parseFrom(responseBody.join());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return SearchModel.Response.getDefaultInstance();
        }
    }

    private static double getMaxScore(SearchModel.Response searchClusterResponse) {
        if (searchClusterResponse.getRelevantDocumentsCount() == 0) {
            return 0;
        }
        return searchClusterResponse.getRelevantDocumentsList()
                .stream()
                .map(document -> document.getScore())
                .max(Double::compareTo)
                .get();
    }

    private static double normalizeScore(double inputScore, double maxScore) {
        return (double) Math.ceil(inputScore * 100.0 / maxScore);
    }

    public List<DocumentStats> getAllDocumentStats(String query, String method, List<Long> documentIds) {
        SearchModel.Request searchRequest = this.getSearchRequest(query, method, documentIds);
        SearchModel.Response searchResponse = this.getSearchResponse(searchRequest);
        
        List<DocumentStats> results = new ArrayList<DocumentStats>();

        double maxScore = getMaxScore(searchResponse);

        for(int i = 0; i < searchResponse.getRelevantDocumentsCount(); i++) {
            double score = normalizeScore(searchResponse.getRelevantDocuments(i).getScore(), maxScore);
            Long documentId = searchResponse.getRelevantDocuments(i).getDocumentId();

            results.add(new DocumentStats(documentId, score));
        }
        return results;
    }

    public List<DocumentEmbeddingProjection> getTopMatching(int numResults, List<DocumentStats> documentStats) {
        List<DocumentEmbeddingProjection> results = new ArrayList<>();
        documentStats.sort((o1,o2) -> o2.getScore().compareTo(o1.getScore()));
        List<Long> documentIds = documentStats.stream().map(item -> item.getDocumentId()).collect(Collectors.toList());
        List<Document> documents = documentRepository.findAllById(documentIds.subList(0, numResults));
        for(Document document: documents) {
            DocumentEmbeddingProjection documentEmbeddingProjection = 
                new DocumentEmbeddingProjection(document.getUrl(), document.getName());
            results.add(documentEmbeddingProjection);
        }
        return results;
    }
}
