package io.github.kingw0.mybatis.plugin.sql.constants;

/**
 *
 */
public enum Join {
    INNER(" inner join "), LEFT(" left join "), RIGHT(" right join "), FULL(" full join "), CROSS(" cross join ");

    private String join;

    Join(String join) {
        this.join = join;
    }

    public String join() {
        return join;
    }
}
