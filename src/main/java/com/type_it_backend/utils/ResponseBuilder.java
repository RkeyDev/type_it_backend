package com.type_it_backend.utils;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseBuilder {
    public static String buildResponse(HashMap<String, Object> data) {
        
        if (data == null || data.isEmpty()) {
            return "No data provided";
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            return jsonData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
}
