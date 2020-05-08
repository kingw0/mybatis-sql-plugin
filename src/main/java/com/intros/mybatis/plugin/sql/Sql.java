package com.intros.mybatis.plugin.sql;

/**
 * Sql class
 *
 * @param <S>
 */
public class Sql<S extends Sql<S>> {
    private StringBuilder buffer = new StringBuilder(128);

    private static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public S append(String part) {
        if (!isEmpty(part)) {
            buffer.append(part);
        }
        return (S) this;
    }

    public S append(int part) {
        buffer.append(part);
        return (S) this;
    }

    public S append(long part) {
        buffer.append(part);
        return (S) this;
    }

    public S append(float part) {
        buffer.append(part);
        return (S) this;
    }

    public S append(double part) {
        buffer.append(part);
        return (S) this;
    }

    public S append(boolean part) {
        buffer.append(part);
        return (S) this;
    }

    public S append(char part) {
        buffer.append(part);
        return (S) this;
    }

    public S append(Object part) {
        buffer.append(part);
        return (S) this;
    }

//    public S foreach(String itemSqlPart<S> part){
//
//    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
