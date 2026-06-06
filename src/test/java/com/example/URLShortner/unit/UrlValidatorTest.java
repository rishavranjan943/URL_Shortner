package com.example.URLShortner.unit;

import com.example.URLShortner.utils.UrlValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator();

    @Test
    void isValid_shouldPassForHttpsUrl() {
        assertThat(validator.isValid("https://google.com")).isTrue();
    }

    @Test
    void isValid_shouldPassForHttpUrl() {
        assertThat(validator.isValid("http://example.com/path?q=1")).isTrue();
    }

    @Test
    void isValid_shouldPassForUrlWithSubdomain() {
        assertThat(validator.isValid("https://blog.example.com")).isTrue();
    }

    @Test
    void isValid_shouldFailForNull() {
        assertThat(validator.isValid(null)).isFalse();
    }

    @Test
    void isValid_shouldFailForBlankString() {
        assertThat(validator.isValid("   ")).isFalse();
    }

    @Test
    void isValid_shouldFailForMissingProtocol() {
        assertThat(validator.isValid("google.com")).isFalse();
    }

    @Test
    void isValid_shouldFailForJavascriptProtocol() {
        assertThat(validator.isValid("javascript:alert('xss')")).isFalse();
    }

    @Test
    void isValid_shouldFailForLocalhost() {
        assertThat(validator.isValid("http://localhost:8080")).isFalse();
    }

    @Test
    void isValid_shouldFailForPrivateIp() {
        assertThat(validator.isValid("http://192.168.1.1/admin")).isFalse();
    }

    @Test
    void isValid_shouldFailForFtpProtocol() {
        assertThat(validator.isValid("ftp://files.example.com")).isFalse();
    }
}