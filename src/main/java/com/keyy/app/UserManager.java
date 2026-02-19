package com.keyy.app;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class UserManager {

    private static final String USER_DATA_DIR = "user_data";
    private static final String USERS_FILE = USER_DATA_DIR + "/users.txt";

    public static void initialize() {
        try {
            Files.createDirectories(Paths.get(USER_DATA_DIR));
            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) usersFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) return false;
        if (userExists(username)) return false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(username + ":" + password);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2 && parts[0].equals(username)) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    public static boolean loginUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password))
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Save a result: wpm|accuracy|timeSeconds|timestamp
    public static void saveResult(String username, double wpm, double accuracy, int timeSeconds) {
        String histFile = USER_DATA_DIR + "/" + username + "_history.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(histFile, true))) {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            writer.write(String.format("%.0f|%.1f|%d|%s", wpm, accuracy, timeSeconds, timestamp));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Returns history newest-first. Each entry: [wpm, accuracy, timeSeconds, timestamp]
    public static List<String[]> getUserHistory(String username) {
        List<String[]> history = new ArrayList<>();
        String histFile = USER_DATA_DIR + "/" + username + "_history.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(histFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) history.add(parts);
            }
        } catch (IOException ignored) {}
        Collections.reverse(history);
        return history;
    }

    // Returns top 10 by best WPM. Each entry: [username, bestWpm]
    public static List<String[]> getLeaderboard() {
        Map<String, Double> bestWpm = new HashMap<>();
        File dir = new File(USER_DATA_DIR);
        if (!dir.exists()) return new ArrayList<>();

        File[] files = dir.listFiles();
        if (files == null) return new ArrayList<>();

        for (File f : files) {
            if (!f.getName().endsWith("_history.txt")) continue;
            String uname = f.getName().replace("_history.txt", "");
            double best = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 1) {
                        try {
                            double w = Double.parseDouble(parts[0]);
                            if (w > best) best = w;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (IOException ignored) {}
            if (best > 0) bestWpm.put(uname, best);
        }

        return bestWpm.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> new String[]{e.getKey(), String.format("%.0f", e.getValue())})
                .collect(Collectors.toList());
    }
}