package com.recceda.smsportal.client;

import com.recceda.smsportal.client.SmsPortalClient;
import com.recceda.smsportal.client.SmsPortalException;
import com.recceda.smsportal.model.BulkMessageResponse;
import com.recceda.smsportal.model.SmsMessage;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmsPortalClientTest {

    private static final String AUTH_JSON =
            "{\"Token\":\"test-bearer-token\",\"Schema\":\"Bearer\"}";

    private static final String SEND_JSON =
            "{\"Cost\":1,\"RemainingBalance\":499,\"EventId\":12345678901," +
            "\"Sample\":\"Hello!\",\"Messages\":1,\"Parts\":1," +
            "\"CostBreakDown\":[{\"Network\":\"Local\",\"Cost\":1,\"Quantity\":1}]," +
            "\"ErrorReport\":{\"NoNetwork\":0,\"Duplicates\":0,\"OptedOuts\":0,\"Faults\":[]}}";

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
                fakeResponse(200, AUTH_JSON),
                fakeResponse(200, SEND_JSON)
        );

        BulkMessageResponse response = client.sendMessages(
                List.of(new SmsMessage("27812345678", "Hello!")), true);

        assertEquals(12345678901L, response.getEventId());
        assertEquals(1, response.getMessages());
        assertEquals(1.0, response.getCost());
        assertEquals(499.0, response.getRemainingBalance());
        assertNotNull(response.getErrorReport());
        assertNotNull(response.getCostBreakDown());
        assertEquals(1, response.getCostBreakDown().size());
        assertEquals("Local", response.getCostBreakDown().get(0).getNetwork());
    }

    @Test
    void sendMessages_authFailure_throwsSmsPortalException() {
        StubClient client = new StubClient(
                fakeResponse(401, "Unauthorized")
        );

        SmsPortalException ex = assertThrows(SmsPortalException.class, () ->
                client.sendMessages(List.of(new SmsMessage("27812345678", "Hello!")), true));

        assertEquals(401, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("Authentication failed"));
    }

    @Test
    void sendMessages_apiError_throwsSmsPortalException() {
        StubClient client = new StubClient(
                fakeResponse(200, AUTH_JSON),
                fakeResponse(400, "Bad Request")
        );

        SmsPortalException ex = assertThrows(SmsPortalException.class, () ->
                client.sendMessages(List.of(new SmsMessage("27812345678", "Hello!")), true));

        assertEquals(400, ex.getHttpStatusCode());
    }

    @Test
    void sendMessages_emptyList_throwsIllegalArgumentException() {
        StubClient client = new StubClient();
        assertThrows(IllegalArgumentException.class, () ->
                client.sendMessages(List.of(), false));
    }

    @Test
    void sendMessages_nullList_throwsIllegalArgumentException() {
        StubClient client = new StubClient();
        assertThrows(IllegalArgumentException.class, () ->
                client.sendMessages(null, false));
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
                fakeResponse(200, AUTH_JSON),
                fakeResponse(200, SEND_JSON),
                fakeResponse(200, SEND_JSON)
        );

        client.sendMessages(List.of(new SmsMessage("27812345678", "Hello!")), true);
        client.sendMessages(List.of(new SmsMessage("27812345679", "World!")), true);

        // 1 auth + 2 sends = 3 total calls (token cached after first auth)
        assertEquals(3, client.getCallCount());
    }

    @Test
    void sendMessages_tokenExpired_retriesOnce() throws Exception {
        // Sequence: auth → 401 on send → re-auth → success send
        StubClient client = new StubClient(
                fakeResponse(200, AUTH_JSON),
                fakeResponse(401, "Unauthorized"),
                fakeResponse(200, AUTH_JSON),
                fakeResponse(200, SEND_JSON)
        );

        BulkMessageResponse response = client.sendMessages(
                List.of(new SmsMessage("27812345678", "Hello!")), true);

        assertEquals(12345678901L, response.getEventId());
        assertEquals(4, client.getCallCount());
    }

    @Test
    void smsMessage_constructors_setFieldsCorrectly() {
        SmsMessage msg1 = new SmsMessage("27812345678", "Hello!");
        assertEquals("27812345678", msg1.getDestination());
        assertEquals("Hello!", msg1.getContent());
        assertNull(msg1.getSender());

        SmsMessage msg2 = new SmsMessage("27812345678", "Hello!", "MyApp");
        assertEquals("MyApp", msg2.getSender());

        msg2.setSendTime("2026-05-01T10:00:00Z");
        assertEquals("2026-05-01T10:00:00Z", msg2.getSendTime());
    }
}
