package com.ekspres.ssc.smartwater2.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ekspres.ssc.smartwater2.util.HexTools;

import java.util.List;
import java.util.UUID;

/**
 * Created by GEOINFO-PC on 22/02/2018.
 */

public class BluetoothLeService extends Service {

    private final String TAG = "PortableRecharge";
    private BluetoothManager bluetoothManager;      // 蓝牙管理器
    private BluetoothAdapter bluetoothAdapter;      // 蓝牙适配器
    private BluetoothGatt gatt;                     // 蓝牙设备Gatt服务器

    /**
     * The constant ACTION_GATT_CONNECTED.
     */
    public final static String ACTION_GATT_CONNECTED = "com.example.c.cardreader.ACTION_GATT_CONNECTED";
    /**
     * The constant ACTION_GATT_DISCONNECTED.
     */
    public final static String ACTION_GATT_DISCONNECTED = "com.example.c.cardreader.ACTION_GATT_DISCONNECTED";

    /**
     * The constant ACTION_GATT_SERVICES_DISCOVERED.
     */
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.c.cardreader.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.c.cardreader.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.c.cardreader.EXTRA_DATA";
    //发送数据成功
    public final static String ACTION_WRITE_SUCCESS = "com.example.c.cardreader.ACTION_WRITE_SUCCESS";

    // 蓝牙写卡器
    public static UUID UUID_NOTIFY = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static UUID UUID_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");

    /**
     * The Notify characteristic.
     */
    public BluetoothGattCharacteristic notifyCharacteristic;


    /**
     * 写入数据
     *
     * @param strValue 要写入的数据
     */
    public void writeString(String strValue) {
        //notifyCharacteristic.setValue(strValue.getBytes());
        write(strValue.getBytes());
    }


    /**
     * 向蓝牙中写入字节流
     *
     * @param value 字节流
     */
    public boolean write(byte[] value){
        if (notifyCharacteristic == null) {
            return false;
        }
        notifyCharacteristic.setValue(value);
        Log.i("tag", HexTools.getLog(value));
        gatt.writeCharacteristic(notifyCharacteristic);
        return true;
    }

    /**
     * 查找服务
     *
     * @param gattServices gatt services
     */
    public void findService(List<BluetoothGattService> gattServices)
    {
        //Log.i(TAG, "服务总数:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices)
        {
            Log.d(TAG, gattService.getUuid().toString());
            //Log.d(TAG, UUID_SERVICE.toString());
            if(gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString()))
            {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                //Log.i(TAG, "Count is:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                {
                    //Log.e(TAG, gattCharacteristic.getUuid().toString());
                    if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString()))
                    {
                        //Log.i(TAG, gattCharacteristic.getUuid().toString());
                        //Log.i(TAG, UUID_NOTIFY.toString());
                        notifyCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(gattCharacteristic, true);
                        //readCharacteristic(gattCharacteristic);

                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        return;
                    }
                    //Log.e(TAG, "-----------------------------------");
                }
            }
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        // 连接状态改变回调函数
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.i(TAG, "旧状态 = " + status + " 新状态 =" + newState);
            if(status == BluetoothGatt.GATT_SUCCESS)
            {

                //
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;

                    // 发送更新广播，执行时会有一定的延迟
                    broadcastUpdate(intentAction);
                    //Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "连接到GATT服务器");
                    // Attempts to discover services after successful connection.
                    //Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
                    Log.i(TAG, "尝试开始发现服务：" + gatt.discoverServices());
                }
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    gatt.close();
                    gatt = null;

                    //Log.i(TAG, "Disconnected from GATT server.");
                    Log.i(TAG, "GATT服务关闭连接");
                    broadcastUpdate(intentAction);
                }
            }
        }

        // 服务发现回调函数
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                findService(gatt.getServices());
            }
            else {
                if(gatt.getDevice().getUuids() == null)
                    Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onCharacteristicRead");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //Log.e(TAG, "onCharacteristicChanged");
        }

        // 写回调函数
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //Log.e(TAG, "onCharacteristicWrite");
            broadcastUpdate(ACTION_WRITE_SUCCESS);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
            Log.e(TAG, "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
            Log.e(TAG, "onDescriptorWrite");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b) {
            Log.e(TAG, "onReadRemoteRssi");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int a) {
            Log.e(TAG, "onReliableWriteCompleted");
        }

    };

    // 广播
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // 广播更改
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
            sendBroadcast(intent);
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (gatt != null) {
            gatt.close();
            gatt = null;
        }

        return super.onUnbind(intent);
    }

    private final IBinder binder = new BluetoothLeBinder();

    /**
     * 初始化Bluetooth adapter
     *
     * @return 初始化成功，返回true，否则返回false
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "不能初始化蓝牙管理器");
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "不能获取蓝牙适配器");
            return false;
        }

        Log.i(TAG, "蓝牙适配器初始化成功。");
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result         is reported asynchronously through the         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}         callback.
     */
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            //Log.w(TAG, "Device not found.  Unable to connect.");
            Log.w(TAG, "设备没有发现，不能连接。");
            return false;
        }

        //  重新获取gatt
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if(gatt != null)  {
            gatt.close();
            gatt = null;
        }

        gatt = device.connectGatt(this, false, gattCallback);
        //mBluetoothGatt.connect();

        //Log.d(TAG, "Trying to create a new connection.");
        Log.d(TAG, "尝试创建一个新的连接（Trying to create a new connection.）");
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (bluetoothAdapter == null || gatt == null) {
            //Log.w(TAG, "BluetoothAdapter not initialized");
            Log.w(TAG, "蓝牙适配器没有构建");
            return;
        }

        gatt.disconnect();
    }

    /**
     * 关闭gatt服务端
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (gatt == null) {
            return;
        }
        gatt.close();
        gatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        gatt.readCharacteristic(characteristic);
    }

    public void readCharacteristic(){
        readCharacteristic(notifyCharacteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.setCharacteristicNotification(characteristic, enabled);

        for(BluetoothGattDescriptor dp:characteristic.getDescriptors()){
            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(dp);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (gatt == null)
            return null;

        return gatt.getServices();
    }

    /**
     * 蓝牙 binder
     */
    public class BluetoothLeBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
