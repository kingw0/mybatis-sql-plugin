package com.intros.mybatis.plugin.utils;

import java.util.Collection;
import java.util.List;

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

    public static Object specificValueInParam(Object param, int index) {
        Class<?> type = param.getClass();

        if (List.class.isAssignableFrom(type)) {
            return ((List) param).get(index);
        } else if (type.isArray()) {
            return ((Object[]) param)[index];
        }

        throw new IllegalArgumentException("UnSupport type of paramObj!");
    }
}
