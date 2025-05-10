package com.type_it_backend.data_structure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientRequest{
    @JsonProperty("type")
    private String type;
    
    private String jsonString;

    public ClientRequest() {} // Default constructor for Jackson

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }



} 
