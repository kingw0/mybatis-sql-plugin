package cn.intros.mybatis.plugin.generator;

import cn.intros.mybatis.plugin.SqlType;
import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * @author teddy
 * @since 2019/08/23
 */
public class DefaultSqlGeneratorFactory implements SqlGeneratorFactory {
    @Override
    public SqlGenerator createGenerator(ProviderContext context, SqlType sqlType) {
        SqlGenerator generator;

        switch (sqlType) {
            case INSERT:
                generator = new InsertSqlGenerator(context, sqlType);
                break;
            case SELECT:
                generator = new SelectSqlGenerator(context, sqlType);
                break;
            case UPDATE:
                generator = new UpdateSqlGenerator(context, sqlType);
                break;
            case DELETE:
                generator = new DeleteSqlGenerator(context, sqlType);
                break;
            default:
                generator = new DefaultSqlGenerator(context, sqlType);
                break;
        }

        return generator;
    }
}
