package io.github.kingw0.mybatis.plugin.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(Sorts.class)
public @interface Sort {
    String column() default "";

    String expression() default "";

    String order() default "ASC";
}
