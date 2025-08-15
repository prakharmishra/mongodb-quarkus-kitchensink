package org.mongodb.model;

import java.util.List;

public record CursorPage<T>(
    List<T> data,
    String nextCursor
) {
    public static <T> CursorPage<T> empty() {
        return new CursorPage<T>(List.of(), null);
    }
}