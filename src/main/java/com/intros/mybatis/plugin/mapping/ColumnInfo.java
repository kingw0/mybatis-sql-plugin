package com.intros.mybatis.plugin.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Column info of the class's field
 */
public class ColumnInfo {
    /**
     * column mapping field
     */
    private Field field;

    /**
     *
     */
    private PropertyDescriptor propertyDescriptor;

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
     *
     */
    private boolean nullable;

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

    /**
     * @return
     */
    public PropertyDescriptor propertyDescriptor() {
        return propertyDescriptor;
    }

    /**
     * @param propertyDescriptor
     * @return
     */
    public ColumnInfo propertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
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

    public boolean nullable() {
        return nullable;
    }

    public ColumnInfo nullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public Object getValue(Object target) throws InvocationTargetException, IllegalAccessException {
        if (propertyDescriptor != null) {
            return propertyDescriptor.getReadMethod().invoke(target, null);
        } else {
            field.setAccessible(true);
            return field.get(target);
        }
    }
}
