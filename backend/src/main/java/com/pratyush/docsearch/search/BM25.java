package com.pratyush.docsearch.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.pratyush.docsearch.model.DocumentData;

public class BM25 extends RankUtils implements RankingMethod {
    private double k1 = 1.2;
    private double b = 0.75;

    public BM25() {
    }

    private double calculateTermFrequency(List<String> words, String term) {
        double count = 0;
        for (String word: words) {
            if(term.equalsIgnoreCase(word)) {
                count++;
            }
        }
        return (double)count / words.size();
    }

    private double getInverseDocumentFrequency(String term, Map<Long, DocumentData> documentResults) {
        double nt = 0;

        for(Long documentId: documentResults.keySet()) {
            DocumentData documentData = documentResults.get(documentId);
            double frequency = documentData.getFrequency(term);

            if(frequency > 0.0) {
                nt++;
            }
        }
        return nt == 0? 0 : Math.log10((documentResults.size() - nt + 0.5) / (nt + 0.5));
    }

    private double getDocumentLength(DocumentData documentData) {
        return documentData.size();
    }

    private double getAverageDocumentLength(Map<Long, DocumentData> documentResults) {
        double lengths = 0.0;
        for(Map.Entry<Long, DocumentData> documentResult: documentResults.entrySet()) {
            lengths += documentResult.getValue().size();
        }
        return lengths/ documentResults.size();
    }

    private Map<String, Double> getTermToInverseDocumentFrequencyMap(List<String> terms, Map<Long, DocumentData> documentResults) {
        Map<String, Double> termToIDF = new HashMap<>();

        for(String term: terms) {
            double idf = getInverseDocumentFrequency(term, documentResults);
            termToIDF.putIfAbsent(term, idf);
        }
        return termToIDF;
    }

    private double calculateDocumentScore(List<String> terms, DocumentData documentData,
        Map<String, Double> termToInverseDocumentFrequency, double averageDocumentLength) {
        // numerator = term_frequency * (k1 + 1)
        // denominator = term_frequency + k1 * (1 - b + b * (document_length / avg_document_length))

        double score = 0;
        for(String term: terms) {
            double termFrequency = documentData.getFrequency(term);
            double num = termFrequency * (this.k1 + 1);
            double denom = termFrequency + this.k1 * (1 - this.b + this.b * (this.getDocumentLength(documentData)/ averageDocumentLength));
            double inverseDocumentFrequency = termToInverseDocumentFrequency.get(term);
            score += inverseDocumentFrequency * (num/denom);
        }
        return score;
    }

    private void addDocumentScoreToTreeMap(TreeMap<Double, List<Long>> scoreToDocId, double score,
            Long documentId) {
        List<Long> documentIdsWithCurrentScore = scoreToDocId.get(score);
        if(documentIdsWithCurrentScore == null) {
            documentIdsWithCurrentScore = new ArrayList<>();
        }
        documentIdsWithCurrentScore.add(documentId);
        scoreToDocId.put(score, documentIdsWithCurrentScore);
    }

    @Override
    public DocumentData createDocumentData(List<String> words, List<String> terms) {
        DocumentData documentData = new DocumentData();

        for(String term: terms) {
            double termFrequency = calculateTermFrequency(words, term);
            documentData.putTermFrequency(term, termFrequency);
        }
        return documentData;
    }

    @Override
    public NavigableMap<Double, List<Long>> getDocumentsSortedByScore(List<String> terms,
            Map<Long, DocumentData> documentResults) {
        TreeMap<Double, List<Long>> scoreToDocuments = new TreeMap<>();
        Map<String, Double> termToIDF = getTermToInverseDocumentFrequencyMap(terms, documentResults);
        double averageDocumentLength = getAverageDocumentLength(documentResults);
        for(Long documentId: documentResults.keySet()) {
            DocumentData documentData = documentResults.get(documentId);
            double score = calculateDocumentScore(terms, documentData, termToIDF, averageDocumentLength);
            addDocumentScoreToTreeMap(scoreToDocuments, score, documentId);
        }

        return scoreToDocuments.descendingMap();
    }
}
