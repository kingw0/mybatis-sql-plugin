package cn.intros.mybatis.plugin.test;

import cn.intros.mybatis.plugin.annotation.Tab;
import cn.intros.mybatis.plugin.sql.Delete;
import cn.intros.mybatis.plugin.sql.Select;
import cn.intros.mybatis.plugin.sql.Table;
import cn.intros.mybatis.plugin.sql.Update;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.condition.Exists;
import cn.intros.mybatis.plugin.sql.expression.Binder;
import cn.intros.mybatis.plugin.sql.expression.Column;
import cn.intros.mybatis.plugin.sql.expression.Expression;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static cn.intros.mybatis.plugin.sql.expression.Binder.bind;
import static cn.intros.mybatis.plugin.sql.expression.Literal.text;

public class SQLTest {
    @Test
    public void test() {
        System.out.println(TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void testBind() {
        Select select = new Select().columns("a", "b", "c").from("test").where(Column.column("a").eq(bind("p")));
        Assert.assertEquals("SELECT a, b, c FROM test WHERE a = #{p}", select.toString());
    }

    @Test
    public void testBindCollection() {
        Select select = new Select().columns("a", "b", "c").from("test").where(Column.column("a").in(Binder.bindIndices("p", 2)));
        Assert.assertEquals("SELECT a, b, c FROM test WHERE a IN (#{p[0]}, #{p[1]})", select.toString());
    }

    @Test
    public void testSelect() {
        Select select = new Select().columns("id_", "name_", "age_").columns(Column.column("grade_").as("g"),
                                                                             Expression.bracket(Column.column("age_").add(10).add(20))
                                                                                 .mul(20)
            ).from(Table.table("t_test").as("a"))
            .where(Column.column("id_").eq(10)
                       .and(Column.column("name_").like("%teddy%"))
                       .and(Column.column("age_").between(10, 20))
                       .and(Exists.exists(new Select().columns(text("X")).from("t_test").where(Column.column("name_").eq("teddy"))))
                       .and(Column.column("grade_").in(10, 20, 30, 40))
            );
    }

    @Test
    public void testDelete() {
        Delete delete = new Delete("t_test").where(
            Condition.bracket(Column.column("id_").eq(15).and(Column.column("name_").like("%teddy%"))).and(Column.column("grade_").lt(5)));
    }

    @Test
    public void testUpdate() {
        Update update = new Update("t_test").set("name_", "teddy").set("age_", 10).where(Column.column("id_").eq(10));
    }

    @Benchmark
    public void selectBenchmark() {
        Select select = new Select().columns("id_", "name_", "age_").columns(
                Column.column("grade_").as("g"),
                Expression.bracket(Column.column("age_").add(10).add(20)).mul(20)
            ).from(Table.table("t_test").as("a"))
            .where(Column.column("id_").eq(10)
                       .and(Column.column("name_").like("%teddy%"))
                       .and(Column.column("age_").between(10, 20))
//                        .and(exists(new Select().columns(text("X")).from("t_test").where(column("name_").eq("teddy"))))
                       .and(Column.column("grade_").in(10, 20, 30, 40))
            );
    }

    @Benchmark
    @Test
    public void deleteBenchmark() {
        Delete delete = new Delete("t_test").where(
            Condition.bracket(Column.column("id_").eq(15).and(Column.column("name_").like("%teddy%"))).and(Column.column("grade_").lt(5)));
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
        @cn.intros.mybatis.plugin.annotation.Column(name = "id_")
        private long id;
    }

    @Tab(name = "t_child_")
    public static class Child extends Parent {
        @cn.intros.mybatis.plugin.annotation.Column(name = "name_")
        private String name;

        @cn.intros.mybatis.plugin.annotation.Column(name = "age_")
        private long age;

        public Child() {
        }

        public Child(String name, long age) {
            this.name = name;
            this.age = age;
        }

        public String name() {
            return name;
        }

        public long age() {
            return age;
        }
    }
}
