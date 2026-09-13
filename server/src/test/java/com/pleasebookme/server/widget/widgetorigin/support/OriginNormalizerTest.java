package com.pleasebookme.server.widget.widgetorigin.support;

import com.pleasebookme.server.widget.widgetorigin.exception.InvalidWidgetOriginException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OriginNormalizerTest {

    @ParameterizedTest
    @MethodSource("validOrigins")
    void normalize_returnsBrowserOriginForm(String raw, String expected) {
        assertThat(OriginNormalizer.normalize(raw)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("invalidOrigins")
    void normalize_rejectsInvalidOrigin(String raw) {
        assertThatThrownBy(() -> OriginNormalizer.normalize(raw))
            .isInstanceOf(InvalidWidgetOriginException.class);
    }

    private static Stream<Arguments> validOrigins() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of("   ", null),
            Arguments.of("Barbershop.com/book?x=1", "https://barbershop.com"),
            Arguments.of("HTTPS://EXAMPLE.COM/", "https://example.com"),
            Arguments.of("https://example.com:443/path", "https://example.com"),
            Arguments.of("https://example.com:8443/path", "https://example.com:8443"),
            Arguments.of("http://localhost:3000", "http://localhost:3000"),
            Arguments.of("http://example.com:80", "http://example.com")
        );
    }

    private static Stream<String> invalidOrigins() {
        return Stream.of(
            "ftp://x",
            "https://",
            "https://user@example.com"
        );
    }
}
