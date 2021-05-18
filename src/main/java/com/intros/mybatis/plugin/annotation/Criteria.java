package com.intros.mybatis.plugin.annotation;

import com.intros.mybatis.plugin.sql.condition.generator.ConditionGenerator;
import com.intros.mybatis.plugin.sql.condition.generator.Eq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Criteria {
    String column();

    Class<? extends ConditionGenerator> condition() default Eq.class;

    boolean nullable() default true;
}
