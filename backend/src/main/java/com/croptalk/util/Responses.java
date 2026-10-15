package com.croptalk.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the small JSON bodies the Express controllers returned. */
public final class Responses {
    private Responses() {}

    public static Map<String, Object> message(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("message", message);
        return m;
    }

    public static Map<String, Object> error(String error) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", error);
        return m;
    }

    /** { message, error: error.message } */
    public static Map<String, Object> messageWithError(String message, String errorMessage) {
        Map<String, Object> m = message(message);
        m.put("error", errorMessage);
        return m;
    }

    /** { message, error } where error was an Error object (res.json serialises it as {}). */
    public static Map<String, Object> messageWithEmptyError(String message) {
        Map<String, Object> m = message(message);
        m.put("error", new LinkedHashMap<>());
        return m;
    }
}
