package com.ekspres.ssc.smartwater2.broadcast;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ekspres.ssc.smartwater2.listener.onReceiveListener;
import com.ekspres.ssc.smartwater2.service.BluetoothLeService;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public class GattUpdateReceiver extends BroadcastReceiver {


    private static onReceiveListener mOnReceiveListener;

    public GattUpdateReceiver() {
    }

    public void setOnReceiveListener(onReceiveListener onReceiveListener) {
        mOnReceiveListener = onReceiveListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //Log.i(TAG, "收到广播事件：" + action);
        if (mOnReceiveListener == null) {
            return;
        }
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            mOnReceiveListener.onConnect();
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            mOnReceiveListener.onDisconnect();
        }  else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            mOnReceiveListener.onDataAvailable(intent);
        } else if (BluetoothLeService.ACTION_WRITE_SUCCESS.equals(action)) {
            mOnReceiveListener.onWriteSuccess();
        }
    }
}
