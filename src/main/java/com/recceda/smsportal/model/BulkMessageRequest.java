package com.recceda.smsportal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for bulk SMS submission.
 */
public class BulkMessageRequest {

    @JsonProperty("Messages")
    private List<SmsMessage> messages;


    public BulkMessageRequest() {}

    public BulkMessageRequest(List<SmsMessage> messages) {
        this.messages = messages;
    }

    public List<SmsMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SmsMessage> messages) {
        this.messages = messages;
    }
}
