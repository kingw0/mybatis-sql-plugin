package io.github.kingw0.mybatis.plugin.annotation;

import io.github.kingw0.mybatis.plugin.generator.DefaultSqlGeneratorFactory;
import io.github.kingw0.mybatis.plugin.generator.SqlGeneratorFactory;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Generator {
    Class<? extends SqlGeneratorFactory> factory() default DefaultSqlGeneratorFactory.class;
}
