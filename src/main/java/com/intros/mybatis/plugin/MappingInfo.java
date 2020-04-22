package com.intros.mybatis.plugin;

import java.util.List;
import java.util.Map;

/**
 * @author teddy
 */
public class MappingInfo {
    private String table;

    private List<Class<?>> classes;

    private Map<Class<?>, List<Pair>> columnInfos;

    public String table() {
        return table;
    }

    public List<Class<?>> classes() {
        return classes;
    }

    public Map<Class<?>, List<Pair>> columnInfos() {
        return columnInfos;
    }

    public MappingInfo table(String table) {
        this.table = table;
        return this;
    }

    public MappingInfo classes(List<Class<?>> classes) {
        this.classes = classes;
        return this;
    }

    public MappingInfo columnInfos(Map<Class<?>, List<Pair>> columnInfos) {
        this.columnInfos = columnInfos;
        return this;
    }

    public static class Pair<L, R> {
        private L left;

        private R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public static <L, R> Pair<L, R> of(L left, R right) {
            return new Pair<>(left, right);
        }

        public L left() {
            return left;
        }

        public R right() {
            return right;
        }
    }
}
