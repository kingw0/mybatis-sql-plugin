package com.intros.mybatis.plugin.test;

import com.intros.mybatis.plugin.test.mapper.Domain;
import com.intros.mybatis.plugin.test.mapper.DomainMapper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


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
        configuration.addMapper(DomainMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    @Benchmark
    public void testSelectByCriteria() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByCriteria("teddy");
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());
        }
    }

    @Test
    @Benchmark
    public void testSelectByProvider() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByProvider("teddy");
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());
        }
    }

    @Test
    @Benchmark
    public void testSelectByInCriteria() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByInCriteria(Arrays.asList(1L));
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());

            Domain domain = new Domain();
            domain.name("andy");

            session.getMapper(DomainMapper.class).insertByKeyProperty(domain);

            res = session.getMapper(DomainMapper.class).selectByInCriteria(Arrays.asList(1L, 2L));
            Assert.assertEquals(2, res.size());
        }
    }

    @Test
    @Benchmark
    public void testSelectByMultiCriteria() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByMultiCriteria("teddy", Arrays.asList(1L));
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());

            res = session.getMapper(DomainMapper.class).selectByMultiCriteria("teddy", Arrays.asList(2L));
            Assert.assertEquals(0, res.size());

            Domain domain = new Domain();
            domain.name("andy");

            session.getMapper(DomainMapper.class).insertByKeyProperty(domain);

            res = session.getMapper(DomainMapper.class).selectByMultiCriteria("andy", Arrays.asList(1L, 2L));
            Assert.assertEquals(1, res.size());
        }
    }

    @Test
    @Benchmark
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Domain domain = new Domain();
            domain.name("andy");

            session.getMapper(DomainMapper.class).insertByKeyProperty(domain);

            List<Domain> res = session.getMapper(DomainMapper.class).select();

            Assert.assertEquals(2, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());
            Assert.assertEquals(2, res.get(1).id());
            Assert.assertEquals("andy", res.get(1).name());
        }
    }

    @Test
    @Benchmark
    public void testBatchInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Domain> domains = Arrays.asList(new Domain().name("lily"), new Domain().name("andy"));

            session.getMapper(DomainMapper.class).batchInsertListByKeyProperty(domains);

            session.getMapper(DomainMapper.class).batchInsertArrayByKeyProperty(new Domain().name("lily"), new Domain().name("andy"));

            List<Domain> res = session.getMapper(DomainMapper.class).select();

            Assert.assertEquals(5, res.size());
        }
    }

    @Test
    @Benchmark
    public void testUpdate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Domain domain = new Domain().name("fred");
            session.getMapper(DomainMapper.class).insertByKeyProperty(domain);

            domain = new Domain().id(1L).name("andy");
            session.getMapper(DomainMapper.class).updateByKeyProperty(domain);

            List<Domain> res = session.getMapper(DomainMapper.class).select();
            Assert.assertEquals(2, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("andy", res.get(0).name());
            Assert.assertEquals(2, res.get(1).id());
            Assert.assertEquals("fred", res.get(1).name());
        }
    }

    @Test
    @Benchmark
    public void testBatchUpdateByKeyProperty() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Domain domain = new Domain();
            domain.name("fred");
            session.getMapper(DomainMapper.class).insertByKeyProperty(domain);

            List<Domain> domains = Arrays.asList(new Domain().id(1L).name("andy_update"), new Domain().id(2L).name("fred_update"));
            session.getMapper(DomainMapper.class).batchUpdateByKeyProperty(domains);

            List<Domain> res = session.getMapper(DomainMapper.class).select();
            Assert.assertEquals(2, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("andy_update", res.get(0).name());
            Assert.assertEquals(2, res.get(1).id());
            Assert.assertEquals("fred_update", res.get(1).name());
        }
    }

    @Test
    @Benchmark
    public void testDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Domain domain = new Domain();
            domain.id(2L).name("fred");

            session.getMapper(DomainMapper.class).insertByKeyProperty(domain);

            session.getMapper(DomainMapper.class).delete(1L);

            List<Domain> res = session.getMapper(DomainMapper.class).select();

            Assert.assertEquals(1, res.size());
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

    /**
     * 1 bit(sign,always zero) + 14 bits(instance id) + 41 bits(timestamp) + 8 bits(counter)
     */
    public static class IdGenerator {
        /**
         * 2020.06.01 00:00:00
         */
        private static final long BEGIN = 1590940800217L;
        /**
         * 41 bits, 69 years from 2020.06.01 00:00:00
         */
        private static final long TIMESTAMP_MASK = (1L << 42) - 1;

        private static AtomicInteger counter = new AtomicInteger(0);

        public static long id(long instanceId) {
            return 0L + (instanceId << 49) + (((System.currentTimeMillis() - BEGIN) & TIMESTAMP_MASK) << 8) + (counter.getAndAdd(1) & 0xff);
        }
    }
}
