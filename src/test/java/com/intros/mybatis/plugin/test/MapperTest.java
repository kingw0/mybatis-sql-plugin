package com.intros.mybatis.plugin.test;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@State(Scope.Benchmark)
public class MapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @Setup
    @Before
    public void setup() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1;database_to_upper=false");
        ds.setUser("sa");
        ds.setPassword("sa");

        initDatabase(ds, "init/schema.sql", "init/data.sql");

        Environment environment = new Environment("development", new JdbcTransactionFactory(), ds);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(TestMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    @Benchmark
    public void testSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<TestMapper.Test> res = session.getMapper(TestMapper.class).queryByName("teddy");
            System.out.println(res);
        }
    }

    @Test
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TestMapper.Test test = new TestMapper.Test();
            test.id(2L).name("andy");

            session.getMapper(TestMapper.class).insert(test);

            List<TestMapper.Test> res = session.getMapper(TestMapper.class).queryAll();
            System.out.println(res);
        }
    }

    @Test
    public void testUpdate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TestMapper.Test test = new TestMapper.Test();
            test.id(2L).name("sirui");

            session.getMapper(TestMapper.class).insert(test);

            test.id(1L).name("andy");

            session.getMapper(TestMapper.class).update(test);

            List<TestMapper.Test> res = session.getMapper(TestMapper.class).queryAll();
            System.out.println(res);
        }
    }

    @Test
    @Benchmark
    public void testDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            long begin = System.currentTimeMillis();

            session.getMapper(TestMapper.class).delete(1L);

            List<TestMapper.Test> res = session.getMapper(TestMapper.class).queryAll();

//            System.out.println(System.currentTimeMillis() - begin);

//            System.out.println(res);
        }
    }

    @Test
    public void runBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                // 导入要测试的类
                .include(MapperTest.class.getSimpleName())
                // 预热
                .warmupIterations(1)
                // 度量1轮
                .measurementIterations(1)
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    private void initDatabase(DataSource ds, String... scripts) throws SQLException {
        try (Connection connection = ds.getConnection(); Statement statement = connection.createStatement()) {
            for (String script : scripts) {
                statement.execute(String.format("runscript from '%s'", path(script)));
            }
        }
    }

    private String path(String file) {
        return this.getClass().getClassLoader().getResource(file).getPath();
    }
}
