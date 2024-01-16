package cn.intros.mybatis.plugin.mapping;

/**
 * Column info of the class's field
 */
public class ColumnInfo {
    /**
     * column name
     */
    private String column;

    /**
     *
     */
    private String parameter;

    /**
     *
     */
    private String prop;


    /**
     *
     */
    private boolean insert;

    /**
     *
     */
    private boolean update;

    /**
     *
     */
    private String test;

    /**
     *
     */
    private String expression;

    private boolean insertNull;

    private boolean updateNull;

    public String column() {
        return column;
    }

    public ColumnInfo column(String column) {
        this.column = column;
        return this;
    }

    public String parameter() {
        return parameter;
    }

    public ColumnInfo parameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    public String prop() {
        return prop;
    }

    public ColumnInfo prop(String prop) {
        this.prop = prop;
        return this;
    }

    public boolean insert() {
        return insert;
    }

    public ColumnInfo insert(boolean insert) {
        this.insert = insert;
        return this;
    }

    public boolean update() {
        return update;
    }

    public ColumnInfo update(boolean update) {
        this.update = update;
        return this;
    }

    public String test() {
        return this.test;
    }

    public ColumnInfo test(String test) {
        this.test = test;
        return this;
    }

    public String expression() {
        return expression;
    }

    public ColumnInfo expression(String expression) {
        this.expression = expression;
        return this;
    }

    public boolean insertNull() {
        return insertNull;
    }

    public ColumnInfo insertNull(boolean insertNull) {
        this.insertNull = insertNull;
        return this;
    }

    public boolean updateNull() {
        return updateNull;
    }

    public ColumnInfo updateNull(boolean updateNull) {
        this.updateNull = updateNull;
        return this;
    }
}
