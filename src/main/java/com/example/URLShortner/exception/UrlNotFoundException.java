package com.example.URLShortner.exception;

public class UrlNotFoundException
        extends RuntimeException {

    public UrlNotFoundException(String message) {
        super(message);
    }
}