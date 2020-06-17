package com.intros.mybatis.plugin.annotation;

import java.lang.annotation.*;

/**
 * @author teddy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Columns {
    Column[] value();
}
