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
            File f = new File(USERS_FILE);
            if (!f.exists()) f.createNewFile();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) return false;
        if (userExists(username)) return false;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            w.write(username + ":" + password); w.newLine(); return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public static boolean userExists(String username) {
        try (BufferedReader r = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(":");
                if (p.length >= 2 && p[0].equals(username)) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    public static boolean loginUser(String username, String password) {
        try (BufferedReader r = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(":");
                if (p.length >= 2 && p[0].equals(username) && p[1].equals(password)) return true;
            }
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    public static void saveResult(String username, double wpm, double accuracy, int timeSeconds) {
        String histFile = USER_DATA_DIR + "/" + username + "_history.txt";
        try (BufferedWriter w = new BufferedWriter(new FileWriter(histFile, true))) {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            w.write(String.format("%.0f|%.1f|%d|%s", wpm, accuracy, timeSeconds, ts));
            w.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Returns newest-first. Each entry: [wpm, accuracy, timeSeconds, timestamp]
    public static List<String[]> getUserHistory(String username) {
        List<String[]> list = new ArrayList<>();
        String histFile = USER_DATA_DIR + "/" + username + "_history.txt";
        try (BufferedReader r = new BufferedReader(new FileReader(histFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 4) list.add(p);
            }
        } catch (IOException ignored) {}
        Collections.reverse(list);
        return list;
    }

    public static void clearHistory(String username) {
        java.io.File f = new java.io.File(USER_DATA_DIR + "/" + username + "_history.txt");
        if (f.exists()) f.delete();
    }

    // Top 10 by best WPM. Each entry: [username, bestWpm]
    public static List<String[]> getLeaderboard() {
        Map<String, Double> best = new HashMap<>();
        File dir = new File(USER_DATA_DIR);
        if (!dir.exists()) return new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return new ArrayList<>();
        for (File f : files) {
            if (!f.getName().endsWith("_history.txt")) continue;
            String uname = f.getName().replace("_history.txt", "");
            double topWpm = 0;
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] p = line.split("\\|");
                    if (p.length >= 1) {
                        try { double w = Double.parseDouble(p[0]); if (w > topWpm) topWpm = w; }
                        catch (NumberFormatException ignored) {}
                    }
                }
            } catch (IOException ignored) {}
            if (topWpm > 0) best.put(uname, topWpm);
        }
        return best.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> new String[]{e.getKey(), String.format("%.0f", e.getValue())})
                .collect(Collectors.toList());
    }
}