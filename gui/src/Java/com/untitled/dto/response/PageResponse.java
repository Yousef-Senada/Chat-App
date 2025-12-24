package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    int totalPages,
    long totalElements,
    boolean first,
    boolean last) {
  public static <T> PageResponse<T> fromJson(String json, java.util.function.Function<Map<String, Object>, T> mapper) {
    Map<String, Object> map = JsonMapper.parseJson(json);

    List<T> content = new ArrayList<>();
    List<Map<String, Object>> contentList = JsonMapper.getList(map, "content");
    for (Map<String, Object> item : contentList) {
      content.add(mapper.apply(item));
    }

    int pageNumber = JsonMapper.getInt(map, "number");
    int pageSize = JsonMapper.getInt(map, "size");
    int totalPages = JsonMapper.getInt(map, "totalPages");
    long totalElements = JsonMapper.getInt(map, "totalElements");
    boolean first = JsonMapper.getBoolean(map, "first");
    boolean last = JsonMapper.getBoolean(map, "last");

    return new PageResponse<>(content, pageNumber, pageSize, totalPages, totalElements, first, last);
  }

  public boolean hasMore() {
    return !last;
  }
}
