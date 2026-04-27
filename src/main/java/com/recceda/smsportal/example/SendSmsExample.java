package com.recceda.smsportal.example;

import java.util.List;

import com.recceda.smsportal.client.SmsPortalClient;
import com.recceda.smsportal.client.SmsPortalException;
import com.recceda.smsportal.model.BulkMessageResponse;
import com.recceda.smsportal.model.SmsMessage;

/**
 * Runnable example showing how to use {@link SmsPortalClient}.
 *
 * <p>Set environment variables before running:
 * <pre>
 * export SMSPORTAL_CLIENT_ID=your-client-id
 * export SMSPORTAL_API_SECRET=your-api-secret
 * mvn exec:java -Dexec.mainClass="com.recceda.smsportal.example.smsportal.SendSmsExample"
 * </pre>
 */
public class SendSmsExample {

    public static void main(String[] args) {
        String clientId = System.getenv("SMSPORTAL_CLIENT_ID");
        String apiSecret = System.getenv("SMSPORTAL_API_SECRET");

        if (clientId == null || apiSecret == null) {
            System.err.println("Please set SMSPORTAL_CLIENT_ID and SMSPORTAL_API_SECRET environment variables.");
            System.exit(1);
        }

        SmsPortalClient client = new SmsPortalClient(clientId, apiSecret);

        List<SmsMessage> messages = List.of(
                new SmsMessage("27812345678", "Hello from SMSPortal Java client!", "MyApp")
        );

        try {
            // Always use testMode = true first to validate without consuming credits
            BulkMessageResponse response = client.sendMessages(messages, true);

            System.out.println("EventId:          " + response.getEventId());
            System.out.println("Messages:         " + response.getMessages());
            System.out.println("Parts:            " + response.getParts());
            System.out.println("Cost:             " + response.getCost());
            System.out.println("Remaining Balance:" + response.getRemainingBalance());
            System.out.println("Sample:           " + response.getSample());

            if (response.getCostBreakDown() != null) {
                response.getCostBreakDown().forEach(cb ->
                        System.out.printf("  Network: %s | Cost: %.2f | Qty: %d%n",
                                cb.getNetwork(), cb.getCost(), cb.getQuantity()));
            }

        } catch (SmsPortalException e) {
            System.err.println("SMSPortal error (HTTP " + e.getHttpStatusCode() + "): " + e.getMessage());
            System.exit(1);
        }
    }
}
