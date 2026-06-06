package com.example.URLShortner.utils;



import java.io.IOException;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;;


@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS = 10;      // per window
    private static final int WINDOW_SECONDS = 60;
    private final boolean rateLimitEnabled;          // ← add field



    public RateLimitFilter(StringRedisTemplate redisTemplate,@Value("${rate.limit.enabled:true}") boolean rateLimitEnabled) {
        this.redisTemplate = redisTemplate;
        this.rateLimitEnabled=rateLimitEnabled;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!rateLimitEnabled) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        String key = "rate_limit:" + ip;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // first request in window — set expiry
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        if (count > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
            response.getWriter().write("Rate limit exceeded. Try again in 60 seconds.");
            return;
        }

        chain.doFilter(request, response);
    }
}