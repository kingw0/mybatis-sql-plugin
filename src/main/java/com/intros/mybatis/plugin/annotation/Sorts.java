package com.intros.mybatis.plugin.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Sorts {
    Sort[] value();
}
