package com.ekspres.ssc.smartwater2.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.ekspres.ssc.smartwater2.listener.OnScanListener;

public class BluetoothLe { private final String TAG = "PortableRecharge";

    //private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;              // 搜索蓝牙时间
    private static final String BLE_NAME = "JETSON-BLE";        // 默认的蓝牙名称


    private boolean scanning;                                      // 是否在搜索蓝牙
    private BluetoothAdapter bluetoothAdapter;                   // 蓝牙适配器

    // 事件
    private OnScanListener onScanListener;

    public boolean init(Activity activity){
        // Android系统从4.3版本开始支持蓝牙4.0(BLE)
        // 检查应用程序安装包是否支持蓝牙4.0
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        //Log.i(TAG, "应用支持蓝牙4.0");

        // 初始化获得一个bluetoothManager，并检测设备是否支持蓝牙
        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(activity.getApplicationContext(), "您的设备不支持蓝牙4.0!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 搜索蓝牙设备
        // scanLeDevice(true);

        return true;
    }

    // 扫描BEL蓝牙设备
    public void scanLeDevice(final boolean enable) {

        // 如果已经在搜索蓝牙，则停止搜索
        if(enable && scanning)
            return;


        if (enable) {

            // 10秒钟后停止搜索蓝牙（SCAN_PERIOD）
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(scanning) {
                        Log.i(TAG, "停止搜索蓝牙（10s）");
                        scanning = false;
                        bluetoothAdapter.stopLeScan(leScanCallback);
                    }
                }
            }, SCAN_PERIOD);

            scanning = true;
            Log.i(TAG, "开始搜索蓝牙.");
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            scanning = false;

            Log.i(TAG, "停止搜索蓝牙");
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    // 蓝牙设备搜索回调
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

            String bleName = device.getName();
            //Log.i(TAG, "找到蓝牙设备" + bleName);

            if (BLE_NAME.equals(bleName)) {
                scanLeDevice(false);                       // 停止搜索蓝牙

                if(onScanListener != null)
                    onScanListener.scan(device);
            }



        }
    };

    /**
     *  设置扫描侦听事件
     * */
    public void setOnScanListener(OnScanListener onScanListener) {
        this.onScanListener = onScanListener;
    }

}
