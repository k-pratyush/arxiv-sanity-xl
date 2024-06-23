package com.pratyush.docsearch.search;

import com.pratyush.docsearch.model.SerializationUtils;
import com.pratyush.docsearch.model.Task;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.pratyush.docsearch.model.DocumentData;
import com.pratyush.docsearch.model.Result;
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

    private Result createResult(Task task) throws FileNotFoundException {
        List<String> documents = task.getDocuments();
        Result result = new Result();

        for(String document: documents) {
            List<String> words = parseWordsFromDocument(document);
            DocumentData documentData = TFIDF.createDocumentData(words, task.getSearchTerms());
            result.addDocumentData(document, documentData);
        }
        return result;
    }

    private List<String> parseWordsFromDocument(String document) throws FileNotFoundException {
        FileReader fileReader = new FileReader(document);
        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromLines(lines);
            return words;
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
}
