package com.santa.secret;

import java.util.List;
import java.util.Optional;

public class ListUtils {
    public static <T> Optional<T> safeGet(List<T> list, int index) {
        return Optional.ofNullable(list).filter(l -> index >= 0 && index < l.size()).map(l -> l.get(index));
    }

    public static <T> Optional<T> safeGet(Optional<List<T>> list, int index) {
        return list.filter(l -> index >= 0 && index < l.size()).map(l -> l.get(index));
    }
}
