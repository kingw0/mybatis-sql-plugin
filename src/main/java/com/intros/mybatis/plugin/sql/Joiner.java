package com.intros.mybatis.plugin.sql;

import java.util.Iterator;
import java.util.List;

public class Joiner {
    /**
     * @param sql
     * @param separator
     * @param writers
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S join(S sql, String separator, List<? extends SqlWriter<S>> writers) {
        return join(sql, separator, null, null, writers);
    }

    /**
     * @param sql
     * @param separator
     * @param writers
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S join(S sql, String separator, SqlWriter<S>... writers) {
        return join(sql, separator, null, null, writers);
    }

    /**
     * @param sql
     * @param separator
     * @param open
     * @param close
     * @param writers
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S join(S sql, String separator, String open, String close, List<? extends SqlWriter<S>> writers) {
        Iterator<? extends SqlWriter<S>> iter = writers.iterator();

        if (iter.hasNext()) {
            iter.next().write(sql.append(open));

            while (iter.hasNext()) {
                iter.next().write(sql.append(separator));
            }
        }

        return sql.append(close);
    }

    /**
     * @param sql
     * @param separator
     * @param open
     * @param close
     * @param writers
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S join(S sql, String separator, String open, String close, SqlWriter<S>... writers) {
        checkArg(writers);

        writers[0].write(sql.append(open));

        if (writers.length > 1) {
            for (int i = 1, len = writers.length; i < len; i++) {
                writers[i].write(sql.append(separator));
            }
        }

        return sql.append(close);
    }

    /**
     * @param sql
     * @param separator
     * @param parts
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S join(S sql, String separator, String... parts) {
        return join(sql, separator, null, null, parts);
    }

    /**
     * @param sql
     * @param separator
     * @param parts
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S joins(S sql, String separator, List<String> parts) {
        return joins(sql, separator, null, null, parts);
    }

    /**
     * @param sql
     * @param separator
     * @param open
     * @param close
     * @param parts
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S joins(S sql, String separator, String open, String close, List<String> parts) {
        Iterator<String> iter = parts.iterator();

        if (iter.hasNext()) {
            sql.append(open).append(iter.next());

            while (iter.hasNext()) {
                sql.append(separator).append(iter.next());
            }
        }

        return sql.append(close);
    }

    /**
     * @param sql
     * @param separator
     * @param parts
     * @param <S>
     * @return
     */
    public static <S extends Sql<S>> S join(S sql, String separator, String open, String close, String... parts) {
        checkArg(parts);

        sql.append(open).append(parts[0]);

        if (parts.length > 1) {
            for (int i = 1, len = parts.length; i < len; i++) {
                sql.append(separator).append(parts[i]);
            }
        }

        return sql.append(close);
    }

    public static <S extends Sql<S>> String join(List<S> sqls, String seperator) {
        StringBuilder buffer = new StringBuilder(256 * sqls.size());

        buffer.append(sqls.get(0));

        for (int i = 1, size = sqls.size(); i < size; i++) {
            buffer.append(seperator).append(sqls.get(i));
        }

        return buffer.toString();
    }

    /**
     * @param args
     */
    public static void checkArg(Object... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Statement appender should not by empty!");
        }
    }
}
