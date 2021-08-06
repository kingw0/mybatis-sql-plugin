package com.intros.mybatis.plugin.test.mapper;

import com.intros.mybatis.plugin.ResolvedSqlProvider;
import com.intros.mybatis.plugin.annotation.Criterion;
import com.intros.mybatis.plugin.annotation.Sort;
import com.intros.mybatis.plugin.annotation.Tab;
import com.intros.mybatis.plugin.sql.Select;
import com.intros.mybatis.plugin.sql.Table;
import com.intros.mybatis.plugin.sql.condition.builder.In;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DomainMapper {
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByCriteria(@Criterion(column = "name_") String name);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByCriteriaDynamic(@Criterion(column = "name_",
            test = "#this != null && #this != ''") String name);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByCriteriaExpression(@Criterion(column = "name_", expression = "name_ != #{name}") String name);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByInCriteria(@Criterion(column = "id_", builder = In.class) List<Long> ids);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByMultiCriteria(@Criterion(column = "name_") String name, @Criterion(column = "id_", builder =
            In.class) List<Long> ids);

    @SelectProvider(type = ResolvedSqlProvider.class)
    @Sort(column = "id_", order = "DESC")
    List<Domain> selectOrder(String name);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByProvider(String name);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> select();

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void insert(Domain domain);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void batchInsertList(List<Domain> domains);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void batchInsertArray(Domain... domains);

    @UpdateProvider(type = ResolvedSqlProvider.class)
    void update(@Criterion(column = Domain.COLUMN_ID, prop = "id") Domain domain);

    @UpdateProvider(type = ResolvedSqlProvider.class)
    void batchUpdate(@Criterion(column = Domain.COLUMN_ID, prop = "id") List<Domain> domains);

    @DeleteProvider(type = ResolvedSqlProvider.class)
    @Tab(name = "t_domain")
    void delete(@Criterion(column = "id_") long id);

    /**
     * Provider for TestMapper
     */
    interface Provider {
        default Select selectByProvider(String name) {
            Table DOMAIN_TABLE_A = Table.table(Domain.class).as("a");
            Table DOMAIN_TABLE_B = Table.table(Domain.class).as("b");

            Select select = new Select().columns(DOMAIN_TABLE_A.columns()).from(DOMAIN_TABLE_A).join(DOMAIN_TABLE_B).on(DOMAIN_TABLE_A.column(Domain.COLUMN_ID).eq(DOMAIN_TABLE_B.column(Domain.COLUMN_ID)));
//            return new Select().columns(MappingUtils.columns(Test.class, true)).from("t_domain").where(column("name_").eq(bind("name")));
//            return "select id_ id, name_ name from t_domain where name_ = #{name}";
            System.out.println(select);

            return select;
        }
    }
}
