package cn.intros.mybatis.plugin.annotation;

public @interface Lock {

    boolean update() default true;

    boolean share() default false;

    boolean nowait() default false;
}
