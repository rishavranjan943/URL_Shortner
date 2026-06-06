package com.example.URLShortner.unit;

import com.example.URLShortner.utils.Base62Encoder;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class Base62EncoderTest {

    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    void generate_shouldReturnSixCharacterCode() {
        assertThat(encoder.generate()).hasSize(6);
    }

    @Test
    void generate_shouldOnlyContainBase62Characters() {
        assertThat(encoder.generate()).matches("[a-zA-Z0-9]+");
    }

    @Test
    void generate_shouldReturnDifferentCodesOnMultipleCalls() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            codes.add(encoder.generate());
        }
        assertThat(codes).hasSize(100);
    }

    @Test
    void generate_shouldNeverReturnNull() {
        assertThat(encoder.generate()).isNotNull();
    }

    @Test
    void generate_shouldNeverReturnBlank() {
        assertThat(encoder.generate()).isNotBlank();
    }
}