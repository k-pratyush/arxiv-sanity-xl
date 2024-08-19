package com.pratyush.docsearch.search;

import com.pratyush.docsearch.model.SerializationUtils;
import com.pratyush.docsearch.model.Task;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import com.pratyush.docsearch.model.DocumentData;
import com.pratyush.docsearch.model.Result;
import com.pratyush.docsearch.server.DatabaseDriver;
import com.pratyush.docsearch.server.OnRequestCallback;

public class SearchWorker implements OnRequestCallback {
    public static final String ENDPOINT = "/task";

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        Task task = (Task) SerializationUtils.deserialize(requestPayload);
        Result result = new Result();
        try {
            result = createResult(task);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return SerializationUtils.serialize(result);
    }

    private Map<Long, String> getDocumentIdToDocument(List<Long> documentIds) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        return databaseDriver.getDocumentIdToDocument(documentIds);
    }

    private Result createResult(Task task) throws FileNotFoundException {
        Map<Long, String> docIdToDocs = getDocumentIdToDocument(task.getDocumentIds());
        Result result = new Result();
        RankingMethod rankingMethod = RankMethodFactory.getRankingAlgorithm(task.getRankingMethod());
        System.out.println("RANKING METHOD: " + rankingMethod);
        for(Map.Entry<Long, String> entry: docIdToDocs.entrySet()) {
            List<String> words = rankingMethod.getWordsFromLine(entry.getValue());
            DocumentData documentData = rankingMethod.createDocumentData(words, task.getSearchTerms());
            result.addDocumentData(entry.getKey(), documentData);
        }

        return result;
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
}
