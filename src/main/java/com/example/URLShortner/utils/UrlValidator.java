package com.example.URLShortner.utils;

import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.List;

@Component
public class UrlValidator {

    // block internal/private network redirects
    private static final List<String> BLOCKLIST = List.of(
        "localhost", "127.0.0.1", "0.0.0.0", "192.168.", "10.", "172."
    );

    public boolean isValid(String url) {
        if (url == null || url.isBlank()) return false;

        try {
            URI uri = URI.create(url);

            // must have http or https scheme
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https")))
                return false;

            // must have a host
            String host = uri.getHost();
            if (host == null || host.isBlank()) return false;

            // block private/internal addresses
            for (String blocked : BLOCKLIST) {
                if (host.contains(blocked)) return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}