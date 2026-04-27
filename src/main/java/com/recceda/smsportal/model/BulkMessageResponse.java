package com.recceda.smsportal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for POST /v1/bulkmessages.
 */
public class BulkMessageResponse {

    @JsonProperty("Cost")
    private double cost;

    @JsonProperty("RemainingBalance")
    private double remainingBalance;

    @JsonProperty("EventId")
    private long eventId;

    @JsonProperty("Sample")
    private String sample;

    @JsonProperty("Messages")
    private int messages;

    @JsonProperty("Parts")
    private int parts;

    @JsonProperty("CostBreakDown")
    private List<CostBreakDown> costBreakDown;

    @JsonProperty("ErrorReport")
    private ErrorReport errorReport;

    public BulkMessageResponse() {}

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public int getMessages() {
        return messages;
    }

    public void setMessages(int messages) {
        this.messages = messages;
    }

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    public List<CostBreakDown> getCostBreakDown() {
        return costBreakDown;
    }

    public void setCostBreakDown(List<CostBreakDown> costBreakDown) {
        this.costBreakDown = costBreakDown;
    }

    public ErrorReport getErrorReport() {
        return errorReport;
    }

    public void setErrorReport(ErrorReport errorReport) {
        this.errorReport = errorReport;
    }

    public static class CostBreakDown {

        @JsonProperty("Network")
        private String network;

        @JsonProperty("Cost")
        private double cost;

        @JsonProperty("Quantity")
        private int quantity;

        public CostBreakDown() {}

        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }

        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class ErrorReport {

        @JsonProperty("NoNetwork")
        private int noNetwork;

        @JsonProperty("Duplicates")
        private int duplicates;

        @JsonProperty("OptedOuts")
        private int optedOuts;

        @JsonProperty("Faults")
        private List<Object> faults;

        public ErrorReport() {}

        public int getNoNetwork() { return noNetwork; }
        public void setNoNetwork(int noNetwork) { this.noNetwork = noNetwork; }

        public int getDuplicates() { return duplicates; }
        public void setDuplicates(int duplicates) { this.duplicates = duplicates; }

        public int getOptedOuts() { return optedOuts; }
        public void setOptedOuts(int optedOuts) { this.optedOuts = optedOuts; }

        public List<Object> getFaults() { return faults; }
        public void setFaults(List<Object> faults) { this.faults = faults; }
    }
}
