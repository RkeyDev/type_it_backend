package com.type_it_backend.utils;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.enums.ResponseType;

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


    public static String buildErrorResponse(ResponseType errorType,String errorMessage) {
        HashMap<String, Object> errorData = new HashMap<>();
        errorData.put("error", errorMessage);
        
        return buildResponse(errorData);
    }
}
