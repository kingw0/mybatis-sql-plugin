package com.intros.mybatis.plugin.annotation;

import java.lang.annotation.*;

/**
 * @author teddy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Columns {
    Column[] value();
}
