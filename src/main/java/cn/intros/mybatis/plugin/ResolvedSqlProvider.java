package cn.intros.mybatis.plugin;


import cn.intros.mybatis.plugin.annotation.Generator;
import cn.intros.mybatis.plugin.generator.DefaultSqlGeneratorFactory;
import cn.intros.mybatis.plugin.generator.SqlGenerator;
import cn.intros.mybatis.plugin.generator.DefaultSqlGenerator;
import cn.intros.mybatis.plugin.generator.SqlGeneratorFactory;
import cn.intros.mybatis.plugin.utils.ReflectionUtils;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.scripting.LanguageDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Automatically generate sql statements by mapper method annotations, method parameters and return object types of the mapper method and corresponding annotations
 *
 * @author teddy
 * @since 2019/4/21
 */
public class ResolvedSqlProvider implements ProviderMethodResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolvedSqlProvider.class);

    private static final String SQL_PROVIDER_METHOD = "sqlProviderMethod";

    private static Method resolvedMethod = null;

    private static Map<Method, SqlGenerator> generators = new ConcurrentHashMap<>();

    static {
        try {
            resolvedMethod = ReflectionUtils.getMethod(ResolvedSqlProvider.class, SQL_PROVIDER_METHOD, ProviderContext.class, Object.class);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Error resolve method for ProviderSqlSource!", e);
        }
    }

    /**
     * Provides the providerMethod of the ProviderSqlSource instance associated with the specified mapper method
     *
     * <p>
     * If a specified mapper method has a sql provider annotation,such as {@link SelectProvider}, which type is {@link ResolvedSqlProvider}, mybatis will create an ProviderSqlSource instance for this mapper method.
     * The method pointed to by the providerMethod property of the ProviderSqlSource instance will be called when the mapper method is executed, and the corresponding SQL statement will be returned.
     * {@link org.apache.ibatis.builder.annotation.MapperAnnotationBuilder#getSqlSourceFromAnnotations(Method, Class, LanguageDriver)}
     * {@link org.apache.ibatis.builder.annotation.ProviderSqlSource#createSqlSource(Object)}
     * </p>
     *
     * @param context
     * @return
     */
    @Override
    public Method resolveMethod(ProviderContext context) {
        return resolvedMethod;
    }

    /**
     * the method provide sql statement when mapper method is executed
     *
     * <p>
     * Called by {@link org.apache.ibatis.builder.annotation.ProviderSqlSource#createSqlSource(Object)}
     * </p>
     *
     * @param context
     * @param parameterObject
     * @return
     */
    public String sqlProviderMethod(ProviderContext context, Object parameterObject) throws Throwable {
        Method mapperMethod = context.getMapperMethod();

        SqlType sqlType = SqlType.SELECT;

        if (mapperMethod.isAnnotationPresent(InsertProvider.class)) {
            sqlType = SqlType.INSERT;
        } else if (mapperMethod.isAnnotationPresent(UpdateProvider.class)) {
            sqlType = SqlType.UPDATE;
        } else if (mapperMethod.isAnnotationPresent(DeleteProvider.class)) {
            sqlType = SqlType.DELETE;
        }

        return sql(context, parameterObject, sqlType);
    }

    /**
     * @param context
     * @param parameterObject
     * @param sqlType
     * @return
     */
    private String sql(ProviderContext context, Object parameterObject, SqlType sqlType) throws Throwable {
        Method mapperMethod = context.getMapperMethod();

        SqlGenerator sqlGenerator = generators.get(mapperMethod);

        // build generator for each mapper method
        if (sqlGenerator == null) {
            synchronized (generators) {
                sqlGenerator = generators.get(mapperMethod);

                if (sqlGenerator == null) {
                    try {
                        sqlGenerator = buildGenerator(context, sqlType);
                    } catch (ReflectiveOperationException e) {
                        LOGGER.warn("Failed to get automated sql generator of method [{}] in class [{}], we will use default sql generator!", context.getMapperMethod(), context.getMapperType(), e);
                        sqlGenerator = new DefaultSqlGenerator(context, sqlType);
                    }

                    generators.put(mapperMethod, sqlGenerator);
                }
            }
        }

        return sqlGenerator.generate(context, parameterObject, sqlType);
    }

    /**
     * @param context
     * @param sqlType
     * @return
     */
    private SqlGenerator buildGenerator(ProviderContext context, SqlType sqlType) throws InstantiationException, IllegalAccessException {
        SqlGeneratorFactory factory = getFactory(context.getMapperMethod());
        return factory.createGenerator(context, sqlType);
    }

    /**
     * @param mapperMethod
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private SqlGeneratorFactory getFactory(Method mapperMethod) throws IllegalAccessException, InstantiationException {
        SqlGeneratorFactory factory = null;

        if (mapperMethod.isAnnotationPresent(Generator.class)) {
            Generator generator = mapperMethod.getAnnotation(Generator.class);
            Class<? extends SqlGeneratorFactory> factoryClass = generator.factory();
            factory = factoryClass.newInstance();
        }

        return factory == null ? new DefaultSqlGeneratorFactory() : factory;
    }
}
