package com.example.buddyfinder_backend.util;

public final class SanitizeUtil {

    private SanitizeUtil() {}

    public static String sanitize(String input) {
        if (input == null) return null;
        String withoutTags = input.replaceAll("<[^>]*>", "");
        return withoutTags.replaceAll("\\s+", " ").trim();
    }
}
