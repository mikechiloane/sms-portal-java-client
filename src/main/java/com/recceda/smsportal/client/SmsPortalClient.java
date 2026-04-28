package com.recceda.smsportal.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recceda.smsportal.model.BulkMessageRequest;
import com.recceda.smsportal.model.BulkMessageResponse;
import com.recceda.smsportal.model.SmsMessage;

/**
 * Main entry point for the SMSPortal Java client.
 *
 * <p>Usage:
 * <pre>{@code
 * SmsPortalClient client = new SmsPortalClient(
 *     System.getenv("SMSPORTAL_CLIENT_ID"),
 *     System.getenv("SMSPORTAL_API_SECRET")
 * );
 * BulkMessageResponse response = client.sendMessages(
 *     List.of(new SmsMessage("27812345678", "Hello!"))
 * );
 * }</pre>
 *
 * <p>Thread safety: this client is stateless and can be shared across threads.
 */
public class SmsPortalClient {

    private static final String BASE_URL = "https://rest.smsportal.com";

    private final String clientId;
    private final String apiSecret;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a client with the default {@link HttpClient}.
     *
     * @param clientId  SMSPortal API Client ID
     * @param apiSecret SMSPortal API Secret
     */
    public SmsPortalClient(String clientId, String apiSecret) {
        this(clientId, apiSecret, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    /**
     * Creates a client with a custom {@link HttpClient} (useful for testing/proxies).
     *
     * @param clientId   SMSPortal API Client ID
     * @param apiSecret  SMSPortal API Secret
     * @param httpClient custom HTTP client instance
     */
    public SmsPortalClient(String clientId, String apiSecret, HttpClient httpClient) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be null or blank");
        }
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalArgumentException("apiSecret must not be null or blank");
        }
        this.clientId = clientId;
        this.apiSecret = apiSecret;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends one or more SMS messages via SMSPortal.
     *
     * @param messages list of {@link SmsMessage} objects to send
     * @return {@link BulkMessageResponse} containing cost, event ID, and error report
     * @throws SmsPortalException if the API returns an error
     */
    public BulkMessageResponse sendMessages(List<SmsMessage> messages)
            throws SmsPortalException {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be null or empty");
        }

        String token = getToken();
        BulkMessageRequest request = new BulkMessageRequest(messages);

        try {
            String body = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/BulkMessages"))
                    .header("Authorization", "Basic " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = execute(httpRequest);

            // On 401 the token may have expired mid-window; refresh and retry once
            if (response.statusCode() == 401) {

                token = getToken();
                httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/bulkmessages"))
                        .header("Authorization", "Basic " + token)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .timeout(Duration.ofSeconds(30))
                        .build();
                response = execute(httpRequest);
            }

            if (response.statusCode() != 200) {
                throw new SmsPortalException(
                        "Unexpected response from /bulkmessages: HTTP " + response.statusCode()
                                + " — " + response.body(),
                        response.statusCode());
            }

            return objectMapper.readValue(response.body(), BulkMessageResponse.class);

        } catch (SmsPortalException e) {
            throw e;
        } catch (Exception e) {
            throw new SmsPortalException("Failed to send messages: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the Base64-encoded Basic authorization token derived from
     * {@code clientId:apiSecret}.
     *
     * @return base64-encoded token value
     */
    public String getToken() throws SmsPortalException {

        String credentials = clientId + ":" + apiSecret;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Executes an {@link HttpRequest} and returns the {@link HttpResponse}.
     * Extracted for testability.
     */
    HttpResponse<String> execute(HttpRequest request) throws SmsPortalException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new SmsPortalException("HTTP request failed: " + e.getMessage(), e);
        }
    }
}
