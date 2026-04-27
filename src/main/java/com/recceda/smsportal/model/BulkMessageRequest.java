package com.recceda.smsportal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /v1/bulkmessages.
 */
public class BulkMessageRequest {

    @JsonProperty("Messages")
    private List<SmsMessage> messages;

    @JsonProperty("SendOptions")
    private SendOptions sendOptions;

    public BulkMessageRequest() {}

    public BulkMessageRequest(List<SmsMessage> messages, boolean testMode) {
        this.messages = messages;
        this.sendOptions = new SendOptions(testMode);
    }

    public List<SmsMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SmsMessage> messages) {
        this.messages = messages;
    }

    public SendOptions getSendOptions() {
        return sendOptions;
    }

    public void setSendOptions(SendOptions sendOptions) {
        this.sendOptions = sendOptions;
    }

    public static class SendOptions {

        @JsonProperty("TestMode")
        private boolean testMode;

        public SendOptions() {}

        public SendOptions(boolean testMode) {
            this.testMode = testMode;
        }

        public boolean isTestMode() {
            return testMode;
        }

        public void setTestMode(boolean testMode) {
            this.testMode = testMode;
        }
    }
}
