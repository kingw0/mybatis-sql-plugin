package com.intros.mybatis.plugin.mapping;

import java.lang.reflect.Field;

/**
 * Column info of the class's field
 */
public class ColumnInfo {
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
    private String prop;

    /**
     *
     */
    private boolean insert;

    /**
     *
     */
    private boolean update;

    /**
     * @return
     */
    public Field field() {
        return field;
    }

    /**
     * @param field
     * @return
     */
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

    public String prop() {
        return prop;
    }

    public ColumnInfo prop(String prop) {
        this.prop = prop;
        return this;
    }

    public boolean insert() {
        return insert;
    }

    public ColumnInfo insert(boolean insert) {
        this.insert = insert;
        return this;
    }

    public boolean update() {
        return update;
    }

    public ColumnInfo update(boolean update) {
        this.update = update;
        return this;
    }
}
