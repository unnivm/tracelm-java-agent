package org.usbtechno.collector;

import java.util.*;
import java.util.stream.*;

public class HeuristicEvaluator {

    private static final Set<String> STOP_WORDS = Set.of(
            "the","is","a","an","and","or","to","of","in","on","for","with","as","by","at"
    );

    public static double evaluate(String prompt, String response) {

        if (response == null || response.isBlank()) return 0.0;

        double relevance = relevanceScore(prompt, response);
        double coverage = coverageScore(prompt, response);
        double length = lengthScore(response);
        double structure = structureScore(response);
        double coherence = coherenceScore(response);
        double penalty = penaltyScore(response);

        double score =
                0.30 * relevance +
                        0.20 * coverage +
                        0.15 * length +
                        0.15 * structure +
                        0.10 * coherence +
                        0.10 * penalty;

        return round(score);
    }

    // -----------------------------
    // 1. Relevance (keyword overlap)
    // -----------------------------
    private static double relevanceScore(String prompt, String response) {
        Set<String> promptWords = extractKeywords(prompt);
        Set<String> responseWords = extractKeywords(response);

        long match = promptWords.stream()
                .filter(responseWords::contains)
                .count();

        return promptWords.isEmpty() ? 0 : (double) match / promptWords.size();
    }

    // -----------------------------
    // 2. Coverage (how much addressed)
    // -----------------------------
    private static double coverageScore(String prompt, String response) {
        int promptLength = prompt.split(" ").length;
        int responseLength = response.split(" ").length;

        if (promptLength == 0) return 0;

        double ratio = (double) responseLength / promptLength;

        return Math.min(ratio / 5.0, 1.0); // normalize
    }

    // -----------------------------
    // 3. Length Quality
    // -----------------------------
    private static double lengthScore(String response) {
        int len = response.length();

        if (len < 30) return 0.2;
        if (len < 80) return 0.5;
        if (len < 200) return 0.8;
        return 1.0;
    }

    // -----------------------------
    // 4. Structure (sentences, formatting)
    // -----------------------------
    private static double structureScore(String response) {
        int sentences = response.split("[.!?]").length;

        double score = 0;

        if (sentences >= 2) score += 0.5;
        if (response.contains("\n")) score += 0.2;
        if (response.contains(":") || response.contains("-")) score += 0.3;

        return Math.min(score, 1.0);
    }

    // -----------------------------
    // 5. Coherence (simple readability proxy)
    // -----------------------------
    private static double coherenceScore(String response) {
        String[] words = response.split(" ");
        int longWords = (int) Arrays.stream(words)
                .filter(w -> w.length() > 4)
                .count();

        double ratio = (double) longWords / words.length;

        return Math.min(ratio, 1.0);
    }

    // -----------------------------
    // 6. Negative signals (penalty)
    // -----------------------------
    private static double penaltyScore(String response) {
        String lower = response.toLowerCase();

        double penalty = 1.0;

        if (lower.contains("i don't know")) penalty -= 0.4;
        if (lower.contains("sorry")) penalty -= 0.2;
        if (lower.contains("error")) penalty -= 0.3;

        return Math.max(penalty, 0);
    }

    // -----------------------------
    // Utility
    // -----------------------------
    private static Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());
    }

    private static double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
