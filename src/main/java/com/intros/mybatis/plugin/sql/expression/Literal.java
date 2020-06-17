package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.Sql;

import static com.intros.mybatis.plugin.sql.constants.Keywords.SINGLE_QUOTES;

/**
 * Literal(Constant) expression in sql statement
 *
 * @author teddy
 */
public class Literal<S extends Sql<S>> extends Expression<S> {
    private Object literal;

    private Type type;

    protected Literal(String literal) {
        this.literal = literal;
        this.type = Type.STRING;
    }

    protected Literal(Number literal) {
        this.literal = literal;
        this.type = Type.NUMBER;
    }

    public static Literal number(Number number) {
        return new Literal(number);
    }

    public static Literal text(String text) {
        return new Literal(text);
    }

    @Override
    public S write(S sql) {
        switch (this.type) {
            case STRING:
                sql.append(SINGLE_QUOTES).append((String) literal).append(SINGLE_QUOTES);
                break;
            default:
                sql.append(this.literal.toString());
                break;
        }

        return sql;
    }

    public enum Type {
        NUMBER(0), STRING(1);

        private int type;

        Type(int type) {
            this.type = type;
        }

        public int type() {
            return type;
        }
    }
}
