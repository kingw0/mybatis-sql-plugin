package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.constants.Keywords;

import java.util.ArrayList;
import java.util.List;

public class Sorts {
    private List<Sort> sorts;

    public Sorts() {
        sorts = new ArrayList<>();
    }

    public Sorts add(Sort sort) {
        this.sorts.add(sort);
        return this;
    }

    public List<Sort> sorts() {
        return this.sorts;
    }

    public static class Sort {
        public static final String ORDER_DESC = Keywords.KW_DESC;

        public static final String ORDER_ASC = Keywords.KW_ASC;

        private String column;

        private String order;

        public Sort(String column, String order) {
            this.column = column;
            this.order = order;
        }

        public String getColumn() {
            return column;
        }

        public String getOrder() {
            return order;
        }
    }
}
