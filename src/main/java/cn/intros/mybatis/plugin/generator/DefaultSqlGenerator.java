package cn.intros.mybatis.plugin.generator;

import cn.intros.mybatis.plugin.SqlType;
import cn.intros.mybatis.plugin.annotation.*;
import cn.intros.mybatis.plugin.mapping.ColumnInfo;
import cn.intros.mybatis.plugin.mapping.CriterionInfo;
import cn.intros.mybatis.plugin.sql.Sql;
import cn.intros.mybatis.plugin.sql.condition.Condition;
import cn.intros.mybatis.plugin.sql.constants.Keywords;
import cn.intros.mybatis.plugin.utils.ReflectionUtils;
import cn.intros.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;

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
    protected String alias;
    protected int mappingParamIndex = -1;
    protected Parameter[] parameters;
    protected String[] paramNames;
    protected Map<String, ColumnInfo> columns = new LinkedHashMap<>();
    protected Collection<CriterionInfo> criteria = new LinkedList<>();
    //    protected Map<String, CriterionInfo> criteria = new LinkedHashMap<>();
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
        LOGGER.debug("Begin to generate [{}] sql for method[{}] of class[{}].Param is [{}].",
                     sqlType,
                     context.getMapperMethod(),
                     context.getMapperType(), paramObject);

        String sql = null;

        if (providerMethod != null && methodHandle != null) {
            // if we can find the default provider method, use the default method to generate sql
            LOGGER.debug("Generate sql for method[{}] of class[{}] from provider [{}].", context.getMapperMethod(),
                         context.getMapperType(), providerMethod);
            sql = generateSqlFromProvider(paramObject);
        } else {
            // generate sql from mapper method automatically
            sql = sql(context, paramObject);
        }

        LOGGER.debug("Generate sql statement[{}] for method[{}] of class[{}].Params is [{}]!", sql,
                     context.getMapperMethod(), context.getMapperType(), paramObject);

        return sql;
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

    protected Optional<Condition> conditions(Collection<CriterionInfo> criterionInfos, Object element, int size,
                                             int index) {
        return criterionInfos.stream().map(criterionInfo -> condition(criterionInfo, size, index, element)).filter(Objects::nonNull)
            .reduce((c1, c2) -> c1.and(c2));
    }

    protected Optional<Condition> conditions(Collection<CriterionInfo> criterionInfos, Object paramObject) {
        return criterionInfos.stream().map(criterionInfo -> condition(criterionInfo, paramObject)).filter(Objects::nonNull)
            .reduce((c1, c2) -> c1.and(c2));
    }

    protected Condition condition(CriterionInfo criterionInfo, Object paramObject) {
        return condition(criterionInfo, 0, -1, paramObject);
    }

    protected Condition condition(CriterionInfo criterionInfo, int size, int index, Object paramObject) {
        if (!test(criterionInfo.test(), paramObject)) {
            return null;
        }

        if (StringUtils.isNotBlank(criterionInfo.expression())) {
            return new Condition() {
                @Override
                public Sql write(Sql sql) {
                    return sql.append(Keywords.SPACE).append(criterionInfo.expression()).append(Keywords.SPACE);
                }
            };
        }

        Object paramValue = StringUtils.isNotBlank(criterionInfo.parameter()) ? paramValue(paramObject,
                                                                                           criterionInfo.parameter()) : null;

        return size > 0 && index > -1 ? criterionInfo.builder().build(criterionInfo, size, index, paramObject,
                                                                      paramValue) :
               criterionInfo.builder().build(criterionInfo, paramObject, paramValue);
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

        Tab tab = null;

        if (sqlType == SqlType.DELETE) {
            if (mapperMethod.isAnnotationPresent(Tab.class)) {
                tab = mapperMethod.getAnnotation(Tab.class);
            } else {
                throw new IllegalStateException(
                    "Can't find table name for mapper method " + mapperMethod + " when " + "generate sql automatically!");
            }
        } else {
            if (mapperMethod.isAnnotationPresent(Tab.class)) {
                tab = mapperMethod.getAnnotation(Tab.class);
            } else {
                if (mappingClass != null && mappingClass.isAnnotationPresent(Tab.class)) {
                    tab = mappingClass.getAnnotation(Tab.class);
                } else {
                    throw new IllegalStateException(
                        "Can't find table name for mapper method " + mapperMethod + " " + "when " + "generate sql automatically!");
                }
            }
        }

        if (tab != null) {
            this.table = tab.name();
            this.alias = tab.alias();
        }

        if (sqlType != SqlType.DELETE) {
            columns(mapperMethod, mappingClass, this.paramNames, this.parameters);
        }

        if (sqlType != SqlType.INSERT) {
            criteria(mapperMethod, this.paramNames, this.parameters);
        }
    }

    private void columns(Method mapperMethod, Class<?> mappingClass, String[] paramNames, Parameter[] parameters) {
        Map<String, ColumnInfo> columns = new HashMap<>();

        if (mappingClass != null && !Map.class.isAssignableFrom(mappingClass)) {
            // Get column info from mapping class
            for (Field field : mappingClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    columns.put(column.name(),
                                columnInfo(this.mappingParamIndex > -1 ? paramNames[this.mappingParamIndex] : null, field.getName(),
                                           column));
                }
            }
        }

        // Get column info from mapper method annotation
        if (mapperMethod.isAnnotationPresent(Columns.class)) {
            for (Column column : mapperMethod.getAnnotation(Columns.class).value()) {
                String prop =
                    StringUtils.isBlank(column.prop()) ? (columns.containsKey(column.name()) ? columns.get(column.name()).prop() : "") :
                    column.prop();
                this.columns.put(columnKey(column), columnInfo(column.parameter(), prop, column));
            }
        } else if (mapperMethod.isAnnotationPresent(Column.class)) {
            Column column = mapperMethod.getAnnotation(Column.class);
            String prop =
                StringUtils.isBlank(column.prop()) ? (columns.containsKey(column.name()) ? columns.get(column.name()).prop() : "") :
                column.prop();
            this.columns.put(columnKey(column), columnInfo(column.parameter(), prop, column));
        } else {
            this.columns.putAll(columns);
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
            .test(column.test()).expression(column.expression()).insertNull(column.insertNull()).updateNull(column.updateNull());
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
                    this.criteria.add(criterionInfo(paramName, criterion));
                }
            } else if (parameter.isAnnotationPresent(Criterion.class)) {
                this.criteria.add(criterionInfo(paramName, parameter.getAnnotation(Criterion.class)));
            }
        }

        if (mapperMethod.isAnnotationPresent(Criteria.class)) {
            for (Criterion criterion : mapperMethod.getAnnotation(Criteria.class).value()) {
                this.criteria.add(criterionInfo(criterion.parameter(), criterion));
            }
        } else if (mapperMethod.isAnnotationPresent(Criterion.class)) {
            Criterion criterion = mapperMethod.getAnnotation(Criterion.class);
            this.criteria.add(criterionInfo(criterion.parameter(), criterion));
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
            return new CriterionInfo().column(criterion.column()).expression(criterion.expression()).test(criterion.test())
                .parameter(paramName).prop(criterion.prop()).builder(criterion.builder().getDeclaredConstructor().newInstance());
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

                if (parameterType == null) {
                    continue;
                }

                if (Map.class.isAssignableFrom(parameterType) || parameterType.isAnnotationPresent(Tab.class)) {
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
            // List<Map<>>
            Type t = ((ParameterizedType) type).getActualTypeArguments()[0];
            return t instanceof ParameterizedType ?
                   (Class<?>) ((ParameterizedType) t).getRawType() : (Class<?>) t;
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
     * TODO: 2021/8/4 skip special parameters (i.e. {@link org.apache.ibatis.session.RowBounds} or
     * {@link org.apache.ibatis.session.ResultHandler}) ?
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
            providerClass = mapperMethod.isAnnotationPresent(Provider.class) ?
                            mapperMethod.getAnnotation(Provider.class).clazz() : null;
        }

        if (providerClass != null) {
            try {
                providerMethod = providerClass.getMethod(mapperMethod.getName(), mapperMethod.getParameterTypes());

                if (providerClass.isInterface() && providerMethod.isDefault()) {
                    Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                                          new Class<?>[]{providerClass}, (p, m, args) -> null);
                    methodHandle = ReflectionUtils.getDefaultMethodHandle(proxy, providerMethod);
                } else if (Modifier.isStatic(providerMethod.getModifiers())) {
                    methodHandle = ReflectionUtils.getStaticMethodHandle(providerClass, providerMethod);
                }
            } catch (ReflectiveOperationException e) {
                // ignore
                LOGGER.debug("No provider method for " + mapperMethod + "!");
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
            LOGGER.debug("Class {} not found!", className);
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
     * Mybatis will use {@link org.apache.ibatis.reflection.ParamNameResolver#getNamedParams(Object[])} to convert
     * mapper method to sql command param.
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
