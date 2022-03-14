package cn.intros.mybatis.plugin.generator;

import cn.intros.mybatis.plugin.SqlType;
import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * @author teddy
 * @since 2019/08/23
 */
public interface SqlGeneratorFactory {
    /**
     * @param context
     * @param sqlType
     * @return
     */
    SqlGenerator createGenerator(ProviderContext context, SqlType sqlType);
}
