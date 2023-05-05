package cn.intros.mybatis.plugin.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Provider {
    /**
     * @return
     */
    Class<?> clazz();

    String method();
}
