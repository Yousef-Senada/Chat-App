package com.untitled.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonMapper {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public static String toJson(Object obj) {
    if (obj == null) {
      return "null";
    }
    if (obj instanceof String) {
      return "\"" + escapeString((String) obj) + "\"";
    }
    if (obj instanceof Number || obj instanceof Boolean) {
      return obj.toString();
    }
    if (obj instanceof UUID) {
      return "\"" + obj.toString() + "\"";
    }
    if (obj instanceof LocalDateTime) {
      return "\"" + ((LocalDateTime) obj).format(DATE_TIME_FORMATTER) + "\"";
    }
    if (obj instanceof List<?>) {
      return listToJson((List<?>) obj);
    }
    if (obj instanceof Map<?, ?>) {
      return mapToJson((Map<?, ?>) obj);
    }
    if (obj.getClass().isRecord()) {
      return recordToJson(obj);
    }
    return objectToJson(obj);
  }

  private static String escapeString(String s) {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private static String listToJson(List<?> list) {
    StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    for (Object item : list) {
      if (!first)
        sb.append(",");
      sb.append(toJson(item));
      first = false;
    }
    sb.append("]");
    return sb.toString();
  }

  private static String mapToJson(Map<?, ?> map) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (!first)
        sb.append(",");
      sb.append("\"").append(entry.getKey()).append("\":");
      sb.append(toJson(entry.getValue()));
      first = false;
    }
    sb.append("}");
    return sb.toString();
  }

  private static String recordToJson(Object record) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;

    var components = record.getClass().getRecordComponents();
    for (var component : components) {
      try {
        Object value = component.getAccessor().invoke(record);
        if (!first)
          sb.append(",");
        sb.append("\"").append(component.getName()).append("\":");
        sb.append(toJson(value));
        first = false;
      } catch (Exception e) {
      }
    }
    sb.append("}");
    return sb.toString();
  }

  private static String objectToJson(Object obj) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;

    for (var method : obj.getClass().getMethods()) {
      String name = method.getName();
      if (name.startsWith("get") && name.length() > 3 &&
          method.getParameterCount() == 0 &&
          !name.equals("getClass")) {
        try {
          String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
          Object value = method.invoke(obj);
          if (!first)
            sb.append(",");
          sb.append("\"").append(fieldName).append("\":");
          sb.append(toJson(value));
          first = false;
        } catch (Exception e) {
        }
      }
    }
    sb.append("}");
    return sb.toString();
  }

  public static Map<String, Object> parseJson(String json) {
    if (json == null || json.trim().isEmpty()) {
      return new HashMap<>();
    }
    json = json.trim();
    if (!json.startsWith("{")) {
      return new HashMap<>();
    }
    return parseObject(json, new int[] { 0 });
  }

  public static String getString(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? value.toString() : null;
  }

  public static UUID getUUID(Map<String, Object> map, String key) {
    String value = getString(map, key);
    return value != null ? UUID.fromString(value) : null;
  }

  public static boolean getBoolean(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return "true".equalsIgnoreCase(String.valueOf(value));
  }

  public static int getInt(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public static LocalDateTime getDateTime(Map<String, Object> map, String key) {
    String value = getString(map, key);
    if (value == null)
      return null;
    try {
      return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    } catch (Exception e) {
      try {
        return LocalDateTime.parse(value);
      } catch (Exception e2) {
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> getList(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof List<?>) {
      return (List<Map<String, Object>>) value;
    }
    return new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getMap(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Map<?, ?>) {
      return (Map<String, Object>) value;
    }
    return new HashMap<>();
  }

  private static Map<String, Object> parseObject(String json, int[] pos) {
    Map<String, Object> result = new HashMap<>();
    pos[0]++;
    skipWhitespace(json, pos);

    while (pos[0] < json.length() && json.charAt(pos[0]) != '}') {
      skipWhitespace(json, pos);
      if (json.charAt(pos[0]) == '}')
        break;

      String key = parseString(json, pos);
      skipWhitespace(json, pos);

      if (pos[0] < json.length() && json.charAt(pos[0]) == ':') {
        pos[0]++;
      }
      skipWhitespace(json, pos);

      Object value = parseValue(json, pos);
      result.put(key, value);

      skipWhitespace(json, pos);
      if (pos[0] < json.length() && json.charAt(pos[0]) == ',') {
        pos[0]++;
      }
    }

      if (pos[0] < json.length())
        pos[0]++;
    return result;
  }

  private static List<Object> parseArray(String json, int[] pos) {
    List<Object> result = new ArrayList<>();
    pos[0]++;
    skipWhitespace(json, pos);

    while (pos[0] < json.length() && json.charAt(pos[0]) != ']') {
      Object value = parseValue(json, pos);
      result.add(value);

      skipWhitespace(json, pos);
      if (pos[0] < json.length() && json.charAt(pos[0]) == ',') {
        pos[0]++;
      }
      skipWhitespace(json, pos);
    }

    if (pos[0] < json.length())
      pos[0]++;
    return result;
  }

  private static Object parseValue(String json, int[] pos) {
    skipWhitespace(json, pos);
    if (pos[0] >= json.length())
      return null;

    char c = json.charAt(pos[0]);

    if (c == '"') {
      return parseString(json, pos);
    }
    if (c == '{') {
      return parseObject(json, pos);
    }
    if (c == '[') {
      return parseArray(json, pos);
    }
    if (c == 't' || c == 'f') {
      return parseBoolean(json, pos);
    }
    if (c == 'n') {
      return parseNull(json, pos);
    }
    if (c == '-' || Character.isDigit(c)) {
      return parseNumber(json, pos);
    }

    return null;
  }

  private static String parseString(String json, int[] pos) {
    pos[0]++;
    StringBuilder sb = new StringBuilder();

    while (pos[0] < json.length()) {
      char c = json.charAt(pos[0]);
      if (c == '"') {
        pos[0]++;
        break;
      }
      if (c == '\\' && pos[0] + 1 < json.length()) {
        pos[0]++;
        char next = json.charAt(pos[0]);
        switch (next) {
          case 'n':
            sb.append('\n');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 't':
            sb.append('\t');
            break;
          case '"':
            sb.append('"');
            break;
          case '\\':
            sb.append('\\');
            break;
          default:
            sb.append(next);
        }
      } else {
        sb.append(c);
      }
      pos[0]++;
    }

    return sb.toString();
  }

  private static Number parseNumber(String json, int[] pos) {
    int start = pos[0];
    boolean hasDecimal = false;

    while (pos[0] < json.length()) {
      char c = json.charAt(pos[0]);
      if (c == '.')
        hasDecimal = true;
      if (!Character.isDigit(c) && c != '.' && c != '-' && c != '+' && c != 'e' && c != 'E') {
        break;
      }
      pos[0]++;
    }

    String numStr = json.substring(start, pos[0]);
    if (hasDecimal) {
      return Double.parseDouble(numStr);
    }
    return Long.parseLong(numStr);
  }

  private static Boolean parseBoolean(String json, int[] pos) {
    if (json.substring(pos[0]).startsWith("true")) {
      pos[0] += 4;
      return true;
    }
    if (json.substring(pos[0]).startsWith("false")) {
      pos[0] += 5;
      return false;
    }
    return false;
  }

  private static Object parseNull(String json, int[] pos) {
    if (json.substring(pos[0]).startsWith("null")) {
      pos[0] += 4;
    }
    return null;
  }

  private static void skipWhitespace(String json, int[] pos) {
    while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) {
      pos[0]++;
    }
  }
}
