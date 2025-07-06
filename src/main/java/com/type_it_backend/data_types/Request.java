package com.type_it_backend.data_types;

import org.java_websocket.WebSocket;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.enums.RequestType;
import com.fasterxml.jackson.core.type.TypeReference;

public class Request {
    private WebSocket senderConn;
    private RequestType requestType;
    private HashMap<String, Object> data;

    public Request(WebSocket senderConn, RequestType requestType, HashMap<String, Object> data) {
        this.senderConn = senderConn;
        this.requestType = requestType;
        this.data = data;
    }   

    public static HashMap<String,Object> stringToHashMap(String data){
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HashMap<String, Object> map = objectMapper.readValue(data, new TypeReference<HashMap<String, Object>>() {});
            return map;
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static Request stringToRequest(String requestType,WebSocket senderConn) {
        // Convert the requestType string to a HashMap
        HashMap<String,Object> requestHashMap = stringToHashMap(requestType);

        RequestType request_type = RequestType.valueOf(String.valueOf(requestHashMap.get("type")).toUpperCase());
        HashMap<String,Object> data = new HashMap<>();
        if (requestHashMap.get("data") != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            data = objectMapper.convertValue(requestHashMap.get("data"), new TypeReference<HashMap<String, Object>>() {});
        }

        try{
            return new Request(senderConn, request_type, data);
        }
        catch (Exception e) {
            System.out.println("Error creating Request object: " + e.getMessage());
            return null;
        }

    }

    public void setSenderConn(WebSocket senderConn) {
        this.senderConn = senderConn;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public WebSocket getSenderConn() {
        return senderConn;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public HashMap<String, Object> getData() {
        return data;
    }


}
