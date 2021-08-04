package com.intros.mybatis.plugin.test.mapper;

import com.intros.mybatis.plugin.sql.Sorts;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.List;

import static com.intros.mybatis.plugin.sql.Sorts.Sort.ORDER_DESC;


public class DomainMapperTest extends MapperTest {

    @Test
    public void testSelectByCriteria() {
        execute(session -> {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByCriteria("teddy");
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());
        });
    }

    @Test
    public void testSelectByCriteriaExpression() {
        execute(session -> {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByCriteriaExpression("teddy");
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(2, res.get(0).id());
            Assert.assertEquals("andy", res.get(0).name());
        });
    }

    @Test
    public void testSelectOrder() {
        execute(session -> {
            List<Domain> res = session.getMapper(DomainMapper.class).selectOrder("teddy",
                    new Sorts().add(new Sorts.Sort("id_", ORDER_DESC)));
            Assert.assertEquals(2, res.size());
            Assert.assertEquals(2, res.get(0).id());
            Assert.assertEquals("andy", res.get(0).name());
        });
    }

    @Test
    public void testSelectByCriteriaDynamic() {
        execute(session -> {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByCriteriaDynamic("");
            Assert.assertEquals(2, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals(2, res.get(1).id());
            Assert.assertEquals("teddy", res.get(0).name());
            Assert.assertEquals("andy", res.get(1).name());
        });
    }


    @Test
    public void testSelectByProvider() {
        execute(session -> {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByProvider("teddy");
            Assert.assertEquals(2, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());
        });
    }

    @Test
    public void testSelectByInCriteria() {
        execute(session -> {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByInCriteria(Arrays.asList(1L));
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());

            Domain domain = new Domain();
            domain.name("andy");

            session.getMapper(DomainMapper.class).insert(domain);

            res = session.getMapper(DomainMapper.class).selectByInCriteria(Arrays.asList(1L, 2L));
            Assert.assertEquals(2, res.size());
        });
    }

    @Test
    public void testSelectByMultiCriteria() {
        execute(session -> {
            List<Domain> res = session.getMapper(DomainMapper.class).selectByMultiCriteria("teddy", Arrays.asList(1L));
            Assert.assertEquals(1, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());

            res = session.getMapper(DomainMapper.class).selectByMultiCriteria("teddy", Arrays.asList(2L));
            Assert.assertEquals(0, res.size());

            Domain domain = new Domain();
            domain.name("andy");

            session.getMapper(DomainMapper.class).insert(domain);

            res = session.getMapper(DomainMapper.class).selectByMultiCriteria("andy", Arrays.asList(1L, 2L));
            Assert.assertEquals(1, res.size());
        });
    }

    @Test
    public void testInsert() {
        execute(session -> {
            Domain domain = new Domain();
            domain.name("handsome");

            session.getMapper(DomainMapper.class).insert(domain);

            List<Domain> res = session.getMapper(DomainMapper.class).select();

            Assert.assertEquals(3, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("teddy", res.get(0).name());
            Assert.assertEquals(2, res.get(1).id());
            Assert.assertEquals("andy", res.get(1).name());
        });
    }

    @Test
    public void testBatchInsert() {
        execute(session -> {
            List<Domain> domains = Arrays.asList(new Domain().name("lily"), new Domain().name("andy"));

            session.getMapper(DomainMapper.class).batchInsertList(domains);

            session.getMapper(DomainMapper.class).batchInsertArray(new Domain().name("lily"), new Domain().name("andy"));

            List<Domain> res = session.getMapper(DomainMapper.class).select();

            Assert.assertEquals(6, res.size());
        });
    }

    @Test
    public void testUpdate() {
        execute(session -> {
            Domain domain = new Domain().name("fred");
            session.getMapper(DomainMapper.class).insert(domain);

            domain = new Domain().id(1L).name("andy");
            session.getMapper(DomainMapper.class).update(domain);

            List<Domain> res = session.getMapper(DomainMapper.class).select();
            Assert.assertEquals(3, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("andy", res.get(0).name());
            Assert.assertEquals(2, res.get(1).id());
            Assert.assertEquals("andy", res.get(1).name());
            Assert.assertEquals(3, res.get(2).id());
            Assert.assertEquals("fred", res.get(2).name());
        });
    }

    @Test
    public void testBatchUpdateByKeyProperty() {
        execute(session -> {
            Domain domain = new Domain();
            domain.name("fred");
            session.getMapper(DomainMapper.class).insert(domain);

            List<Domain> domains = Arrays.asList(new Domain().id(1L).name("andy_update"), new Domain().id(2L).name("fred_update"));
            session.getMapper(DomainMapper.class).batchUpdate(domains);

            List<Domain> res = session.getMapper(DomainMapper.class).select();
            Assert.assertEquals(3, res.size());
            Assert.assertEquals(1, res.get(0).id());
            Assert.assertEquals("andy_update", res.get(0).name());
            Assert.assertEquals(2, res.get(1).id());
            Assert.assertEquals("fred_update", res.get(1).name());
        });
    }

    @Test
    public void testDelete() {
        execute(session -> {
            Domain domain = new Domain();
            domain.name("fred");

            session.getMapper(DomainMapper.class).insert(domain);
            session.getMapper(DomainMapper.class).delete(1L);

            List<Domain> res = session.getMapper(DomainMapper.class).select();

            Assert.assertEquals(2, res.size());
        });
    }

    public void runBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                // 导入要测试的类
                .include(DomainMapperTest.class.getSimpleName())
                // 预热
                .warmupIterations(1)
                // 度量1轮
                .measurementIterations(1)
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
