package com.type_it_backend.utils;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.type_it_backend.enums.DatabaseTable;
import com.type_it_backend.enums.Language;

public class DatabaseManager {

    // --- Database Configuration ---
    private static final String DB_URL = "jdbc:postgresql://ep-broad-queen-ag83mq5w-pooler.c-2.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require";
    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASS = "npg_Ldnlir5v2FSU";

    // --- Cached Preloaded Questions ---
    private static final Map<DatabaseTable, List<String>> preloadedQuestions = new HashMap<>();
    private static boolean preloaded = false;

    // ===========================================
    // ===========  PRELOADING PHASE  ============
    // ===========================================

    public static synchronized void loadAllTables() {
        if (preloaded) {
            System.out.println("[INFO] Skipping reload — tables already preloaded.");
            return;
        }

        try {
            Class.forName("org.postgresql.Driver");

            for (Language lang : Language.values()) {
                DatabaseTable table = lang.getDatabaseTableName();
                List<String> questions = new ArrayList<>();

                try {
                    preloadQuestions(lang, questions);
                    preloadedQuestions.put(table, Collections.unmodifiableList(questions));
                    System.out.println("[LOAD] Preloaded " + questions.size() + " questions for " + lang);
                } catch (Exception e) {
                    System.err.println("[LOAD] ❌ Failed to preload questions for " + lang + ": " + e.getMessage());
                    preloadedQuestions.put(table, Collections.emptyList());
                }
            }

            preloaded = true;
            System.out.println("[INFO] All question tables preloaded successfully.");

        } catch (ClassNotFoundException e) {
            System.err.println("[FATAL] PostgreSQL driver not found!");
            e.printStackTrace();
        }
    }

    private static void preloadQuestions(Language language, List<String> questions) {
        DatabaseTable table = language.getDatabaseTableName();
        String sql = "SELECT question FROM " + table.getResourceName();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String q = rs.getString("question");
                if (q != null && !q.isEmpty()) {
                    questions.add(q.trim());
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("SQL error while preloading " + language + ": " + e.getMessage(), e);
        }
    }

    // ===========================================
    // ===========  PUBLIC UTILITIES  ============
    // ===========================================

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Safely returns immutable preloaded question list
    public static List<String> getPreloadedQuestions(Language language) {
        if (language == null) {
            System.err.println("[WARN] getPreloadedQuestions called with null language!");
            return Collections.emptyList();
        }

        DatabaseTable table = language.getDatabaseTableName();
        List<String> questions = preloadedQuestions.get(table);

        if (questions == null || questions.isEmpty()) {
            System.err.println("[WARN] No preloaded questions found for " + language + " (" + table + ")");
            return Collections.emptyList();
        }

        return questions;
    }

    // Random access for rare fallback use
    public static String getRandomQuestion(DatabaseTable table) {
        List<String> list = preloadedQuestions.get(table);
        if (list == null || list.isEmpty()) return null;
        int index = (int) (Math.random() * list.size());
        return list.get(index);
    }

    // Fetch possible answers for a given question
    public static List<String> getPossibleAnswers(String question, DatabaseTable dbTable) {
        List<String> answers = new ArrayList<>();
        if (question == null || dbTable == null) return answers;

        String sql = "SELECT possible_answers FROM " + dbTable.getResourceName() + " WHERE question = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, question);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Array arr = rs.getArray("possible_answers");
                    if (arr != null) {
                        Object[] words = (Object[]) arr.getArray();
                        for (Object w : words) {
                            if (w != null) answers.add(w.toString().trim());
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch possible answers for question: " + question);
            e.printStackTrace();
        }

        return answers;
    }

    // ===========================================
    // ============  DEBUG HELPERS  ==============
    // ===========================================

    public static void printPreloadedSummary() {
        System.out.println("=== Preloaded Question Summary ===");
        for (Map.Entry<DatabaseTable, List<String>> entry : preloadedQuestions.entrySet()) {
            System.out.println(entry.getKey().name() + " → " + entry.getValue().size() + " questions");
        }
        System.out.println("==================================");
    }
}
