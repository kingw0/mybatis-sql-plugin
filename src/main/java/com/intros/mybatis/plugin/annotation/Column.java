package com.intros.mybatis.plugin.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Repeatable(Columns.class)
public @interface Column {
    String name();

    String alias() default "";
}
