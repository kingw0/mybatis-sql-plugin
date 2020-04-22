package com.intros.mybatis.plugin.utils;

public class StringUtils {
    public static boolean isNotBlank(String cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(String cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
