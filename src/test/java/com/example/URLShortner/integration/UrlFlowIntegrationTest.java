package com.example.URLShortner.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class UrlFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // disable auto-redirect following so we can assert on 302 directly
        restTemplate.getRestTemplate().setRequestFactory(
            new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection,
                                                 String httpMethod) throws IOException {
                    super.prepareConnection(connection, httpMethod);
                    connection.setInstanceFollowRedirects(false);
                }
            }
        );
    }

    // ── Test 1: shorten a URL ───────────────────────────────
    @Test
    void shorten_shouldReturnShortUrl() {
        Map<String, String> body = Map.of("longUrl", "https://google.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/shorten", body, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("shortUrl");
        assertThat((String) response.getBody().get("shortUrl")).isNotBlank();
    }

    // ── Test 2: redirect returns 302 with Location header ──
    @Test
    void redirect_shouldReturn302_withLocationHeader() {
        Map<String, String> body = Map.of("longUrl", "https://example.com");

        Map shortenResponse = restTemplate
            .postForEntity("/shorten", body, Map.class)
            .getBody();

        String shortCode = (String) shortenResponse.get("shortUrl");

        ResponseEntity<Void> redirectResponse = restTemplate
            .getForEntity("/" + shortCode, Void.class);

        assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(redirectResponse.getHeaders().getLocation())
            .hasToString("https://example.com");
    }

    // ── Test 3: stats after redirects ──────────────────────
    @Test
    void stats_shouldShowIncrementedClickCount() {
        Map<String, String> body = Map.of("longUrl", "https://github.com");

        Map shortenResponse = restTemplate
            .postForEntity("/shorten", body, Map.class)
            .getBody();

        String shortCode = (String) shortenResponse.get("shortUrl");

        restTemplate.getForEntity("/" + shortCode, Void.class);
        restTemplate.getForEntity("/" + shortCode, Void.class);

        ResponseEntity<Map> statsResponse = restTemplate
            .getForEntity("/stats/" + shortCode, Map.class);

        assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Integer) statsResponse.getBody().get("clickCount"))
            .isEqualTo(2);
    }

    // ── Test 4: unknown code returns 404 ───────────────────
    @Test
    void redirect_shouldReturn404_forUnknownCode() {
        ResponseEntity<Map> response = restTemplate
            .getForEntity("/xyz_does_not_exist_abc", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── Test 5: duplicate long URL returns same short code ─
    @Test
    void shorten_shouldReturnSameCode_forDuplicateLongUrl() {
        Map<String, String> body = Map.of("longUrl", "https://duplicate-test.com");

        Map first  = restTemplate.postForEntity("/shorten", body, Map.class).getBody();
        Map second = restTemplate.postForEntity("/shorten", body, Map.class).getBody();

        assertThat(first.get("shortUrl")).isEqualTo(second.get("shortUrl"));
    }

    // ── Test 6: invalid URL returns 400 ────────────────────
    @Test
    void shorten_shouldReturn400_forInvalidUrl() {
        Map<String, String> body = Map.of("longUrl", "not-a-valid-url");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/shorten", body, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Test 7: unknown stats code returns 404 ─────────────
    @Test
    void stats_shouldReturn404_forUnknownCode() {
        ResponseEntity<Map> response = restTemplate
            .getForEntity("/stats/expired_code_xyz", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}