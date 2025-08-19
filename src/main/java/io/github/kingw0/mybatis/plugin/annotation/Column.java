package io.github.kingw0.mybatis.plugin.annotation;

import java.lang.annotation.*;

/**
 * Specific the attributes of a column when generate select, insert, update statement
 *
 * @author teddy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Repeatable(Columns.class)
public @interface Column {
    /**
     * The column name in the table
     *
     * <p>when used in select statement, this value can be null instead using expression as select column</p>
     *
     * @return
     */
    String name() default "";

    /**
     * When automatically generating insert or update statements,
     * specify which parameter of the mapper method to obtain the value of the column
     * <p>
     * When this annotation is written on parameter, the default value is the parameter name.
     *
     * @return
     */
    String parameter() default "";

    /**
     * Parameter's field name corresponding to this column
     *
     * @return
     */
    String prop() default "";

    /**
     * Whether the column is included in the automatically generated insert statement
     *
     * @return
     */
    boolean insert() default true;

    /**
     * Whether the column is included in the automatically generated update statement
     *
     * @return
     */
    boolean update() default true;

    /**
     * Mybatis test expression
     * <p>
     * if test expression is false, then ignore this column when generate statement
     *
     * @return
     */
    String test() default "";

    /**
     * @return
     */
    String expression() default "";

    /**
     * @return
     */
    boolean insertNull() default false;

    /**
     * @return
     */
    boolean updateNull() default false;
}
