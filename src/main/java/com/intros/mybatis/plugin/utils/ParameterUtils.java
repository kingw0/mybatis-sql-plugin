package com.intros.mybatis.plugin.utils;

import org.apache.ibatis.exceptions.ExceptionFactory;

import java.util.Arrays;
import java.util.Collection;

public class ParameterUtils {
    public static <T> Collection<T> collection(Object value) {
        Collection<T> collection;

        if (Collection.class.isAssignableFrom(value.getClass())) {
            collection = (Collection) value;
        } else if (value.getClass().isArray()) {
            collection = Arrays.asList((T[]) value);
        } else {
            throw ExceptionFactory.wrapException(String.format("Param %s is not collection or array.", value),
                    new IllegalStateException());
        }

        return collection;
    }
}
