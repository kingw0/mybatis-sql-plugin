package com.intros.mybatis.plugin;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author teddy
 */
public class MappingInfo {
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

    public Class<?> clazz() {
        return clazz;
    }

    public MappingInfo clazz(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    public String table() {
        return table;
    }

    public MappingInfo table(String table) {
        this.table = table;
        return this;
    }

    public List<ColumnInfo> columnInfos() {
        return columnInfos;
    }

    public MappingInfo columnInfos(List<ColumnInfo> columnInfos) {
        this.columnInfos = columnInfos;
        return this;
    }

    /**
     * Column info of the class's field
     */
    public static class ColumnInfo {
        /**
         * column mapping field
         */
        private Field field;

        /**
         * column name
         */
        private String column;

        /**
         * column alias
         */
        private String alias;

        /**
         *
         */
        private boolean updateKey;

        public Field field() {
            return field;
        }

        public ColumnInfo field(Field field) {
            this.field = field;
            return this;
        }

        public String column() {
            return column;
        }

        public ColumnInfo column(String column) {
            this.column = column;
            return this;
        }

        public String alias() {
            return alias;
        }

        public ColumnInfo alias(String alias) {
            this.alias = alias;
            return this;
        }

        public boolean updateKey() {
            return updateKey;
        }

        public ColumnInfo updateKey(boolean updateKey) {
            this.updateKey = updateKey;
            return this;
        }
    }
}
