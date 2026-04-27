package com.recceda.smsportal.client;

/**
 * Checked exception thrown when the SMSPortal API returns an error
 * or when an unexpected HTTP status code is received.
 */
public class SmsPortalException extends Exception {

    private final int httpStatusCode;

    public SmsPortalException(String message) {
        super(message);
        this.httpStatusCode = -1;
    }

    public SmsPortalException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public SmsPortalException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = -1;
    }

    /**
     * Returns the HTTP status code from the API response, or -1 if not applicable.
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
