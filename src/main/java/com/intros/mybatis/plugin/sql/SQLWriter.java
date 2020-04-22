package com.intros.mybatis.plugin.sql;

import com.intros.mybatis.plugin.sql.SQL;

/**
 * @author teddy
 */
public interface SQLWriter<S extends SQL<S>> {
    S write(S sql);
}