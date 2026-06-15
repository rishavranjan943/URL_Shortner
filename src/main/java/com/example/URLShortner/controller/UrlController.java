package com.example.URLShortner.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.URLShortner.dto.RequestUrl;
import com.example.URLShortner.dto.ResponseUrl;
import com.example.URLShortner.dto.UrlStatsResponse;
import com.example.URLShortner.services.UrlService;

import jakarta.validation.Valid;


@RestController
public class UrlController {
    private final UrlService urlService;
    
    public UrlController(UrlService urlService)
    {
        this.urlService=urlService;
    }

    @PostMapping("/shorten")
    public ResponseUrl shorten(@Valid @RequestBody RequestUrl request)
    {
        String code=urlService.shortenUrl(request);
        return new ResponseUrl(code);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode)
    {
        String longUrl=urlService.getLongUrl(shortCode);
        urlService.incrementClickCount(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(longUrl))
                            .build();
    }

    @GetMapping("/stats/{code}")
    public UrlStatsResponse getStats(@PathVariable String code) {
        return urlService.getStats(code);
    }
}
