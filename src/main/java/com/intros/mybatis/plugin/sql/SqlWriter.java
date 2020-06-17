package com.intros.mybatis.plugin.sql;

/**
 * @author teddy
 */
public interface SqlWriter<S extends Sql<S>> {
    S write(S sql);
}