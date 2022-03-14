package cn.intros.mybatis.plugin.generator;

import cn.intros.mybatis.plugin.SqlType;
import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * @author teddy
 * @since 2019/08/23
 */
public interface SqlGenerator {
    /**
     * @param context
     * @param paramObject
     * @param sqlType
     * @return
     */
    String generate(ProviderContext context, Object paramObject, SqlType sqlType) throws Throwable;
}
