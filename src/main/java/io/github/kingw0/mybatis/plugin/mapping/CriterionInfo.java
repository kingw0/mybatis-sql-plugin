package io.github.kingw0.mybatis.plugin.mapping;

import io.github.kingw0.mybatis.plugin.sql.condition.builder.Builder;

/**
 * @author teddy
 */
public class CriterionInfo {
    private String column;

    private String parameter;

    private String prop;

    private Builder builder;

    private String test;

    private String expression;

    public String column() {
        return column;
    }

    public CriterionInfo column(String column) {
        this.column = column;
        return this;
    }

    public String parameter() {
        return parameter;
    }

    public CriterionInfo parameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    public String prop() {
        return prop;
    }

    public CriterionInfo prop(String prop) {
        this.prop = prop;
        return this;
    }


    public Builder builder() {
        return builder;
    }

    public CriterionInfo builder(Builder builder) {
        this.builder = builder;
        return this;
    }

    public String test() {
        return test;
    }

    public CriterionInfo test(String test) {
        this.test = test;
        return this;
    }

    public String expression() {
        return expression;
    }

    public CriterionInfo expression(String expression) {
        this.expression = expression;
        return this;
    }
}
