package com.intros.mybatis.plugin.mapping;

import com.intros.mybatis.plugin.annotation.Column;
import com.intros.mybatis.plugin.annotation.Table;
import com.intros.mybatis.plugin.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapping info registry
 *
 * @author teddy
 */
public class MappingInfoRegistry {
    private static final MappingInfoRegistry instance = new MappingInfoRegistry();
    private static final Class<Table> TABLE_CLASS = Table.class;
    private static final Class<Column> COLUMN_CLASS = Column.class;
    private Map<Class<?>, MappingInfo> mappingInfos = new ConcurrentHashMap<>();

    protected MappingInfoRegistry() {
    }

    public static MappingInfoRegistry getInstance() {
        return instance;
    }

    /**
     * register a class and its super class
     *
     * @param mappingClass
     */
    public void register(Class<?> mappingClass) {
        if (!mappingInfos.containsKey(mappingClass)) {
            synchronized (mappingInfos) {
                registerImpl(mappingClass);
            }
        }
    }

    /**
     * register mapping class recursive
     *
     * @param mappingClass
     */
    public void registerImpl(Class<?> mappingClass) {
        if (!mappingInfos.containsKey(mappingClass)) {
            mappingInfos.put(mappingClass, mapping(mappingClass));

            Class<?> superClass = mappingClass.getSuperclass();

            if (custom(superClass)) {
                // register parent class
                registerImpl(superClass);
            }
        }
    }

    /**
     * get mapping info of a class
     *
     * @param mappingClass
     * @return
     */
    public MappingInfo mappingInfo(Class<?> mappingClass) {
        if (!mappingInfos.containsKey(mappingClass)) {
            register(mappingClass);
        }

        return mappingInfos.get(mappingClass);
    }

    /**
     * judge if a clazz is a custom class
     *
     * @param clazz
     * @return
     */
    private boolean custom(Class<?> clazz) {
        // jdk inner class's class loader is null
        return clazz.getClassLoader() != null && !clazz.isInterface();
    }

    /**
     * @param clazz
     * @return
     */
    private MappingInfo mapping(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(TABLE_CLASS)) {
            throw new IllegalArgumentException("Mapping class[" + clazz.getName() + "] must have table annotation.");
        }

        return new MappingInfo().clazz(clazz).table(clazz.getAnnotation(TABLE_CLASS).name())
                .columnInfos(mappingColumns(clazz));

    }

    /**
     * @param mappingClass
     * @return
     */
    private List<ColumnInfo> mappingColumns(Class<?> mappingClass) {
        List<ColumnInfo> columnInfos = new ArrayList<>();

        for (Field field : mappingClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(COLUMN_CLASS)) {
                continue;
            }

            Column column = field.getAnnotation(COLUMN_CLASS);

            columnInfos.add(new ColumnInfo().field(field).column(column.name()).insert(column.insert()).update(column.update())
                    .prop(StringUtils.isBlank(column.alias()) ? field.getName() : column.alias()));
        }

        return columnInfos;
    }
}
