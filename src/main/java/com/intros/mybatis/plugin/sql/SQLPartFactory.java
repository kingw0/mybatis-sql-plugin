package com.intros.mybatis.plugin.sql;

/**
 * @param <P>
 * @author teddy
 */
public interface SQLPartFactory<P extends SQLPart> {
    P create(Object... initArgs);
}
