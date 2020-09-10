package com.intros.mybatis.plugin.mapping;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author teddy
 */
public class MappingInfo {
    /**
     * mapping class
     */
    private Class<?> clazz;

    /**
     *
     */
    private BeanInfo beanInfo;

    /**
     *
     */
    private Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<>();

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
        try {
            this.beanInfo = Introspector.getBeanInfo(clazz);
            Arrays.stream(this.beanInfo.getPropertyDescriptors()).forEach(propertyDescriptor -> propertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor));
        } catch (IntrospectionException e) {
            // not a java bean
        }
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

        for (ColumnInfo columnInfo : columnInfos) {
            if (this.beanInfo != null) {
                columnInfo.propertyDescriptor(propertyDescriptors.get(columnInfo.prop()));
            }
        }

        return this;
    }
}
