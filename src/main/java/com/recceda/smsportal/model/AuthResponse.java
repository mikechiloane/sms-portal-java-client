package com.recceda.smsportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for the GET /v1/authentication endpoint.
 */
public class AuthResponse {

    @JsonProperty("Token")
    private String token;

    @JsonProperty("Schema")
    private String schema;

    public AuthResponse() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
