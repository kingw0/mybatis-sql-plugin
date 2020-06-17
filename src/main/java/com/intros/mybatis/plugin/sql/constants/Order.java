package com.intros.mybatis.plugin.sql.constants;

public enum Order {
    ASC(" ASC"), DESC(" DESC");
    
    private String order;

    Order(String order) {
        this.order = order;
    }

    public String order() {
        return order;
    }
}
