package com.intros.mybatis.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MappingInfoRegistry {
    private Map<Class<?>, MappingInfo> mappingInfos = new ConcurrentHashMap<>();

    public MappingInfo register(Class<?> cls) {
        if (!mappingInfos.containsKey(cls)) {
            synchronized (this) {
                if (!mappingInfos.containsKey(cls)) {
                    mappingInfos.put(cls, construct(cls));
                }
            }
        }

        return mappingInfos.get(cls);
    }

    private MappingInfo construct(Class<?> cls) {
        MappingInfo mappingInfo = new MappingInfo();


        return mappingInfo;
    }
}
