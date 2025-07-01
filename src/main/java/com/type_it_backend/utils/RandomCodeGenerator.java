package com.type_it_backend.utils;

import java.util.concurrent.ConcurrentHashMap;

public class RandomCodeGenerator {

    private static final long codeExpirationTime =  5 * (60 * 60 * 1000); // 5 hours in milliseconds
    private static final int CODE_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    // stores random codes and their expiration times
    public static ConcurrentHashMap<String, Long> randomCodes = new ConcurrentHashMap<>(); 

    public static String generateRandomCode() {
        StringBuilder code;
        // Generate a unique random code and ensure it does not already exist
        do {
            code = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                int index = (int) (Math.random() * CHARACTERS.length());
                code.append(CHARACTERS.charAt(index));
            }
        } while (randomCodes.containsKey(code.toString()));

        // Store the code with its expiration time
        String codeString = code.toString();
        long expirationTime = System.currentTimeMillis() + codeExpirationTime;
        randomCodes.put(codeString, expirationTime); // Store with expiration

        return codeString;
    }

    public static boolean removeCode(String code){
        if(isExists(code)) {
            randomCodes.remove(code);
            return true;
        }
        return false;
    }

    public static boolean isExists(String code) {
        if(isExpired(code)) {
            randomCodes.remove(code); // Remove expired code
            return false;
        }

        return randomCodes.containsKey(code);
    }

    public static boolean isExpired(String code) {
        Long expirationTime = randomCodes.get(code);
        if (expirationTime == null) {
            return true; // Code does not exist or has expired
        }
        return System.currentTimeMillis() > expirationTime; // Check if the current time is past the expiration time
    }

    static {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60 * 1000); // Run every minute
                    long now = System.currentTimeMillis();
                    for (String code : randomCodes.keySet()) {
                        if (randomCodes.get(code) < now) {
                            randomCodes.remove(code);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt flag
                    break;
                }
            }
        });
        cleaner.setDaemon(true); // So it doesn't prevent app shutdown
        cleaner.start();
    }

}
