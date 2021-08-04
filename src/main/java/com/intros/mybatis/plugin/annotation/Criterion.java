package com.intros.mybatis.plugin.annotation;

import com.intros.mybatis.plugin.sql.condition.builder.Builder;

import java.lang.annotation.*;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Repeatable(Criteria.class)
public @interface Criterion {
    String column();

    String parameter() default "";

    String prop() default "";

    Class<? extends Builder> builder() default Builder.class;

    String test() default "";

    String expression() default "";
}
