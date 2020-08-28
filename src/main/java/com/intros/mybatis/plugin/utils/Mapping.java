package com.intros.mybatis.plugin.utils;

import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.MappingInfoRegistry;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Mapping {
    private static final MappingInfoRegistry registry = MappingInfoRegistry.getInstance();

    /**
     * @param mappingClass
     * @param filter
     * @param consumer
     */
    public static void consume(Class<?> mappingClass, Predicate<ColumnInfo> filter, Consumer<ColumnInfo> consumer) {
        Stream<ColumnInfo> stream = registry.mappingInfo(mappingClass).columnInfos().stream();

        if (filter != null) {
            stream = stream.filter(filter);
        }

        stream.forEach(consumer);
    }
}
