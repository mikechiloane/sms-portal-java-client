package com.recceda.smsportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single SMS message to be sent via SMSPortal.
 * Phone numbers must be in E.164 format without the '+' prefix.
 * Example: South Africa +27 81 234 5678 → "27812345678"
 */
public class SmsMessage {

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("content")
    private String content;


    public SmsMessage() {}

    public SmsMessage(String destination, String content) {
        this.destination = destination;
        this.content = content;
    }

    public SmsMessage(String destination, String content, String sender) {
        this.destination = destination;
        this.content = content;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
