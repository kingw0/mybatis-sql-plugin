package com.intros.mybatis.plugin.utils;

import org.apache.ibatis.exceptions.ExceptionFactory;

import java.util.Arrays;
import java.util.Collection;

public class ParameterUtils {
    /**
     * get param size if param is collection or array
     *
     * @param param
     * @return
     */
    public static int sizeOfParam(Object param) {
        int size = -1;

        Class<?> type = param.getClass();

        if (Collection.class.isAssignableFrom(type)) {
            size = ((Collection) param).size();
        } else if (type.isArray()) {
            size = ((Object[]) param).length;
        }

        return size;
    }

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
