package com.intros.mybatis.plugin.sql;

/**
 * @param <P>
 * @author teddy
 */
public interface SqlPartFactory<P extends SqlPart> {
    P create(Object... initArgs);
}
