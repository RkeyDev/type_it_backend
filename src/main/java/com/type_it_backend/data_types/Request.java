package com.type_it_backend.data_types;

import java.net.http.WebSocket;
import java.util.HashMap;

import com.type_it_backend.enums.RequestType;

public class Request {
    private WebSocket senderConn;
    private RequestType requestType;
    private HashMap<String, String> data;

    Request(WebSocket senderConn, RequestType requestType, HashMap<String, String> data) {
        this.senderConn = senderConn;
        this.requestType = requestType;
        this.data = data;
    }   

    public static HashMap<String,String> stringToHashMap(String data){
        HashMap<String, String> map = new HashMap<>();
        if (data == null || data.isEmpty()) {
            return map;
        }
        String[] pairs = data.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");

            if (keyValue.length == 2) { 
                map.put(keyValue[0].trim(), keyValue[1].trim()); 
            }
        }
        return map;
    }

    private RequestType stringToRequestType(String requestType) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void setSenderConn(WebSocket senderConn) {
        this.senderConn = senderConn;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public WebSocket getSenderConn() {
        return senderConn;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public HashMap<String, String> getData() {
        return data;
    }


}
