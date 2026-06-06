package com.example.URLShortner.services;

import com.example.URLShortner.dto.RequestUrl;
import com.example.URLShortner.dto.UrlStatsResponse;

public interface UrlService {
    String shortenUrl(RequestUrl request);
    String getLongUrl(String shortCode);
    UrlStatsResponse getStats(String code);
    void deleteUrl(String shortCode);
    void incrementClickCount(String shortCode);
}
