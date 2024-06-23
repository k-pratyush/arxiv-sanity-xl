package com.pratyush.docsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pratyush.docsearch.model.DocumentData;
import com.pratyush.docsearch.search.TFIDF;

public class SequentialSearch {
    public static final String BOOKS_DIRECTORY = "./resources/books";
    public static final String SEARCH_QUERY_1 = "Algorithms";

    public static void main(String[] args) throws FileNotFoundException {
        File documentsDirectory = new File(BOOKS_DIRECTORY);

        List<String> documents = Arrays.asList(documentsDirectory.list())
            .stream()
            .map(documentName -> BOOKS_DIRECTORY + "/" + documentName)
            .collect(Collectors.toList());

        List<String> terms = TFIDF.getWordsFromLine(SEARCH_QUERY_1);

        findMostRelevantDocuments(documents, terms);
    }

    private static void findMostRelevantDocuments(List<String> documents, List<String> terms) throws FileNotFoundException {
        Map<String, DocumentData> documentResults = new HashMap<>();

        for(String document: documents) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(document))) {
                List<String> lines = bufferedReader.lines().collect(Collectors.toList());
                List<String> words = TFIDF.getWordsFromLines(lines);

                DocumentData documentData = TFIDF.createDocumentData(words, terms);
                documentResults.put(document, documentData);
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<Double, List<String>> documentsByScore = TFIDF.getDocumentsSortedByScore(terms, documentResults);
        printResults(documentsByScore);
    }

    private static void printResults(Map<Double, List<String>> documentsByScore) {
        for(Map.Entry<Double, List<String>> docScorePair: documentsByScore.entrySet()) {
            double score = docScorePair.getKey();
            for(String document: docScorePair.getValue()) {
                System.out.println(String.format("Book: %s - Score: %f", document.split("/")[3], score));
            }
        }
    }
}
