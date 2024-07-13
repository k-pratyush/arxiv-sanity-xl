package com.pratyush.docsearch.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.zookeeper.KeeperException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pratyush.docsearch.cluster_management.ServiceRegistry;
import com.pratyush.docsearch.model.DocumentData;
import com.pratyush.docsearch.model.Result;
import com.pratyush.docsearch.model.SearchModel;
import com.pratyush.docsearch.model.SerializationUtils;
import com.pratyush.docsearch.model.Task;
import com.pratyush.docsearch.server.OnRequestCallback;
import com.pratyush.docsearch.server.WebClient;

public class SearchCoordinator implements OnRequestCallback {
    private static final String ENDPOINT = "/search";
    private static final String documentDirectory = "./resources/docs/";
    private final ServiceRegistry workerServiceRegistry;
    private final WebClient client;
    private final List<String> documents;

    public SearchCoordinator(ServiceRegistry workerServiceRegistry, WebClient webClient) {
        this.workerServiceRegistry = workerServiceRegistry;
        this.client = webClient;
        this.documents = readDocumentsList();
    }

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        try {
            SearchModel.Request request = SearchModel.Request.parseFrom(requestPayload);
            SearchModel.Response response = createResponse(request);
            return response.toByteArray();

        } catch(InvalidProtocolBufferException e) {
            return SearchModel.Response.getDefaultInstance().toByteArray();
        }
        
    }

    @Override
    public  String getEndpoint() {
        return ENDPOINT;
    }

    private SearchModel.Response createResponse(SearchModel.Request request) {
        SearchModel.Response.Builder searchResponse = SearchModel.Response.newBuilder();

        List<String> searchTerms = TFIDF.getWordsFromLine(request.getSearchQuery());
        List<String> workers;
        try {
            workers = workerServiceRegistry.getServiceAddresses();
            if(workers.isEmpty()) {
                System.out.println("No workers present");
                return searchResponse.build();
            }

            List<Task> tasks = createTask(workers.size(), searchTerms);
            List<Result> results = sendTasksToWorkers(workers, tasks);
            List<SearchModel.Response.DocumentStats> sortedDocs = aggregateResults(results, searchTerms);

            searchResponse.addAllRelevantDocuments(sortedDocs);
            return searchResponse.build();

        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return searchResponse.build();
    }

    private List<SearchModel.Response.DocumentStats> aggregateResults(List<Result> results, List<String> searchTerms) {
        Map<String, DocumentData> allDocResults = new HashMap<>();

        for(Result result: results) {
            allDocResults.putAll(result.getDocumentToDocumentData());
        }

        Map<Double, List<String>> scoreToDocs = TFIDF.getDocumentsSortedByScore(searchTerms, allDocResults);

        return sortDocsByScore(scoreToDocs);
    }

    private List<SearchModel.Response.DocumentStats> sortDocsByScore(Map<Double, List<String>> scoreToDocs) {
        List<SearchModel.Response.DocumentStats> sortedDocs = new ArrayList<>();

        for(Map.Entry<Double, List<String>> scoreDocPair: scoreToDocs.entrySet()) {
            double score = scoreDocPair.getKey();

            for(String doc: scoreDocPair.getValue()) {
                File documentPath = new File(doc);

                SearchModel.Response.DocumentStats documentStats = SearchModel.Response.DocumentStats.newBuilder()
                    .setScore(score)
                    .setDocumentName(documentPath.getName())
                    .setDocumentSize(documentPath.length())
                    .build();
                sortedDocs.add(documentStats);
            }
        }

        return sortedDocs;
    }

    private List<Result> sendTasksToWorkers(List<String> workers, List<Task> tasks) {
        CompletableFuture<Result>[] futures = new CompletableFuture[workers.size()];

        for(int i = 0; i < workers.size(); i++) {
            String worker = workers.get(i);
            Task task = tasks.get(i);

            byte[] payload = SerializationUtils.serialize(task);
            futures[i] = client.sendTask(worker, payload);
        }

        List<Result> results = new ArrayList<>();
        for(CompletableFuture<Result> future: futures) {
            try {
                Result result = future.get();
                results.add(result);
            } catch (InterruptedException | ExecutionException e) {

            }
        }

        return results;
    }
    
    private List<Task> createTask(int numWorkers, List<String> searchTerms) {

        List<List<String>> workerDocs = splitDocumnentsList(numWorkers, documents);

        List<Task> tasks = new ArrayList<>();

        for(List<String> docsForWorker: workerDocs) {
            Task task = new Task(searchTerms, docsForWorker);
            tasks.add(task);
        }
        return tasks;
    }

    private List<List<String>> splitDocumnentsList(int numWorkers, List<String> documents) {
        int numDocsPerWorker = (documents.size() + numWorkers - 1) / numWorkers;

        List<List<String>> workerDocs = new ArrayList<>();

        for(int i = 0; i < numWorkers; i++) {
            int firstDocIdx = i * numDocsPerWorker;
            int lastDocIdx = Math.min(firstDocIdx + numDocsPerWorker, documents.size());

            if(firstDocIdx > lastDocIdx) {
                break;
            }

            List<String> currentWorkerDocs = new ArrayList<>(documents.subList(firstDocIdx, lastDocIdx));
            workerDocs.add(currentWorkerDocs);
        }
        return workerDocs;
    }

    private List<String> readDocumentsList() {
        File docDirectory = new File(documentDirectory);
        return Arrays.asList(docDirectory.list())
            .stream()
            .map(docName -> documentDirectory + "/" + docName)
            .collect(Collectors.toList());
    }

}