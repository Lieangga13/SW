package com.ekspres.ssc.smartwater2.listener;

import android.bluetooth.BluetoothDevice;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public interface OnScanListener {

    /**
     * 蓝牙搜索结果响应
     *
     * @param bleDevice 蓝牙设备
     */
    void scan(BluetoothDevice bleDevice);
}
