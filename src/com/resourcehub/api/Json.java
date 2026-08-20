package com.resourcehub.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// no external json library, so this class does encode/decode
public final class Json {
    private Json() {
    }

    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            writeString(sb, (String) value);
        } else if (value instanceof Boolean || value instanceof Number) {
            sb.append(value);
        } else if (value instanceof Map) {
            writeObject(sb, (Map<String, ?>) value);
        } else if (value instanceof Collection) {
            writeArray(sb, (Collection<?>) value);
        } else {
            writeString(sb, String.valueOf(value));
        }
    }

    private static void writeObject(StringBuilder sb, Map<String, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeString(sb, entry.getKey());
            sb.append(':');
            write(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void writeArray(StringBuilder sb, Collection<?> items) {
        sb.append('[');
        boolean first = true;
        for (Object item : items) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            write(sb, item);
        }
        sb.append(']');
    }

    private static void writeString(StringBuilder sb, String value) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    public static Map<String, Object> parseObject(String json) {
        Object parsed = new Parser(json).parseValue();
        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("Expected a JSON object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) parsed;
        return map;
    }

    public static String asString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    public static int asInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value).trim());
    }

    public static double asDouble(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(String.valueOf(value).trim());
    }

    private static final class Parser {
        private final String json;
        private int index;

        Parser(String json) {
            this.json = json == null ? "" : json.trim();
        }

        Object parseValue() {
            skipWhitespace();
            if (index >= json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            char c = json.charAt(index);
            if (c == '{') {
                return parseObject();
            }
            if (c == '[') {
                return parseArray();
            }
            if (c == '"') {
                return parseString();
            }
            if (c == 't' || c == 'f') {
                return parseBoolean();
            }
            if (c == 'n') {
                return parseNull();
            }
            if (c == '-' || Character.isDigit(c)) {
                return parseNumber();
            }
            throw new IllegalArgumentException("Unexpected character at position " + index);
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                index++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    break;
                }
                expect(',');
            }
            return map;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                index++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    break;
                }
                expect(',');
            }
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (index < json.length()) {
                char c = json.charAt(index++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (index >= json.length()) {
                        throw new IllegalArgumentException("Unterminated escape");
                    }
                    char esc = json.charAt(index++);
                    switch (esc) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(esc);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            if (index + 4 > json.length()) {
                                throw new IllegalArgumentException("Invalid unicode escape");
                            }
                            String hex = json.substring(index, index + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            index += 4;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid escape: \\" + esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Unterminated string");
        }

        private Object parseNumber() {
            int start = index;
            if (peek('-')) {
                index++;
            }
            while (index < json.length() && Character.isDigit(json.charAt(index))) {
                index++;
            }
            boolean isDouble = false;
            if (peek('.')) {
                isDouble = true;
                index++;
                while (index < json.length() && Character.isDigit(json.charAt(index))) {
                    index++;
                }
            }
            if (peek('e') || peek('E')) {
                isDouble = true;
                index++;
                if (peek('+') || peek('-')) {
                    index++;
                }
                while (index < json.length() && Character.isDigit(json.charAt(index))) {
                    index++;
                }
            }
            String number = json.substring(start, index);
            if (isDouble) {
                return Double.parseDouble(number);
            }
            long asLong = Long.parseLong(number);
            if (asLong >= Integer.MIN_VALUE && asLong <= Integer.MAX_VALUE) {
                return (int) asLong;
            }
            return asLong;
        }

        private Boolean parseBoolean() {
            if (json.startsWith("true", index)) {
                index += 4;
                return Boolean.TRUE;
            }
            if (json.startsWith("false", index)) {
                index += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean at " + index);
        }

        private Object parseNull() {
            if (json.startsWith("null", index)) {
                index += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null at " + index);
        }

        private void skipWhitespace() {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
        }

        private boolean peek(char c) {
            return index < json.length() && json.charAt(index) == c;
        }

        private void expect(char c) {
            skipWhitespace();
            if (index >= json.length() || json.charAt(index) != c) {
                throw new IllegalArgumentException("Expected '" + c + "' at position " + index);
            }
            index++;
        }
    }
}
