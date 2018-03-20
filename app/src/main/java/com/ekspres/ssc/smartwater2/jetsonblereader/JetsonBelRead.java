package com.ekspres.ssc.smartwater2.jetsonblereader;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public class JetsonBelRead {
    // 心跳
    public static byte[] heartbeat(){
        BleMessageBase bleMessageBase = new BleMessageBase(0x00, 0x00);
        return bleMessageBase.toBytes();
    }

    // 获取读卡器状态
    public static byte[] getState(){
        BleMessageBase bleMessageBase = new BleMessageBase(0x05, 0xF1);
        bleMessageBase.setCount(0x0A);
        return bleMessageBase.toBytes();
    }

    public static byte[] rfRead(){
        BleMessageBase bleMessageBase = new BleMessageBase(0x01, 0x00);
        bleMessageBase.setCount(28);
        bleMessageBase.setOffset(0x8001);
        return bleMessageBase.toBytes();
    }

    public static byte[] rfWrite(byte[] data,int offset){
        BleMessageBase bleMessageBase = new BleMessageBase(0x02, 0x00);
        bleMessageBase.setCount(4);
        bleMessageBase.setOffset(0x8001 + offset);
        bleMessageBase.setData(data);
        return bleMessageBase.toBytes();
    }
}
