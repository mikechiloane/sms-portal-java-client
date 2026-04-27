package com.recceda.smsportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single SMS message to be sent via SMSPortal.
 * Phone numbers must be in E.164 format without the '+' prefix.
 * Example: South Africa +27 81 234 5678 → "27812345678"
 */
public class SmsMessage {

    @JsonProperty("Destination")
    private String destination;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("Sender")
    private String sender;

    @JsonProperty("SendTime")
    private String sendTime;

    public SmsMessage() {}

    public SmsMessage(String destination, String content) {
        this.destination = destination;
        this.content = content;
    }

    public SmsMessage(String destination, String content, String sender) {
        this.destination = destination;
        this.content = content;
        this.sender = sender;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Optional ISO 8601 datetime string for scheduled/delayed sends.
     * Example: "2026-05-01T10:00:00Z"
     */
    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}
