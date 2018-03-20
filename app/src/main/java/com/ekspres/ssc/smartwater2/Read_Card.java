package com.ekspres.ssc.smartwater2;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ekspres.ssc.smartwater2.broadcast.GattUpdateReceiver;
import com.ekspres.ssc.smartwater2.jetsonblereader.BleMessageBase;
import com.ekspres.ssc.smartwater2.jetsonblereader.JetsonBelRead;
import com.ekspres.ssc.smartwater2.listener.OnScanListener;
import com.ekspres.ssc.smartwater2.listener.onReceiveListener;
import com.ekspres.ssc.smartwater2.service.BluetoothLe;
import com.ekspres.ssc.smartwater2.service.BluetoothLeService;
import com.ekspres.ssc.smartwater2.util.HexTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static com.ekspres.ssc.smartwater2.util.HexTools.hexStr2Bytes;

public class Read_Card extends Activity implements View.OnClickListener, onReceiveListener {

    private static final String TAG       = "TAG";
    private static final String CLEAR_URL = "http://192.168.1.47:9297/appinterface.aspx?type=1";     //reset card
    //private static final String CLEAR_URL = "http://192.168.1.47:9297/appinterface.aspx?type=2";     //sett card
    //private static final String CLEAR_URL = "http://192.168.1.47:9297/appinterface.aspx?type=3";     //user card
    //private static final String CLEAR_URL = "http://192.168.1.47:9297/appinterface.aspx?type=4";     //refund card
    private Button mReadBtn;                      //读按钮
    private Button   mWriteBtn;                     //写按钮
    private Button   mClearBtn;                     //清卡
    private Button   mConnectBtn;                   //连接
    private EditText mEdit;                         //输入区

    private String mWriteInfo;                      //写卡内容
    private              StringBuilder mBuilder          = new StringBuilder(); //读卡数据保存
    private static final int           REQUEST_ENABLE_BT = 1; //请求
    private static final int           DATA_INDEX        = 12;//数据开始的索引
    private int                index;              //切割索引
    private BluetoothLe bluetoothle;
    private GattUpdateReceiver gattUpdateReceiver;
    private BluetoothLeService bluetoothLeService;

    private boolean isConnected;                 //连接标识
    private boolean isReading;                   //正在读卡
    private boolean isWrtring;                   //正在写卡

    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "准备绑定蓝牙服务");
            bluetoothLeService = ((BluetoothLeService.BluetoothLeBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "蓝牙服务绑定失败");
                return;
            }
            Log.i(TAG, "蓝牙服务绑定成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBLT();
        initView();
        setListener();
    }

    /**
     * 检查蓝牙的状态是否可用
     */
    private void checkBLT() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        // Android系统从4.3版本开始支持蓝牙4.0(BLE)
        // 检查应用程序安装包是否支持蓝牙4.0
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "您的设备不支持蓝牙4.0", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Log.i(TAG, "应用支持蓝牙4.0");

        bluetoothle = new BluetoothLe();
        boolean success = bluetoothle.init(this);
        bluetoothle.setOnScanListener(new OnScanListener() {
            @Override
            public void scan(BluetoothDevice bleDevice) {
                Log.i(TAG, String.format(Locale.getDefault(), "找到蓝牙设备，名称：%s  地址：%s", bleDevice.getName(), bleDevice.getAddress()));
                // 连接连牙
                Log.i(TAG, "连接连牙.");
                bluetoothLeService.connect(bleDevice.getAddress());
            }
        });

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        if (adapter == null) {
            Toast.makeText(this, "您的设备不支持蓝牙4.0!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            if (!adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if (!success) {
            Log.e(TAG, "初始化蓝牙失败.");
            finish();
        }


        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);


        gattUpdateReceiver = new GattUpdateReceiver();
        gattUpdateReceiver.setOnReceiveListener(this);
    }

    private void initView() {
        mReadBtn = (Button) findViewById(R.id.main_read);
        mWriteBtn = (Button) findViewById(R.id.main_write);
        mClearBtn = (Button) findViewById(R.id.main_clear);
        mConnectBtn = (Button) findViewById(R.id.main_connect);
        mEdit = (EditText) findViewById(R.id.main_text);
    }

    private void setListener() {
        mReadBtn.setOnClickListener(this);
        mWriteBtn.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);
        mConnectBtn.setOnClickListener(this);
    }

    @Override
    public void onConnect() {
        // 连接成功
        isConnected = true;
        onConnectStateChange();
    }

    @Override
    public void onDisconnect() {
        isConnected = false;
        isReading = false;
        isWrtring = false;
        onConnectStateChange();
    }


    @Override
    public void onDataAvailable(Intent intent) {
        byte[] bytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
        String data = HexTools.getHexString(bytes);
        Log.i(TAG, "收到数据：" + data);
        BleMessageBase ble = new BleMessageBase(bytes);

        if (ble.getCommand() == (byte) 0x85) {
            Log.e(TAG, "无卡通知【" + data + "】");
            Toast.makeText(this, "没有检测到卡，请重新插卡", Toast.LENGTH_SHORT).show();
            return;
        }

        String subData = data.substring(DATA_INDEX, DATA_INDEX + 2);

        if ("FB".equals(subData) && data.substring(0, 2).equals("06")) {
            Toast.makeText(this, "操作异常，请重试", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (data.substring(2, 4).equals("81")) {
                //读卡回复
                if (data.startsWith("0E")) {
                    //8条数据
                    mBuilder.append(data.substring(DATA_INDEX, DATA_INDEX + 16));
                } else if (data.startsWith("0A")) {
                    //4条数据
                    mBuilder.append(data.substring(DATA_INDEX, DATA_INDEX + 8));
                    mEdit.setText(mBuilder.toString());
                    mEdit.setSelection(mBuilder.length());
                    mBuilder.setLength(0);
                    isReading = false;
                }
            } else {
                writeCard();
            }
        }
    }

    @Override
    public void onWriteSuccess() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_read:
                readCard();
                break;

            case R.id.main_write:
                mWriteInfo = mEdit.getText().toString();
                if (TextUtils.isEmpty(mWriteInfo)) {
                    Toast.makeText(this, "请输入写卡内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mWriteInfo.length() != 56) {
                    Toast.makeText(this, "请输入28位写卡内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isWrtring) {
                    Toast.makeText(this, "正在写卡，请稍后", Toast.LENGTH_SHORT).show();
                }

                if (isConnected) {
                    index = 0;
                    writeCard();
                } else {
                    Toast.makeText(this, "请先连接读卡器", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.main_clear:
                getClearData();

                break;

            case R.id.main_connect:
                if (isConnected) {
                    bluetoothLeService.disconnect();
                } else {
                    bluetoothle.scanLeDevice(true);
                }
                break;
        }
    }



    /**
     * 读卡
     */
    private void readCard() {
        if (isReading) {
            Toast.makeText(this, "正在读卡，请稍后", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isConnected) {
            byte[] bytes = JetsonBelRead.rfRead();
            if (!bluetoothLeService.write(bytes)) {
                isConnected = false;
                Toast.makeText(this, "蓝牙缓存错误，请重启", Toast.LENGTH_SHORT).show();
                onConnectStateChange();
            } else {
                isReading = true;
            }
        } else {
            Toast.makeText(this, "请先连接读卡器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "启动蓝牙成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "启动蓝牙失败，请手动启动蓝牙", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 连接状态改变
     */
    private void onConnectStateChange() {
        if (isConnected) {
            mConnectBtn.setText("connect");
        } else {
            mConnectBtn.setText("disconnect");
        }
    }

    private void writeCard() {
        if (index == 6) {
            isWrtring = false;
            return;
        }
        index++;
        String substring = mWriteInfo.substring(index * 8, (index + 1) * 8);
        if (!bluetoothLeService.write(JetsonBelRead.rfWrite(hexStr2Bytes(substring), index))) {
            isConnected = false;
            Toast.makeText(this, "蓝牙缓存错误，请重启", Toast.LENGTH_SHORT).show();
            onConnectStateChange();
        } else {
            isWrtring = true;
        }
    }

    private void getClearData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(CLEAR_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufr=new BufferedReader(new InputStreamReader(inputStream));
                    final StringBuilder response=new StringBuilder();
                    String line=null;
                    while((line=bufr.readLine())!=null){
                        response.append(line);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEdit.setText(response.toString());
                        }
                    });


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isConnected = false;
        unbindService(serviceConnection);
        if (bluetoothLeService != null) {
            bluetoothLeService.close();
            bluetoothLeService = null;
        }
    }
}
