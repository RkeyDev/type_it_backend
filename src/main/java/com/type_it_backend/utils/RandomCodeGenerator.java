package com.type_it_backend.utils;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RandomCodeGenerator {

    private static final long CODE_EXPIRATION_TIME = 5 * 60 * 60 * 1000; // 5 hours
    private static final int CODE_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Stores random codes and their expiration times
    public static final ConcurrentHashMap<String, Long> randomCodes = new ConcurrentHashMap<>();

    // Scheduler for cleaning expired codes
    private static final ScheduledExecutorService cleanerScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "RandomCodeCleanerThread");
        t.setDaemon(true); // Won't prevent app shutdown
        return t;
    });


    public static void initialize() {
        startCleanerThread();
    }

    public static String generateRandomCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            code = sb.toString();
        } while (randomCodes.containsKey(code));

        long expirationTime = System.currentTimeMillis() + CODE_EXPIRATION_TIME;
        randomCodes.put(code, expirationTime);
        return code;
    }

    public static boolean removeCode(String code) {
        if (isExists(code)) {
            randomCodes.remove(code);
            return true;
        }
        return false;
    }

    public static boolean isExists(String code) {
        if (isExpired(code)) {
            randomCodes.remove(code);
            return false;
        }
        return randomCodes.containsKey(code);
    }

    public static boolean isExpired(String code) {
        Long expirationTime = randomCodes.get(code);
        return expirationTime == null || System.currentTimeMillis() > expirationTime;
    }

    /** 
     * Cleaner thread using ScheduledExecutorService for scalable production use
     */
    private static void startCleanerThread() {
        cleanerScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            // Efficient removal using entrySet
            randomCodes.entrySet().removeIf(entry -> entry.getValue() < now);
        }, 1, 1, TimeUnit.MINUTES);
    }

    /** 
        Call this on app shutdown to cleanly stop the scheduler 
     */
    public static void shutdown() {
        cleanerScheduler.shutdownNow();
    }
}
