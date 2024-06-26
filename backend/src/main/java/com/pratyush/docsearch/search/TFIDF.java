package com.pratyush.docsearch.search;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.pratyush.docsearch.model.DocumentData;

public class TFIDF {
    public static double calculateTermFrequency(List<String> words, String term) {
        long count = 0;
        for (String word: words) {
            if(term.equalsIgnoreCase(word)) {
                count++;
            }
        }
        return (double)count / words.size();
    }

    public static DocumentData createDocumentData(List<String> words, List<String> terms) {
        DocumentData documentData = new DocumentData();

        for(String term: terms) {
            double termFrequency = calculateTermFrequency(words, term);
            documentData.putTermFrequency(term, termFrequency);
        }
        return documentData;
    }

    private static double getInverseDocumentFrequency(String term, Map<String, DocumentData> documentResults) {
        double nt = 0;

        for(String document: documentResults.keySet()) {
            DocumentData documentData = documentResults.get(document);
            double frequency = documentData.getFrequency(term);

            if(frequency > 0.0) {
                nt++;
            }
        }
        return nt == 0? 0 : Math.log10(documentResults.size() / nt);
    }

    private static Map<String, Double> getTermToInverseDocumentFrequencyMap(List<String> terms, Map<String, DocumentData> documentResults) {
        Map<String, Double> termToIDF = new HashMap<>();

        for(String term: terms) {
            double idf = getInverseDocumentFrequency(term, documentResults);
            termToIDF.putIfAbsent(term, idf);
        }
        return termToIDF;
    }

    public static Map<Double, List<String>> getDocumentsSortedByScore(List<String> terms, Map<String, DocumentData> documentResults) {
        TreeMap<Double, List<String>> scoreToDocuments = new TreeMap<>();

        Map<String, Double> termToIDF = getTermToInverseDocumentFrequencyMap(terms, documentResults);
        for(String document: documentResults.keySet()) {
            DocumentData documentData = documentResults.get(document);
            double score = calculateDocumentScore(terms, documentData, termToIDF);
            addDocumentScoreToTreeMap(scoreToDocuments, score, document);
        }
        
        return scoreToDocuments.descendingMap();
    }

    private static double calculateDocumentScore(List<String> terms, DocumentData documentData, Map<String, Double> termToInverseDocumentFrequency) {
        double score = 0;
        for(String term: terms) {
            double termFrequency = documentData.getFrequency(term);
            double inverseDocumentFrequency = termToInverseDocumentFrequency.get(term);

            score += termFrequency * inverseDocumentFrequency;
        }
        return score;
    }

    private static void addDocumentScoreToTreeMap(TreeMap<Double, List<String>> scoreToDoc, double score, String document) {
        List<String> documentsWithCurrentScore = scoreToDoc.get(score);

        if(documentsWithCurrentScore == null) {
            documentsWithCurrentScore = new ArrayList<>();
        }

        documentsWithCurrentScore.add(document);
        scoreToDoc.put(score, documentsWithCurrentScore);
    }

    public static List<String> getWordsFromLine(String line) {
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public static List<String> getWordsFromLines(List<String> lines) {
        List<String> words = new ArrayList<>();
        for(String line: lines) {
            words.addAll(getWordsFromLine(line));
        }
        return words;
    }
}