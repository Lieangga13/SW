package com.ekspres.ssc.smartwater2.jetsonblereader;

import com.ekspres.ssc.smartwater2.util.BitConverter;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public class BleMessageBase {
    //private int len = 1 + 1 + 1 + 1 + 1 + 1 + 0x00 + 1 + 1 + 1;

    private int length;		// 消息长度
    private byte[] data;		// 消息体
    private int command;		// 命令字
    private int type;		    // 卡类型
    private int offset;		// 读写偏移地址
    private int count;		    // 读写长度
    private byte status;		// 状态字

    /**
     * 获取消息长度
     */
    public int getLength() {
        return length;
    }

    /**
     * 设置消息长度
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * 获取消息体
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 设置消息体
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * 获取命令字
     */
    public int getCommand() {
        return command;
    }

    /**
     * 设置命令字
     */
    public void setCommand(int command) {
        this.command = command;
    }

    /**
     * 获取卡类型
     */
    public int getType() {
        return type;
    }

    /**
     * 设置卡类型
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * 获取偏移地址
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 设置偏移地址
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * 获取读写长度
     */
    public int getCount() {
        return count;
    }

    /**
     * 设置读写长度
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 获取状态码
     */
    public byte getStatus() {
        return status;
    }

    /**
     * 设置状态码
     */
    public void setStatus(byte status) {
        this.status = status;
    }


    /**
     * 构造函数
     * @param command 命令字
     * @param type	类型
     */
    public BleMessageBase(int command, int type) {
        this.command = command;
        this.type = type;
    }

    /**
     * 构造函数
     * @param bytes 字节流
     */
    public BleMessageBase(byte[] bytes) {
        int i = 0;

        // 消息长度（确定整个协议的有效数据。协议长度+3字节的长度即为整个协议的总长度，即可确认所有的有效数据。）
        this.length = bytes[i++] + 3;

        // 命令字
        this.command = bytes[i++];

        // 卡类型
        this.type = bytes[i++];

        // 读写偏移
        byte[] buffer = new byte[2];
        System.arraycopy(bytes, i, buffer, 0, buffer.length);
        this.offset = BitConverter.byteToShort(buffer);
        i += buffer.length;

        // 读写长度
        this.count = bytes[i++];



        // 数据域
        if((byte)this.command == (byte)0x01 || (byte)this.command == (byte)0x02){
            this.data = new byte[this.count];
            System.arraycopy(bytes, i, this.data, 0, this.count);
            i += this.count;
        }

        // 状态字
        this.status = bytes[i++];

        // 校验
    }

    /**
     * 输出二进制信息
     */
    public byte[] toBytes() {
        byte[] bytes = new byte[19];

        int i = 1;

        // 长度[1]
        //bytes[i++] = (byte)(this.length - 3);

        // 命令类别[1]
        bytes[i++] = (byte)this.command;

        // 卡类型[1]
        bytes[i++] = (byte)this.type;

        // 读写偏移地址[2]
        byte[] buffer = BitConverter.shortToByte((short)this.offset);
        System.arraycopy(buffer, 0, bytes, i, buffer.length);
        i += buffer.length;

        // 读写长度[1]
        bytes[i++] = (byte)this.count;

        // 数据域
        if (this.data != null && this.data.length > 0) {
            System.arraycopy(this.data, 0, bytes, i, this.data.length);
            i += this.data.length;
        }

        // 状态字
        i += 1;

        // 校验码
        short check = getCheck(bytes);
        buffer = BitConverter.shortToByte(check);
        System.arraycopy(buffer, 0, bytes, i, buffer.length);
        i += 2;

        // 长度
        bytes[0] = (byte)(i - 3);

        return bytes;
    }

    /**
     * GetCheck
     * 计算校验和
     * @param buffer 要计算校验和的数组
     */
    public short getCheck(byte[] buffer) {
        char bSum = 0;
        for (int i = 0; i < buffer.length; i++){
            bSum += buffer[i];
            if(buffer[i] < 0)
                bSum += 256;
        }

        bSum ^= 0x5873;

        return (short)bSum;
    }


}
