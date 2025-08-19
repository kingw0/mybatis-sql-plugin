package io.github.kingw0.mybatis.plugin.utils;

import org.apache.ibatis.exceptions.ExceptionFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;

public class ParameterUtils {
    public static Collection collection(Object value) {
        Collection collection;

        if (Collection.class.isAssignableFrom(value.getClass())) {
            collection = (Collection) value;
        } else if (value.getClass().isArray()) {
            collection = new LinkedList<>();
            for (int i = 0, len = Array.getLength(value); i < len; i++) {
                collection.add(Array.get(value, i));
            }
        } else {
            throw ExceptionFactory.wrapException(String.format("Param %s is not collection or array.", value),
                    new IllegalStateException());
        }

        return collection;
    }
}
