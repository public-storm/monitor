package com.zwy.monitor.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.zwy.monitor.common.MyRuntimeException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author zwy
 * @date 2022年08月05日 11:45
 */
public class DesensitizedUtil {
    private DesensitizedUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String KEY = "2P3EStormTestKey";
    private static final String IV_STRING = "0102030405060708";

    /**
     * 加密
     *
     * @param text 加密内容
     * @return java.lang.String
     * @author zwy
     * @date 2022/8/5 0005 11:51
     */
    public static String encrypt(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            throw new MyRuntimeException("内容不能为空");
        }
        try {
            byte[] raw = KEY.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytesIv = IV_STRING.getBytes(StandardCharsets.UTF_8);
            IvParameterSpec iv = new IvParameterSpec(bytesIv);
            cipher.init(1, secretKeySpec, iv);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            return byte2hex(encrypted).toLowerCase();
        } catch (Exception e) {
            throw new MyRuntimeException("加密异常" + e.getMessage(), e);
        }
    }

    /**
     * 解密
     *
     * @param text 解密内容
     * @return java.lang.String
     * @author zwy
     * @date 2022/8/5 0005 11:51
     */
    public static String decrypt(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            throw new MyRuntimeException("内容不能为空");
        }
        try {
            byte[] raw = KEY.getBytes(StandardCharsets.US_ASCII);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytesIv = IV_STRING.getBytes(StandardCharsets.UTF_8);
            IvParameterSpec iv = new IvParameterSpec(bytesIv);
            cipher.init(2, secretKeySpec, iv);
            byte[] encrypted1 = hex2byte(text);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original);
        } catch (Exception e) {
            throw new MyRuntimeException("解密异常" + e.getMessage(), e);
        }
    }

    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stamp;
        for (byte value : b) {
            stamp = Integer.toHexString(value & 0xFF);
            if (stamp.length() == 1) {
                hs.append("0").append(stamp);
            } else {
                hs.append(stamp);
            }
        }
        return hs.toString().toUpperCase();
    }

    private static byte[] hex2byte(String starches) {
        int l = starches.length();
        if (l % 2 == 1) {
            throw new MyRuntimeException("密文长度错误 len " + l);
        }
        byte[] b = new byte[l / 2];
        for (int i = 0; i != l / 2; i++) {
            b[i] = (byte) Integer.parseInt(starches.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }
}
