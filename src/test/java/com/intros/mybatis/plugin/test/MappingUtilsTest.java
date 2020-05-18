package com.intros.mybatis.plugin.test;

import com.intros.mybatis.plugin.sql.constants.BindType;
import com.intros.mybatis.plugin.utils.MappingUtils;
import org.junit.Assert;
import org.junit.Test;

public class MappingUtilsTest {
    @Test
    public void testColumns() {
        Assert.assertEquals("test.id_ id, test.name_ name", MappingUtils.columns(TestMapper.Test.class, "test", null, true));
    }

    @Test
    public void testBind() {
        Assert.assertEquals("#{a.id}, #{a.name}", MappingUtils.bindExpr(TestMapper.Test.class, null, null, BindType.BIND));
    }
}
