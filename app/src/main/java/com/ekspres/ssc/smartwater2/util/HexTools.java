package com.ekspres.ssc.smartwater2.util;

import java.util.Locale;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public class HexTools {
    /**
     * bytes转换成十六进制字符串
     *
     * @param  bytes 数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String getHexString(byte[] bytes) {
        if(bytes == null)
            return null;

        String stmp = "";
        StringBuilder sb = new StringBuilder("");

        for (int i = 0; i < bytes.length; i++) {
            stmp = Integer.toHexString(bytes[i] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
        }

        return sb.toString().toUpperCase(Locale.ENGLISH).trim();
    }

    public static String getLog(byte[] bytes) {
        if(bytes == null)
            return null;

        String stmp = "";
        StringBuilder sb = new StringBuilder("");

        for (int i = 0; i < bytes.length; i++) {
            stmp = Integer.toHexString(bytes[i] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }

        return sb.toString().toUpperCase(Locale.ENGLISH).trim();
    }

    /**
     * bytes字符串转换为Byte值
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[] 字节串
     */
    public static byte[] hexStr2Bytes(String src) {
        try {
            int m = 0, n = 0;
            int l = src.length() / 2;
            byte[] ret = new byte[l];

            for (int i = 0; i < l; i++) {
                m = i * 2 + 1;
                n = m + 1;
                short temp = Short.decode("0x" +  src.substring(i * 2, m) + src.substring(m, n));
                ret[i] = BitConverter.shortToByte(temp)[0];
            }

            return ret;
        } catch (Exception e) {
            return null;
        }

    }


}
