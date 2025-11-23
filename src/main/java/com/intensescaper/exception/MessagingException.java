package com.intensescaper.exception;

public class MessagingException extends RuntimeException {
    
    public MessagingException(String message) {
        super(message);
    }
    
    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

