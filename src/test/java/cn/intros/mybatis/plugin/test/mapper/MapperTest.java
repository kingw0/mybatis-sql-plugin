package cn.intros.mybatis.plugin.test.mapper;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

@State(Scope.Benchmark)
public class MapperTest {
    protected SqlSessionFactory sqlSessionFactory;

    @Before
    @Setup
    public void setup() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1;database_to_upper=false");
        ds.setUser("sa");
        ds.setPassword("sa");

        initDatabase(ds, "script/mysql/schema.sql", "script/mysql/data.sql");

        Environment environment = new Environment("development", new JdbcTransactionFactory(), ds);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(DomainMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    protected void execute(Consumer<SqlSession> executor) {
        execute(true, executor);
    }

    protected void execute(boolean rollback, Consumer<SqlSession> executor) {
        SqlSession session = sqlSessionFactory.openSession();

        try {
            executor.accept(session);
        } finally {
            if (rollback) {
                session.rollback();
            }
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
        return new File(System.getProperty("user.dir"), file).getAbsolutePath();
    }
}
