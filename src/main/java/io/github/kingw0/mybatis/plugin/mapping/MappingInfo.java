package io.github.kingw0.mybatis.plugin.mapping;

import io.github.kingw0.mybatis.plugin.annotation.Column;
import io.github.kingw0.mybatis.plugin.annotation.Tab;
import org.apache.ibatis.exceptions.ExceptionFactory;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * @author teddy
 */
public class MappingInfo {

    private static final Class<Tab> TABLE_CLASS = Tab.class;

    private static final Class<Column> COLUMN_CLASS = Column.class;

    /**
     * mapping class
     */
    private Class<?> clazz;

    /**
     * table name
     */
    private String table;

    /**
     * column infos
     */
    private List<ColumnInfo> columnInfos;

    public MappingInfo(Class<?> clazz) {
        this.clazz = clazz;

        if (clazz.isAnnotationPresent(TABLE_CLASS)) {
            this.table = clazz.getAnnotation(TABLE_CLASS).name();
        } else {
            throw ExceptionFactory.wrapException("Invalid mapping class without table annotation on it!",
                    new IllegalStateException());
        }

        this.columnInfos = new LinkedList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(COLUMN_CLASS)) {
                Column column = field.getAnnotation(COLUMN_CLASS);

                columnInfos.add(new ColumnInfo().column(column.name()).prop(field.getName()).insert(column.insert()).update(column.update())
                        .test(column.test()));
            }
        }
    }

    public Class<?> clazz() {
        return clazz;
    }

    public String table() {
        return table;
    }

    public List<ColumnInfo> columnInfos() {
        return columnInfos;
    }
}
