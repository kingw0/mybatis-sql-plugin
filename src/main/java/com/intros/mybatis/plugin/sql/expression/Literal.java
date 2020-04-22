package com.intros.mybatis.plugin.sql.expression;

import com.intros.mybatis.plugin.sql.SQL;

import static com.intros.mybatis.plugin.sql.constants.Keywords.SINGLE_QUOTES;

/**
 * Literal(Constant) expression in sql statement
 *
 * @author teddy
 */
public class Literal<S extends SQL<S>> extends Expression<S> {
    private static final Class<Literal> THIS_CLASS = Literal.class;

    static {
        registerFactory(THIS_CLASS, (initArgs -> {
            if (initArgs.length == 1) {
                if (initArgs[0] instanceof String) {
                    return new Literal((String) initArgs[0]);
                } else if (initArgs[0] instanceof Number) {
                    return new Literal((Number) initArgs[0]);
                }
            }

            return null;
        }));
    }

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
        return instance(THIS_CLASS, number);
    }

    public static Literal text(String text) {
        return instance(THIS_CLASS, text);
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
