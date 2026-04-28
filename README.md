# SMS Portal Java Client

Java client library for sending SMS messages through SMSPortal.

## Current Package Structure

- `com.recceda.smsportal.client`
  - `SmsPortalClient`
  - `SmsPortalException`
- `com.recceda.smsportal.model`
  - `SmsMessage`
  - `BulkMessageRequest`
  - `BulkMessageResponse`
  - `AuthResponse`
- `com.recceda.smsportal.example`
  - `SendSmsExample`

## Requirements

- Java 11+
- Maven 3.8+

## Dependencies

Runtime:

- `com.fasterxml.jackson.core:jackson-databind:2.16.1`

Test:

- `org.junit.jupiter:junit-jupiter:5.10.2`
- `org.mockito:mockito-core:5.10.0`
- `org.mockito:mockito-junit-jupiter:5.10.0`

## Authentication and Sending Flow

`SmsPortalClient` currently does the following:

1. Builds a Basic token value from `clientId:apiSecret` (Base64 encoded UTF-8).
2. Uses that value directly in `Authorization: Basic <token>` when sending messages.
3. Sends to:
   - Primary path: `POST https://rest.smsportal.com/BulkMessages`
   - Retry path on HTTP 401: `POST https://rest.smsportal.com/bulkmessages`
4. Retries exactly once after a 401 response.

## Data Model Notes (Current Implementation)

### `SmsMessage`

Serialized properties:

- `destination` (`@JsonProperty("destination")`)
- `content` (`@JsonProperty("content")`)

Constructors:

- `SmsMessage()`
- `SmsMessage(String destination, String content)`
- `SmsMessage(String destination, String content, String sender)`

Current behavior:

- The 3-argument constructor accepts `sender` but does not store it.

### `BulkMessageRequest`

Serialized properties:

- `Messages`

Current behavior:

- Constructor signature is `BulkMessageRequest(List<SmsMessage> messages)`.
- Payload currently includes only `Messages`.

### `BulkMessageResponse`

`BulkMessageResponse` and nested classes are configured with `@JsonIgnoreProperties(ignoreUnknown = true)` where applicable.

Mapped fields:

- `cost`
- `remainingBalance`
- `eventId`
- `sample`
- `messages`
- `parts`
- `costBreakdown`
- `errorReport`

Nested models:

- `CostBreakDown`: `network`, `cost`, `quantity`
- `ErrorReport`: `noNetwork`, `duplicates`, `optedOuts`, `faults`

## Usage

```java
import com.recceda.smsportal.client.SmsPortalClient;
import com.recceda.smsportal.client.SmsPortalException;
import com.recceda.smsportal.model.BulkMessageResponse;
import com.recceda.smsportal.model.SmsMessage;

import java.util.List;

public class Demo {
    public static void main(String[] args) throws SmsPortalException {
        SmsPortalClient client = new SmsPortalClient(
            System.getenv("SMSPORTAL_CLIENT_ID"),
            System.getenv("SMSPORTAL_API_SECRET")
        );

        BulkMessageResponse response = client.sendMessages(
          List.of(new SmsMessage("0727388632", "Hello from Java client"))
        );

        System.out.println("Event ID: " + response.getEventId());
        System.out.println("Messages: " + response.getMessages());
        System.out.println("Cost: " + response.getCost());
    }
}
```

## Build and Test

```bash
mvn clean package
mvn test
```

## Run Example

```bash
export SMSPORTAL_CLIENT_ID=your-client-id
export SMSPORTAL_API_SECRET=your-api-secret
mvn exec:java -Dexec.mainClass="com.recceda.smsportal.example.SendSmsExample"
```

## Notes

- Do not commit real credentials.
- `SmsPortalException` includes HTTP status code when available (`getHttpStatusCode()`).
