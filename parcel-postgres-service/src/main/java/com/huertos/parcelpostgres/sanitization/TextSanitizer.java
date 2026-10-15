package com.huertos.parcelpostgres.sanitization;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class TextSanitizer {

    private static final Pattern ZERO_WIDTH = Pattern.compile("[\\u200B\\u200C\\u200D\\uFEFF]");
    private static final Pattern UNSAFE_CONTROLS = Pattern.compile("[\\p{Cntrl}&&[^\\s]]");
    private static final Pattern MALICIOUS = Pattern.compile(
            "<|javascript\\s*:|data\\s*:\\s*text/html|on(?:click|error|load|mouseover)\\s*=",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ALLOWED_TEXT = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s.',\\-]*$");

    private TextSanitizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        String withoutZeroWidth = ZERO_WIDTH.matcher(normalized).replaceAll("");
        String withoutControls = UNSAFE_CONTROLS.matcher(withoutZeroWidth).replaceAll("");
        return withoutControls.replaceAll("\\s+", " ").trim();
    }

    public static boolean hasMaliciousContent(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.indexOf('>') >= 0 || MALICIOUS.matcher(value).find();
    }

    public static boolean isAllowedText(String value) {
        return value != null && ALLOWED_TEXT.matcher(value).matches();
    }
}
