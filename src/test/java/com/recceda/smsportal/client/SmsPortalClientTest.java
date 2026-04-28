package com.recceda.smsportal.client;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.recceda.smsportal.model.BulkMessageResponse;
import com.recceda.smsportal.model.SmsMessage;

class SmsPortalClientTest {

    private static final String SEND_JSON =
            "{\"cost\":1,\"remainingBalance\":499,\"eventId\":12345678901," +
            "\"sample\":\"Hello!\",\"messages\":1,\"parts\":1," +
            "\"costBreakdown\":[{\"network\":\"Local\",\"cost\":1,\"quantity\":1}]," +
            "\"errorReport\":{\"noNetwork\":0,\"duplicates\":0,\"optedOuts\":0,\"faults\":[]}}";

    /** Creates a fake {@link HttpResponse} with the given status code and body. */
    private static HttpResponse<String> fakeResponse(int status, String body) {
        return new HttpResponse<>() {
            @Override public int statusCode() { return status; }
            @Override public String body() { return body; }
            @Override public java.net.http.HttpRequest request() { return null; }
            @Override public java.util.Optional<HttpResponse<String>> previousResponse() { return java.util.Optional.empty(); }
            @Override public java.net.http.HttpHeaders headers() { return null; }
            @Override public java.net.URI uri() { return null; }
            @Override public java.net.http.HttpClient.Version version() { return null; }
            @Override public java.util.Optional<javax.net.ssl.SSLSession> sslSession() { return java.util.Optional.empty(); }
        };
    }

    /**
     * Test subclass that intercepts {@link SmsPortalClient#execute} with pre-queued responses,
     * avoiding any real HTTP calls.
     */
    private static class StubClient extends SmsPortalClient {

        private final Deque<HttpResponse<String>> responses = new ArrayDeque<>();
        private int callCount = 0;

        @SafeVarargs
        StubClient(HttpResponse<String>... stubResponses) {
            super("test-id", "test-secret");
            for (HttpResponse<String> r : stubResponses) {
                responses.add(r);
            }
        }

        @Override
        HttpResponse<String> execute(HttpRequest request) throws SmsPortalException {
            callCount++;
            if (responses.isEmpty()) {
                throw new SmsPortalException("No more stub responses queued");
            }
            return responses.poll();
        }

        int getCallCount() { return callCount; }
    }

    @Test
    void sendMessages_successfulResponse() throws Exception {
        StubClient client = new StubClient(
                fakeResponse(200, SEND_JSON)
        );

        BulkMessageResponse response = client.sendMessages(
            List.of(new SmsMessage("27812345678", "Hello!")));

        assertEquals(12345678901L, response.getEventId());
        assertEquals(1, response.getMessages());
        assertEquals(1.0, response.getCost());
        assertEquals(499.0, response.getRemainingBalance());
        assertNotNull(response.getErrorReport());
        assertNotNull(response.getCostBreakdown());
        assertEquals(1, response.getCostBreakdown().size());
        assertEquals("Local", response.getCostBreakdown().get(0).getNetwork());
    }

    @Test
    void sendMessages_authFailure_throwsSmsPortalException() {
        StubClient client = new StubClient(
            fakeResponse(401, "Unauthorized"),
                fakeResponse(401, "Unauthorized")
        );

        SmsPortalException ex = assertThrows(SmsPortalException.class, () ->
            client.sendMessages(List.of(new SmsMessage("27812345678", "Hello!"))));

        assertEquals(401, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("Unexpected response"));
    }

    @Test
    void sendMessages_apiError_throwsSmsPortalException() {
        StubClient client = new StubClient(
                fakeResponse(400, "Bad Request")
        );

        SmsPortalException ex = assertThrows(SmsPortalException.class, () ->
            client.sendMessages(List.of(new SmsMessage("27812345678", "Hello!"))));

        assertEquals(400, ex.getHttpStatusCode());
    }

    @Test
    void sendMessages_emptyList_throwsIllegalArgumentException() {
        StubClient client = new StubClient();
        assertThrows(IllegalArgumentException.class, () ->
                client.sendMessages(List.of()));
    }

    @Test
    void sendMessages_nullList_throwsIllegalArgumentException() {
        StubClient client = new StubClient();
        assertThrows(IllegalArgumentException.class, () ->
                client.sendMessages(null));
    }

    @Test
    void constructor_nullClientId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new SmsPortalClient(null, "secret"));
    }

    @Test
    void constructor_blankApiSecret_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new SmsPortalClient("id", "  "));
    }

    @Test
    void getToken_cachedTokenReused() throws Exception {
        StubClient client = new StubClient(
                fakeResponse(200, SEND_JSON),
                fakeResponse(200, SEND_JSON)
        );

        client.sendMessages(List.of(new SmsMessage("27812345678", "Hello!")));
        client.sendMessages(List.of(new SmsMessage("27812345679", "World!")));

        // No auth call: only 2 send requests.
        assertEquals(2, client.getCallCount());
    }

    @Test
    void sendMessages_tokenExpired_retriesOnce() throws Exception {
        // Sequence: 401 on send → success on retry
        StubClient client = new StubClient(
                fakeResponse(401, "Unauthorized"),
                fakeResponse(200, SEND_JSON)
        );

        BulkMessageResponse response = client.sendMessages(
            List.of(new SmsMessage("27812345678", "Hello!")));

        assertEquals(12345678901L, response.getEventId());
        assertEquals(2, client.getCallCount());
    }

    @Test
    void smsMessage_constructors_setFieldsCorrectly() {
        SmsMessage msg1 = new SmsMessage("27812345678", "Hello!");
        assertEquals("27812345678", msg1.getDestination());
        assertEquals("Hello!", msg1.getContent());

        SmsMessage msg2 = new SmsMessage("27812345678", "Hello!", "MyApp");
        assertEquals("27812345678", msg2.getDestination());
        assertEquals("Hello!", msg2.getContent());
    }
}
