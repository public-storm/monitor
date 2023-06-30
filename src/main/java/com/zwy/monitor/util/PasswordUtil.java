package com.zwy.monitor.util;

import cn.hutool.crypto.SecureUtil;
import com.zwy.monitor.common.Constants;

/**
 * @author zwy
 * @date 2022年04月18日 11:13
 */
public class PasswordUtil {

    private PasswordUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     *  密码加密
     * @author zwy
     * @date 2022/4/18 0018 11:17
     * @param password password（md5值）
     * @return java.lang.String
     */
    public static String generatePassword(String password) {
        int num = 48;
        int pre = 3;
        password = SecureUtil.md5(password + Constants.SALT);
        char[] cs = new char[num];
        for (int i = 0; i < num; i += pre) {
            cs[i] = password.charAt(i / 3 * 2);
            char c = Constants.SALT.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = password.charAt(i / 3 * 2 + 1);
        }
        return new String(cs);
    }
}
