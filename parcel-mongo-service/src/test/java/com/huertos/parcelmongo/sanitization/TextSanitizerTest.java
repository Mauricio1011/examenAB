package com.huertos.parcelmongo.sanitization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextSanitizerTest {

    @Test
    void collapsesWhitespaceAndRemovesInvisibleCharacters() {
        assertThat(TextSanitizer.normalize("  Huerto \n  Norte  ")).isEqualTo("Huerto Norte");
        assertThat(TextSanitizer.normalize("Huerto\u200B Norte")).isEqualTo("Huerto Norte");
        assertThat(TextSanitizer.normalize("Huerto\u0000 Norte")).isEqualTo("Huerto Norte");
        assertThat(TextSanitizer.normalize(null)).isNull();
    }

    @Test
    void normalizeIsIdempotent() {
        String once = TextSanitizer.normalize("  Huerto   Norte ");
        assertThat(TextSanitizer.normalize(once)).isEqualTo(once);
    }

    @Test
    void detectsMaliciousContentAndRejectsDisallowedCharacters() {
        assertThat(TextSanitizer.hasMaliciousContent("<script>alert(1)</script>")).isTrue();
        assertThat(TextSanitizer.hasMaliciousContent("javascript:alert(1)")).isTrue();
        assertThat(TextSanitizer.hasMaliciousContent("Huerto Norte")).isFalse();
        assertThat(TextSanitizer.isAllowedText("José María O'Connor")).isTrue();
        assertThat(TextSanitizer.isAllowedText("Huerto Norte, zona 2")).isTrue();
        assertThat(TextSanitizer.isAllowedText("Huerto@Norte")).isFalse();
        assertThat(TextSanitizer.isAllowedText("<b>Huerto</b>")).isFalse();
    }
}
