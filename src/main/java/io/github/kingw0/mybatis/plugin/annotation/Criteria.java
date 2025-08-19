package io.github.kingw0.mybatis.plugin.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Criteria {
    Criterion[] value();
}
