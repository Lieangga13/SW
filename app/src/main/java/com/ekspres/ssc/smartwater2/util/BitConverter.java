package com.ekspres.ssc.smartwater2.util;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public class BitConverter {
    //long类型转成byte数组
    public static byte[] longToByte(long number) {

        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(temp & 0xff).byteValue();	// 将最低位保存在最低位
            temp = temp >> 8; 								// 向右移8位
        }

        return b;
    }

    //byte数组转成long
    public static long byteToLong(byte[] b) {

        long s = 0;
        long s0 = b[0] & 0xff;// 最低位
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;// 最低位
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff;

        // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    /**
     * 将long类型转换成为无符号整型（uint)的字节数组
     * TODO
     * @param number
     * @return
     * @throws
     */
    public static byte[] longToUintByte(long number)
    {
        byte[] temp = new byte[4];
        byte[] buffer = longToByte(number);
        System.arraycopy(buffer, 0, temp, 0, 4);
        return temp;
    }

    public static long uintByteToLong(byte[] b)
    {
        byte[] temp = new byte[8];
        System.arraycopy(b, 0, temp, 0, 4);
        return byteToLong(temp);
    }

    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = Integer.valueOf(temp & 0xff).byteValue();	// 将最低位保存在最低位
            temp = temp >> 8; 									// 向右移8位
        }

        return b;
    }

    public static int byteToInt(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;	// 最低位
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = Integer.valueOf(temp & 0xff).byteValue();
            temp = temp >> 8; 	// 向右移8位
        }
        return b;
    }

    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);	// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    // 反转字节数组
    public static void reverse(byte[] b) {
        for (int i = 0; i < b.length / 2; i++) {
            byte temp = b[i];
            b[i] = b[b.length - 1 - i];
            b[b.length - 1 - i] = temp;
        }
    }
}
