package com.intros.mybatis.plugin.test;

import com.intros.mybatis.plugin.ResolvedSqlProvider;
import com.intros.mybatis.plugin.annotation.Column;
import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.annotation.Mapping;
import com.intros.mybatis.plugin.annotation.Table;
import com.intros.mybatis.plugin.sql.Select;
import org.apache.ibatis.annotations.*;

import java.util.List;

import static com.intros.mybatis.plugin.sql.expression.Bind.bind;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

public interface TestMapper {
    @SelectProvider(type = ResolvedSqlProvider.class)
//    List<Map<String, String>> queryByName(@Criteria(column = "name_") String name);
    List<Test> queryByName(@Criteria(column = "name_") String name);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void insert(Test test);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void batchInsert(List<Test> tests);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void insertAll(Test... tests);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Test> queryAll();

    @UpdateProvider(type = ResolvedSqlProvider.class)
    void update(Test test);

    @DeleteProvider(type = ResolvedSqlProvider.class)
    @Mapping(Test.class)
    void delete(@Criteria(column = "id_") long id);

    interface Provider {
        default Select testSelect(String name) {
            return new Select().columns(column("id_").as("id"), column("name_").as("name")).from("t_test").where(column("name_").eq(bind("name")));
        }
    }

    @Table(name = "t_test")
    class Test {
        @Column(name = "id_", keyProperty = true)
        private long id;

        @Column(name = "name_")
        private String name;

        public long id() {
            return id;
        }

        public Test id(long id) {
            this.id = id;
            return this;
        }

        public String name() {
            return name;
        }

        public Test name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
