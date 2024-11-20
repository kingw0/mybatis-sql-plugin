package cn.intros.mybatis.plugin.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Lock {

    boolean update() default true;

    boolean share() default false;

    boolean nowait() default false;
}
