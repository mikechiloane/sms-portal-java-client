package com.recceda.smsportal.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recceda.smsportal.model.AuthResponse;
import com.recceda.smsportal.model.BulkMessageRequest;
import com.recceda.smsportal.model.BulkMessageResponse;
import com.recceda.smsportal.model.SmsMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

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
 *     List.of(new SmsMessage("27812345678", "Hello!")),
 *     true  // testMode
 * );
 * }</pre>
 *
 * <p>Thread safety: {@code getToken()} is synchronized. Safe for shared use across threads.
 */
public class SmsPortalClient {

    private static final String BASE_URL = "https://rest.smsportal.com/v1";
    private static final long TOKEN_CACHE_MILLIS = 23 * 60 * 60 * 1000L; // 23 hours

    private final String clientId;
    private final String apiSecret;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String cachedToken;
    private Instant tokenExpiry;

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
     * @param testMode if {@code true}, validates without sending real SMSes or consuming credits
     * @return {@link BulkMessageResponse} containing cost, event ID, and error report
     * @throws SmsPortalException if the API returns an error
     */
    public BulkMessageResponse sendMessages(List<SmsMessage> messages, boolean testMode)
            throws SmsPortalException {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be null or empty");
        }

        String token = getToken();
        BulkMessageRequest request = new BulkMessageRequest(messages, testMode);

        try {
            String body = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/bulkmessages"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = execute(httpRequest);

            // On 401 the token may have expired mid-window; refresh and retry once
            if (response.statusCode() == 401) {
                synchronized (this) {
                    cachedToken = null;
                    tokenExpiry = null;
                }
                token = getToken();
                httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/bulkmessages"))
                        .header("Authorization", "Bearer " + token)
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
     * Returns a valid Bearer token, fetching a new one if the cached token is absent or expired.
     * This method is synchronized to be safe for multi-threaded use.
     *
     * @return bearer token string
     * @throws SmsPortalException if authentication fails
     */
    public synchronized String getToken() throws SmsPortalException {
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        String credentials = clientId + ":" + apiSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/authentication"))
                    .header("Authorization", "Basic " + encoded)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = execute(request);

            if (response.statusCode() != 200) {
                throw new SmsPortalException(
                        "Authentication failed: HTTP " + response.statusCode()
                                + " — " + response.body(),
                        response.statusCode());
            }

            AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
            cachedToken = authResponse.getToken();
            tokenExpiry = Instant.now().plusMillis(TOKEN_CACHE_MILLIS);
            return cachedToken;

        } catch (SmsPortalException e) {
            throw e;
        } catch (Exception e) {
            throw new SmsPortalException("Authentication request failed: " + e.getMessage(), e);
        }
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
