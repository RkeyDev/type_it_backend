package com.type_it_backend.utils;

import java.util.concurrent.ConcurrentHashMap;

public class RandomCodeGenerator {

    private static final long codeExpirationTime =  5 * (60 * 60 * 1000); // 5 hours in milliseconds
    private static final int CODE_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    // stores random codes and their expiration times
    public static ConcurrentHashMap<String, Long> randomCodes = new ConcurrentHashMap<>(); 

    public static String generateRandomCode() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public static boolean removeCode(String code){
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private boolean isExists(String code) {
       throw new UnsupportedOperationException("Method not implemented yet");
    }

    private boolean isExpired(String code) {
       throw new UnsupportedOperationException("Method not implemented yet"); 
    }
    
}
