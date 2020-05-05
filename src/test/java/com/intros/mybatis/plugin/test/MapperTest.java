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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class MapperTest {
    private SqlSessionFactory sqlSessionFactory;

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
    public void test1() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Map<String, String>> res = session.getMapper(TestMapper.class).testSelect("teddy");
            System.out.println(res);
        }
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
