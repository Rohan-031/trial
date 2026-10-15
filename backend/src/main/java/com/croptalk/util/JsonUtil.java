package com.croptalk.util;

import java.util.Map;

/** Helpers that mimic the JavaScript semantics the Node controllers relied on. */
public final class JsonUtil {
    private JsonUtil() {}

    /** JavaScript falsiness: undefined/null, false, 0, NaN and "" are falsy. */
    public static boolean isFalsy(Object v) {
        if (v == null) return true;
        if (v instanceof Boolean b) return !b;
        if (v instanceof Number n) return n.doubleValue() == 0 || Double.isNaN(n.doubleValue());
        if (v instanceof CharSequence s) return s.length() == 0;
        return false;
    }

    /** Mongoose String casting: primitives are stringified, objects/arrays fail the cast. */
    public static String asString(Object v) {
        if (v == null) return null;
        if (v instanceof String || v instanceof Number || v instanceof Boolean) return v.toString();
        throw new IllegalArgumentException("Cast to string failed for value of type " + v.getClass().getSimpleName());
    }

    /** express.json() leaves req.body as {} when nothing was sent. */
    public static Map<String, Object> orEmpty(Map<String, Object> body) {
        return body == null ? Map.of() : body;
    }
}
