package com.type_it_backend.utils;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:postgresql://ep-broad-queen-ag83mq5w-pooler.c-2.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require";
    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASS = "npg_Ldnlir5v2FSU";

    private static final List<String> preloadedQuestions = new ArrayList<>();

    static {
        try {
            Class.forName("org.postgresql.Driver"); // Load driver
            preloadQuestions(); // Load all questions at startup
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getPreloadedQuestions(){
        return preloadedQuestions;
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /** Preload all questions from DB into memory */
    private static void preloadQuestions() {
        String sql = "SELECT question FROM questions";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String question = rs.getString("question");
                if (question != null && !question.isEmpty()) {
                    preloadedQuestions.add(question);
                }
            }

            System.out.println("Preloaded " + preloadedQuestions.size() + " questions from DB");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Get a random question from the preloaded list */
    public static String getRandomQuestion() {
        if (preloadedQuestions.isEmpty()) return null;
        int index = (int) (Math.random() * preloadedQuestions.size());
        return preloadedQuestions.get(index);
    }

    /** Fetch possible answers from DB (still per round, on demand) */
    public static List<String> getPossibleAnswers(String question) {
        List<String> answers = new ArrayList<>();
        String sql = "SELECT possible_answers FROM questions WHERE question = ?";

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
            e.printStackTrace();
        }

        return answers;
    }
}
