package com.intros.mybatis.plugin.test.mapper;

import com.intros.mybatis.plugin.ResolvedSqlProvider;
import com.intros.mybatis.plugin.annotation.Criteria;
import com.intros.mybatis.plugin.annotation.Mapping;
import com.intros.mybatis.plugin.sql.condition.generator.In;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DomainMapper {
    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByCriteria(@Criteria(column = "name_") String name);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByProvider(String name);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByInCriteria(@Criteria(column = "id_", condition = In.class) List<Long> ids);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> selectByMultiCriteria(@Criteria(column = "name_") String name, @Criteria(column = "id_", condition = In.class) List<Long> ids);

    @SelectProvider(type = ResolvedSqlProvider.class)
    List<Domain> select();

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void insertByKeyProperty(Domain domain);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void batchInsertListByKeyProperty(List<Domain> domains);

    @InsertProvider(type = ResolvedSqlProvider.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id_")
    void batchInsertArrayByKeyProperty(Domain... domains);

    @UpdateProvider(type = ResolvedSqlProvider.class)
    @Options(keyProperty = "id")
    void updateByKeyProperty(Domain domain);

    @UpdateProvider(type = ResolvedSqlProvider.class)
    @Options(keyProperty = "id")
    void batchUpdateByKeyProperty(List<Domain> domains);

    @DeleteProvider(type = ResolvedSqlProvider.class)
    @Mapping(Domain.class)
    void delete(@Criteria(column = "id_") long id);

    /**
     * Provider for TestMapper
     */
    interface Provider {
        default String selectByProvider(String name) {
//            return new Select().columns(MappingUtils.columns(Test.class, true)).from("t_domain").where(column("name_").eq(bind("name")));
            return "select id_ id, name_ name from t_domain where name_ = #{name}";
        }
    }
}
