package com.intros.mybatis.plugin.sql;

public class Joiner {
    /**
     * @param sql
     * @param separator
     * @param writers
     * @param <S>
     * @return
     */
    public static <S extends SQL<S>> S join(S sql, String separator, SQLWriter<S>... writers) {
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
    public static <S extends SQL<S>> S join(S sql, String separator, String open, String close, SQLWriter<S>... writers) {
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
    public static <S extends SQL<S>> S join(S sql, String separator, String... parts) {
        return join(sql, separator, null, null, parts);
    }

    /**
     * @param sql
     * @param separator
     * @param parts
     * @param <S>
     * @return
     */
    public static <S extends SQL<S>> S join(S sql, String separator, String open, String close, String... parts) {
        checkArg(parts);

        sql.append(open).append(parts[0]);

        if (parts.length > 1) {
            for (int i = 1, len = parts.length; i < len; i++) {
                sql.append(separator).append(parts[i]);
            }
        }

        return sql.append(close);
    }

    public static void checkArg(Object... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Statement appender should not by empty!");
        }
    }


}
