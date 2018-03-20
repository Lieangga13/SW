package com.ekspres.ssc.smartwater2.listener;

import android.content.Intent;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public interface onReceiveListener {
    void onConnect();
    void onDisconnect();
    void onDataAvailable(Intent intent);
    void onWriteSuccess();
}
