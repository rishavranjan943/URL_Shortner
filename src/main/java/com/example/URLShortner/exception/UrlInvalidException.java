package com.example.URLShortner.exception;

public class UrlInvalidException 
    extends RuntimeException{
        public UrlInvalidException(String message) {
            super(message);
        }
}
