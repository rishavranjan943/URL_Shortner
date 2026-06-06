package com.example.URLShortner.unit;

import com.example.URLShortner.utils.RateLimitFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps      = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // pass true — rate limiting ON for all unit tests
        filter = new RateLimitFilter(redisTemplate, true);
    }

    @Test
    void shouldAllowRequest_whenUnderLimit() throws Exception {
        when(valueOps.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldAllowRequest_whenAtExactLimit() throws Exception {
        // 10th request — exactly at limit, should still pass
        when(valueOps.increment(anyString())).thenReturn(10L);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldBlock_whenOverLimit() throws Exception {
        // 11th request — one over the limit of 10
        when(valueOps.increment(anyString())).thenReturn(11L);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        assertThat(response.getContentAsString()).contains("Rate limit exceeded");
    }

    @Test
    void shouldBlock_whenWayOverLimit() throws Exception {
        when(valueOps.increment(anyString())).thenReturn(100L);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void shouldSetExpiry_onFirstRequest() throws Exception {
        when(valueOps.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        verify(redisTemplate).expire(
            argThat(key -> key.startsWith("rate_limit:")),
            eq(60L),
            eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void shouldNotSetExpiry_onSubsequentRequests() throws Exception {
        // 5th request — not first, must NOT reset TTL
        when(valueOps.increment(anyString())).thenReturn(5L);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
    }

    @Test
    void shouldUseIpAddress_asKeyPrefix() throws Exception {
        when(valueOps.increment("rate_limit:192.168.1.50")).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.50");

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(valueOps).increment("rate_limit:192.168.1.50");
    }

    @Test
    void shouldTrackDifferentIps_separately() throws Exception {
        when(valueOps.increment("rate_limit:10.0.0.1")).thenReturn(1L);
        when(valueOps.increment("rate_limit:10.0.0.2")).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setRemoteAddr("10.0.0.1");

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("10.0.0.2");

        filter.doFilterInternal(request1, new MockHttpServletResponse(), new MockFilterChain());
        filter.doFilterInternal(request2, new MockHttpServletResponse(), new MockFilterChain());

        verify(valueOps).increment("rate_limit:10.0.0.1");
        verify(valueOps).increment("rate_limit:10.0.0.2");
    }
}