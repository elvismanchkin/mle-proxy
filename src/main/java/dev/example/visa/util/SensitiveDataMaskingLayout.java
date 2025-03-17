package dev.example.visa.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A custom layout for Logback that masks sensitive data in log messages.
 * This includes credit card numbers, passwords, sensitive headers, and PII.
 */
@Slf4j
public class SensitiveDataMaskingLayout extends PatternLayout {

    private static final List<Pattern> PATTERNS = new ArrayList<>();

    static {
        // Credit card numbers (with or without spaces/dashes)
        PATTERNS.add(Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b"));

        // JSON fields containing sensitive data
        PATTERNS.add(Pattern.compile("(\"accountNumber\"\\s*:\\s*\")(.*?)(\")", Pattern.CASE_INSENSITIVE));
        PATTERNS.add(Pattern.compile("(\"cardNumber\"\\s*:\\s*\")(.*?)(\")", Pattern.CASE_INSENSITIVE));
        PATTERNS.add(Pattern.compile("(\"password\"\\s*:\\s*\")(.*?)(\")", Pattern.CASE_INSENSITIVE));
        PATTERNS.add(Pattern.compile("(\"token\"\\s*:\\s*\")(.*?)(\")", Pattern.CASE_INSENSITIVE));
        PATTERNS.add(Pattern.compile("(\"key\"\\s*:\\s*\")(.*?)(\")", Pattern.CASE_INSENSITIVE));
        PATTERNS.add(Pattern.compile("(\"secret\"\\s*:\\s*\")(.*?)(\")", Pattern.CASE_INSENSITIVE));

        // Authorization header
        PATTERNS.add(Pattern.compile("(Authorization:\\s*Basic\\s*)([A-Za-z0-9+/=]+)"));
        PATTERNS.add(Pattern.compile("(Authorization:\\s*Bearer\\s*)([A-Za-z0-9._-]+)"));

        // SSN
        PATTERNS.add(Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"));

        // Email addresses
        PATTERNS.add(Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"));
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        // Get the formatted log message from the parent layout
        String message = super.doLayout(event);

        // Apply all patterns and mask sensitive data
        String maskedMessage = message;
        for (Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(maskedMessage);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                if (matcher.groupCount() >= 2) {
                    // For patterns with capture groups (like JSON fields)
                    String replacement = matcher.group(1) + maskValue(matcher.group(2)) + matcher.group(3);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                } else {
                    // For simple patterns (like credit card numbers)
                    matcher.appendReplacement(sb, maskValue(matcher.group()));
                }
            }
            matcher.appendTail(sb);
            maskedMessage = sb.toString();
        }

        return maskedMessage;
    }

    /**
     * Masks a value by showing only the first and last character, with asterisks in between.
     * Very short values are completely masked.
     */
    private String maskValue(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }

        int visibleChars = Math.min(2, value.length() / 4);
        return value.substring(0, visibleChars) +
                "*".repeat(value.length() - (2 * visibleChars)) +
                value.substring(value.length() - visibleChars);
    }
}