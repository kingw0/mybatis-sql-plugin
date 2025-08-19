package io.github.kingw0.mybatis.plugin.sql.expression;

import io.github.kingw0.mybatis.plugin.sql.Sql;
import io.github.kingw0.mybatis.plugin.sql.constants.Keywords;

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
                sql.append(Keywords.SINGLE_QUOTES).append((String) literal).append(Keywords.SINGLE_QUOTES);
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
