package com.intros.mybatis.plugin.test;

import com.intros.mybatis.plugin.annotation.Column;
import com.intros.mybatis.plugin.annotation.Tab;
import com.intros.mybatis.plugin.sql.Delete;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.Update;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.sql.expression.Binder;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.intros.mybatis.plugin.sql.Table.table;
import static com.intros.mybatis.plugin.sql.condition.Exists.exists;
import static com.intros.mybatis.plugin.sql.expression.Binder.bind;
import static com.intros.mybatis.plugin.sql.expression.Binder.bindIndices;
import static com.intros.mybatis.plugin.sql.expression.Column.column;
import static com.intros.mybatis.plugin.sql.expression.Expression.bracket;
import static com.intros.mybatis.plugin.sql.expression.Literal.text;

public class SQLTest {
    @Test
    public void test() {
        System.out.println(TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void testBind() {
        Select select = new Select().columns("a", "b", "c").from("test").where(column("a").eq(bind("p")));
        Assert.assertEquals("SELECT a, b, c FROM test WHERE a = #{p}", select.toString());
    }

    @Test
    public void testBindCollection() {
        Select select = new Select().columns("a", "b", "c").from("test").where(column("a").in(Binder.bindIndices("p", 2)));
        Assert.assertEquals("SELECT a, b, c FROM test WHERE a IN (#{p[0]}, #{p[1]})", select.toString());
    }

    @Test
    public void testSelect() {
        Select select = new Select().columns("id_", "name_", "age_").columns(column("grade_").as("g"),
                bracket(column("age_").add(10).add(20)).mul(20)
        ).from(table("t_test").as("a"))
                .where(column("id_").eq(10)
                        .and(column("name_").like("%teddy%"))
                        .and(column("age_").between(10, 20))
                        .and(exists(new Select().columns(text("X")).from("t_test").where(column("name_").eq("teddy"))))
                        .and(column("grade_").in(10, 20, 30, 40))
                );
    }

    @Test
    public void testDelete() {
        Delete delete = new Delete("t_test").where(Condition.bracket(column("id_").eq(15).and(column("name_").like("%teddy%"))).and(column("grade_").lt(5)));
    }

    @Test
    public void testUpdate() {
        Update update = new Update("t_test").set("name_", "teddy").set("age_", 10).where(column("id_").eq(10));
    }

    @Benchmark
    public void selectBenchmark() {
        Select select = new Select().columns("id_", "name_", "age_").columns(
                column("grade_").as("g"),
                bracket(column("age_").add(10).add(20)).mul(20)
        ).from(table("t_test").as("a"))
                .where(column("id_").eq(10)
                                .and(column("name_").like("%teddy%"))
                                .and(column("age_").between(10, 20))
//                        .and(exists(new Select().columns(text("X")).from("t_test").where(column("name_").eq("teddy"))))
                                .and(column("grade_").in(10, 20, 30, 40))
                );
    }

    @Benchmark
    @Test
    public void deleteBenchmark() {
        Delete delete = new Delete("t_test").where(Condition.bracket(column("id_").eq(15).and(column("name_").like("%teddy%"))).and(column("grade_").lt(5)));
    }

    public void runBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                // 导入要测试的类
                .include(SQLTest.class.getSimpleName())
                // 预热
                .warmupIterations(1)
                // 度量1轮
                .measurementIterations(1)
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Tab(name = "t_parent_")
    public static class Parent {
        @Column(name = "id_")
        private long id;
    }

    @Tab(name = "t_child_")
    public static class Child extends Parent {
        @Column(name = "name_")
        private String name;

        @Column(name = "age_")
        private long age;
    }
}
