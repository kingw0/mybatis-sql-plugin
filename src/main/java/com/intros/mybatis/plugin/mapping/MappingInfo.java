package com.intros.mybatis.plugin.mapping;

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
}
