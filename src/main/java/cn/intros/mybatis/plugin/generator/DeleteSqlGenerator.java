package cn.intros.mybatis.plugin.generator;

import cn.intros.mybatis.plugin.SqlType;
import cn.intros.mybatis.plugin.sql.Delete;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DeleteSqlGenerator extends DefaultSqlGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSqlGenerator.class);

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public DeleteSqlGenerator(ProviderContext context, SqlType sqlType) {
        super(context, sqlType);
    }

    @Override
    protected String sql(ProviderContext context, Object paramObject) {
        return buildDelete(context, paramObject);
    }

    private String buildDelete(ProviderContext context, Object paramObject) {
        Delete delete = new Delete(this.table);

        Optional<Condition> condition = conditions(criteria, paramObject);

        if (condition.isPresent()) {
            delete.where(condition.get());
        }

        return delete.toString();
    }
}
