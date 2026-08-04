package com.orderflow.worker;

/** Thrown for retryable failures; the message is nacked so Pub/Sub redelivers it. */
public class TransientProcessingException extends Exception {
    public TransientProcessingException(String message) {
        super(message);
    }
}
