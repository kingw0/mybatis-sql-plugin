package com.intros.mybatis.plugin.generator;

import com.intros.mybatis.plugin.SqlType;
import com.intros.mybatis.plugin.annotation.*;
import com.intros.mybatis.plugin.mapping.ColumnInfo;
import com.intros.mybatis.plugin.mapping.CriterionInfo;
import com.intros.mybatis.plugin.sql.Sql;
import com.intros.mybatis.plugin.sql.condition.Condition;
import com.intros.mybatis.plugin.utils.ReflectionUtils;
import com.intros.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intros.mybatis.plugin.sql.constants.Keywords.SPACE;

/**
 * Default Sql Generator, one generator instance for one mapper method
 *
 * <p>
 *
 * </p>
 *
 * @author teddy
 * @since 2019/08/23
 */
public class DefaultSqlGenerator implements SqlGenerator {
    public static final String GENERIC_NAME_PREFIX = "param";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSqlGenerator.class);

    protected boolean hasProvider = true;
    protected boolean hasParamAnnotation;
    protected boolean multiQuery = false;
    protected String table;
    protected int mappingParamIndex = -1;
    protected Parameter[] parameters;
    protected String[] paramNames;
    protected Map<String, ColumnInfo> columns = new LinkedHashMap<>();
    protected Map<String, CriterionInfo> criteria = new LinkedHashMap<>();
    private Class<?> providerClass;
    private Method providerMethod;
    private MethodHandle methodHandle;
    private int expressionColumnSeq = 0;
    private int expressionCriterionSeq = 0;

    /**
     * Constructor for generator
     *
     * @param context
     * @param sqlType
     */
    public DefaultSqlGenerator(ProviderContext context, SqlType sqlType) {
        processMapperParameter(context.getMapperMethod());

        findSqlProvider(context);

        if (providerMethod == null || methodHandle == null) {
            hasProvider = false;
            process(context.getMapperMethod(), sqlType);
        }
    }

    /**
     * Generate sql statement for the specific mapper method
     *
     * @param context
     * @param paramObject
     * @param sqlType
     * @return
     * @throws Throwable
     */
    @Override
    public String generate(ProviderContext context, Object paramObject, SqlType sqlType) throws Throwable {
        if (providerMethod != null && methodHandle != null) {
            // if we can find the default provider method, use the default method to generate sql
            return generateSqlFromProvider(paramObject);
        } else {
            // generate sql from mapper method automatically
            return sql(context, paramObject);
        }
    }

    /**
     * override by sub class
     *
     * @param context
     * @param paramObject
     * @return
     */
    protected String sql(ProviderContext context, Object paramObject) throws Exception {
        return null;
    }

    /**
     * Extract param value from mybatis wrapped parameter
     *
     * <p>
     * {@link  org.apache.ibatis.reflection.ParamNameResolver#getNamedParams}
     * </p>
     *
     * @param param
     * @param paramName
     * @return
     */
    protected Object paramValue(Object param, String paramName) {
        if (this.parameters.length == 0) {
            return null;
        } else if (param instanceof Map) {
            // if has param annotation,mybatis will put the param in a map
            return ((Map) param).get(paramName);
        } else if (this.parameters.length == 1 && !this.hasParamAnnotation) {
            return param;
        }

        return null;
    }

    protected Condition condition(CriterionInfo criterionInfo, Object root) {
        return condition(criterionInfo, 0, -1, root);
    }

    protected Condition condition(CriterionInfo criterionInfo, int size, int index, Object root) {
        if (!test(criterionInfo.test(), root)) {
            return null;
        }

        if (StringUtils.isNotBlank(criterionInfo.expression())) {
            return new Condition() {
                @Override
                public Sql write(Sql sql) {
                    return sql.append(SPACE).append(criterionInfo.expression()).append(SPACE);
                }
            };
        }

        return size > 0 && index > -1 ? criterionInfo.builder().build(criterionInfo, size, index, root) :
                criterionInfo.builder().build(criterionInfo, root);
    }

    protected boolean test(String test, Object root) {
        if (StringUtils.isNotBlank(test)) {
            Object result = OgnlCache.getValue(test, root);

            return result != null && (!(result instanceof Boolean) || (Boolean) result);
        }

        return true;
    }

    private void processMapperParameter(Method mapperMethod) {
        this.parameters = mapperMethod.getParameters();

        this.paramNames = new String[parameters.length];

        int index = 0;

        for (Parameter parameter : this.parameters) {
            if (!hasParamAnnotation && parameter.isAnnotationPresent(Param.class)) {
                this.hasParamAnnotation = true;
            }

            this.paramNames[index] = paramName(parameter, index);

            index++;
        }
    }

    private void process(Method mapperMethod, SqlType sqlType) {
        Class<?> mappingClass = findMappingClass(mapperMethod, sqlType, this.parameters);

        if (sqlType == SqlType.DELETE) {
            if (mapperMethod.isAnnotationPresent(Tab.class)) {
                this.table = mapperMethod.getAnnotation(Tab.class).name();
            } else {
                throw new IllegalStateException("Can't find table name for mapper method " + mapperMethod + " when " +
                        "generate sql automatically!");
            }
        } else {
            if (mapperMethod.isAnnotationPresent(Tab.class)) {
                this.table = mapperMethod.getAnnotation(Tab.class).name();
            } else {
                if (mappingClass != null && mappingClass.isAnnotationPresent(Tab.class)) {
                    this.table = mappingClass.getAnnotation(Tab.class).name();
                } else {
                    throw new IllegalStateException("Can't find table name for mapper method " + mapperMethod + " when " +
                            "generate sql automatically!");
                }
            }
        }

        if (sqlType != SqlType.DELETE) {
            columns(mapperMethod, mappingClass, this.paramNames, this.parameters);
        }

        if (sqlType != SqlType.INSERT) {
            criteria(mapperMethod, this.paramNames, this.parameters);
        }
    }

    private void columns(Method mapperMethod, Class<?> mappingClass, String[] paramNames, Parameter[] parameters) {
        // Get column info from mapping class
        if (mappingClass != null && !Map.class.isAssignableFrom(mappingClass)) {
            for (Field field : mappingClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    this.columns.put(column.name(),
                            columnInfo(this.mappingParamIndex > -1 ? paramNames[this.mappingParamIndex] : null, field.getName(), column));
                }
            }
        }

        int index = 0;

        // Get column info from parameter annotation
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Columns.class)) {
                for (Column column : parameter.getAnnotation(Columns.class).value()) {
                    this.columns.put(columnKey(column), columnInfo(paramNames[index], column.prop(), column));
                }
            } else if (parameter.isAnnotationPresent(Column.class)) {
                Column column = parameter.getAnnotation(Column.class);
                this.columns.put(columnKey(column), columnInfo(paramNames[index], column.prop(), column));
            }
            index++;
        }

        // Get column info from mapper method annotation
        if (mapperMethod.isAnnotationPresent(Columns.class)) {
            for (Column column : mapperMethod.getAnnotation(Columns.class).value()) {
                this.columns.put(columnKey(column), columnInfo(column.parameter(), column.prop(), column));
            }
        } else if (mapperMethod.isAnnotationPresent(Column.class)) {
            Column column = mapperMethod.getAnnotation(Column.class);
            this.columns.put(columnKey(column), columnInfo(column.parameter(), column.prop(), column));
        }
    }

    private String columnKey(Column column) {
        return StringUtils.isBlank(column.name()) ? "column" + (++expressionColumnSeq) : column.name();
    }

    /**
     * @param paramName
     * @param column
     * @return
     */
    private ColumnInfo columnInfo(String paramName, String prop, Column column) {
        return new ColumnInfo().column(column.name()).parameter(paramName).prop(prop).insert(column.insert()).update(column.update())
                .test(column.test()).expression(column.expression());
    }

    /**
     * @param mapperMethod
     * @param paramNames
     * @param parameters
     */
    private void criteria(Method mapperMethod, String[] paramNames, Parameter[] parameters) {
        Parameter parameter;
        String paramName;

        for (int index = 0, len = paramNames.length; index < len; index++) {
            parameter = parameters[index];
            paramName = paramNames[index];

            if (parameter.isAnnotationPresent(Criteria.class)) {
                for (Criterion criterion : parameter.getAnnotation(Criteria.class).value()) {
                    this.criteria.put(criterionKey(criterion), criterionInfo(paramName, criterion));
                }
            } else if (parameter.isAnnotationPresent(Criterion.class)) {
                Criterion criterion = parameter.getAnnotation(Criterion.class);
                this.criteria.put(criterionKey(criterion), criterionInfo(paramName, criterion));
            }
        }

        if (mapperMethod.isAnnotationPresent(Criteria.class)) {
            for (Criterion criterion : mapperMethod.getAnnotation(Criteria.class).value()) {
                if (!this.criteria.containsKey(criterion.column())) {
                    this.criteria.put(criterionKey(criterion), criterionInfo(criterion.parameter(), criterion));
                }
            }
        } else if (mapperMethod.isAnnotationPresent(Criterion.class)) {
            Criterion criterion = mapperMethod.getAnnotation(Criterion.class);
            this.criteria.put(criterionKey(criterion), criterionInfo(criterion.parameter(), criterion));
        }
    }

    private String criterionKey(Criterion criterion) {
        return StringUtils.isBlank(criterion.column()) ? "criterion" + (++expressionCriterionSeq) : criterion.column();
    }

    /**
     * @param paramName
     * @param criterion
     * @return
     */
    private CriterionInfo criterionInfo(String paramName, Criterion criterion) {
        try {
            return new CriterionInfo().column(criterion.column()).expression(criterion.expression()).test(criterion.test()).parameter(paramName).prop(criterion.prop()).builder(criterion.builder().getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            throw ExceptionFactory.wrapException("Failed to create condition from criterion " + criterion + "!", e);
        }
    }

    /**
     * @param mapperMethod
     * @param sqlType
     * @return
     */
    private Class<?> findMappingClass(Method mapperMethod, SqlType sqlType, Parameter[] parameters) {
        Class<?> mappingClass = null;
        if (sqlType == SqlType.SELECT) {
            mappingClass = actualType(mapperMethod.getGenericReturnType());
        } else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE) {
            int index = -1;

            for (Parameter parameter : parameters) {
                Class<?> parameterType = actualType(parameter.getParameterizedType());

                if (parameterType.isAnnotationPresent(Tab.class) || Map.class.isAssignableFrom(parameterType)) {
                    this.mappingParamIndex = ++index;
                    mappingClass = parameterType;
                    break;
                }
            }

            if (this.mappingParamIndex > -1) {
                Class<?> mappingParamType = parameters[this.mappingParamIndex].getType();
                this.multiQuery = Collection.class.isAssignableFrom(mappingParamType) || mappingParamType.isArray();
            }
        }

        return mappingClass;
    }

    /**
     * @param type
     * @return
     */
    private Class<?> actualType(Type type) {
        if (type instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType())) {
            return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        } else if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            return clazz.isArray() ? clazz.getComponentType() : clazz;
        }

        return null;
    }

    /**
     * Get parameter's name by mybatis rule
     *
     * <p>
     * Mybatis will wrap parameter by parameter's name before execute statement.
     * You can find how mybatis get parameter in {@link org.apache.ibatis.reflection.ParamNameResolver} and
     * org.apache.ibatis.session.defaults.DefaultSqlSession#wrapCollection(Object)
     * </p>
     * TODO: 2021/8/4 skip special parameters (i.e. {@link org.apache.ibatis.session.RowBounds} or {@link org.apache.ibatis.session.ResultHandler}) ?
     *
     * @param parameter
     * @return
     */
    private String paramName(Parameter parameter, int index) {
        String paramName;

        if (parameter.isAnnotationPresent(Param.class)) {
            paramName = parameter.getAnnotation(Param.class).value();
        } else {
            paramName = parameter.getName();
        }

        if (this.parameters.length == 1 && !this.hasParamAnnotation) {
            Class<?> type = this.parameters[0].getType();

            if (List.class.isAssignableFrom(type)) {
                paramName = "list";
            } else if (Collection.class.isAssignableFrom(type)) {
                paramName = "collection";
            } else if (type.isArray()) {
                paramName = "array";
            } else if (!type.isPrimitive() && !CharSequence.class.isAssignableFrom(type)) {
                paramName = null;
            }
        } else if (paramName == null) {
            return GENERIC_NAME_PREFIX + index;
        }

        return paramName;
    }

    /**
     * Get provider which provide sql statement for mapper method
     *
     * @param context
     * @return
     */
    private void findSqlProvider(ProviderContext context) {
        Class<?> mapperType = context.getMapperType();

        Method mapperMethod = context.getMapperMethod();

        // Find if there has provider class in mapper. eg., TestMapper$Provider
        providerClass = findClass(mapperType.getName() + "$Provider");

        if (providerClass == null) {
            // Find if there has provider class which name is mapper class name + Provider. eg., TestMapperProvider
            providerClass = findClass(mapperType.getName() + "Provider");
        }

        if (providerClass == null) {
            providerClass = mapperMethod.isAnnotationPresent(Provider.class) ? mapperMethod.getAnnotation(Provider.class).clazz() : null;
        }

        if (providerClass != null) {
            try {
                providerMethod = providerClass.getMethod(mapperMethod.getName(), mapperMethod.getParameterTypes());

                if (providerClass.isInterface() && providerMethod.isDefault()) {
                    Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{providerClass}, (p, m, args) -> null);
                    methodHandle = ReflectionUtils.getDefaultMethodHandle(proxy, providerMethod);
                } else if (Modifier.isStatic(providerMethod.getModifiers())) {
                    methodHandle = ReflectionUtils.getStaticMethodHandle(providerClass, providerMethod);
                }
            } catch (ReflectiveOperationException e) {
                // ignore
//                LOGGER.warn("Failed to get provider method!", e);
            }
        }
    }

    /**
     * @param className
     * @return
     */
    private Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Generate sql from provider for mapper method
     *
     * @param paramObject
     * @return
     * @throws Throwable
     */
    private String generateSqlFromProvider(Object paramObject) throws Throwable {
        Object result;

        Object[] args = convertSqlParamsToArgs(paramObject);

        if (providerClass.isInterface()) {
            result = MethodHandles.spreadInvoker(methodHandle.type(), 0).invokeExact(methodHandle, args);
        } else {
            result = methodHandle.invokeWithArguments(args);
        }

        return result == null ? null : result.toString();
    }

    /**
     * Convert sql param to mapper method args
     *
     * <p>
     * Mybatis will use {@link org.apache.ibatis.reflection.ParamNameResolver#getNamedParams(Object[])} to convert mapper method to sql command param.
     * We use this method to convert sql command param back to args
     * </p>
     *
     * @param param
     * @return
     */
    private Object[] convertSqlParamsToArgs(Object param) {
        int len = this.parameters.length;

        Object[] args = len == 0 || param == null ? null : new Object[len];

        if (param instanceof Map) {
            for (int index = 0; index < len; index++) {
                args[index] = ((Map) param).get(this.paramNames[index]);
            }
        } else if (len == 1 && !this.hasParamAnnotation) {
            args[0] = param;
        }

        return args;
    }
}
