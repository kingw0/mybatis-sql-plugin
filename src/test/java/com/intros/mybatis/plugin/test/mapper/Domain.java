package com.intros.mybatis.plugin.test.mapper;

import com.intros.mybatis.plugin.annotation.Column;
import com.intros.mybatis.plugin.annotation.Tab;

@Tab(name = "t_domain")
public class Domain {
    public static final String COLUMN_ID = "id_";

    @Column(name = "id_", update = false)
    private long id;

    @Column(name = "name_")
    private String name;

    public long id() {
        return id;
    }

    public Domain id(long id) {
        this.id = id;
        return this;
    }

    public String name() {
        return name;
    }

    public Domain name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
