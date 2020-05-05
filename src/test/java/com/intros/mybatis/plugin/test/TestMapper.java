package com.intros.mybatis.plugin.test;

import com.intros.mybatis.plugin.ResolvedSqlProvider;
import com.intros.mybatis.plugin.sql.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

import static com.intros.mybatis.plugin.sql.expression.Binder.bind;
import static com.intros.mybatis.plugin.sql.expression.Column.column;

public interface TestMapper {
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Map<String, String>> testSelect(String name);

    interface Provider {
        default Select testSelect(String name) {
            return new Select().columns(column("id_").as("id"), column("name_").as("name")).from("t_test").where(column("name_").eq(bind("name")));
        }
    }
}
