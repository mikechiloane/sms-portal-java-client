package com.recceda.smsportal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Response model for POST /v1/bulkmessages.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class BulkMessageResponse {

    private double cost;

    private double remainingBalance;

    private long eventId;

    private String sample;

    private int messages;

    private int parts;

    private List<CostBreakDown> costBreakdown;

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

    public List<CostBreakDown> getCostBreakdown() {
        return costBreakdown;
    }

    public void setCostBreakdown(List<CostBreakDown> costBreakdown) {
        this.costBreakdown = costBreakdown;
    }

    public ErrorReport getErrorReport() {
        return errorReport;
    }

    public void setErrorReport(ErrorReport errorReport) {
        this.errorReport = errorReport;
    }

    public static class CostBreakDown {

        private String network;

        private double cost;

        private int quantity;

        public CostBreakDown() {}

        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }

        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorReport {

        private int noNetwork;

        private int duplicates;

        private int optedOuts;

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
