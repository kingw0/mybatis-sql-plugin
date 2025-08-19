package io.github.kingw0.mybatis.plugin.mapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapping info registry
 *
 * @author teddy
 */
public class MappingInfoRegistry {
    private static final MappingInfoRegistry instance = new MappingInfoRegistry();

    private Map<Class<?>, MappingInfo> mappingInfos = new ConcurrentHashMap<>();

    protected MappingInfoRegistry() {
    }

    public static MappingInfoRegistry getInstance() {
        return instance;
    }

    /**
     * register a class and its super class
     *
     * @param mappingClass
     */
    public MappingInfo register(Class<?> mappingClass) {
        if (!mappingInfos.containsKey(mappingClass)) {
            synchronized (mappingInfos) {
                if (!mappingInfos.containsKey(mappingClass)) {
                    MappingInfo mappingInfo = new MappingInfo(mappingClass);
                    mappingInfos.put(mappingClass, mappingInfo);
                    return mappingInfo;
                } else {
                    return mappingInfos.get(mappingClass);
                }
            }
        } else {
            return mappingInfos.get(mappingClass);
        }
    }

    /**
     * get mapping info of a class
     *
     * @param mappingClass
     * @return
     */
    public MappingInfo mappingInfo(Class<?> mappingClass) {
        return register(mappingClass);
    }

    /**
     * judge if a clazz is a custom class
     *
     * @param clazz
     * @return
     */
    private boolean custom(Class<?> clazz) {
        // jdk inner class's class loader is null
        return clazz.getClassLoader() != null && !clazz.isInterface();
    }
}
