package com.intros.mybatis.plugin.annotation;

import com.intros.mybatis.plugin.generator.DefaultSqlGeneratorFactory;
import com.intros.mybatis.plugin.generator.SqlGeneratorFactory;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Generator {
    Class<? extends SqlGeneratorFactory> factory() default DefaultSqlGeneratorFactory.class;
}
