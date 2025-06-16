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

    private HashMap<String,String> stringToHashMap(String data){
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private RequestType stringToRequestType(String requestType) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }


}
