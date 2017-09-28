package com.blue.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blue.R;
import com.blue.adapter.MBluetoothAdapter;
import com.blue.util.ClsUtils;
import com.blue.util.Constant;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/9/27.
 */

public class BlueSearchAct extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_client);
        mContenxt = this;
        setTitle("客户端");
        initSearchBroadcast();
        initView();
        initData();
        getBluetoothMsg();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.startSearch:
                bondDevices.clear();
                unbondDevices.clear();
                scanBluetooth();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }
    }

    private void initView() {
        log = (TextView) findViewById(R.id.log);
        log1 = (TextView) findViewById(R.id.log1);
        unbondDevicesListView = (ListView) findViewById(R.id.unbondDevicesListView);
        bondDevicesListView = (ListView) findViewById(R.id.bondDevicesListView);
        bondAdapter = new MBluetoothAdapter(mContenxt, bondDevices, mHandler);
        unbondAdapter = new MBluetoothAdapter(mContenxt, unbondDevices, mHandler);
        unbondDevicesListView.setAdapter(unbondAdapter);
        bondDevicesListView.setAdapter(bondAdapter);
        bondDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                ClsUtils.closeDiscoverableTimeout(mBluetoothAdapter);
                final BluetoothDevice bluetoothDevice = bondDevices.get(position);
                Bundle bun = new Bundle();
                Constant.setBluetoothDevice(bluetoothDevice);
                Intent intent = new Intent(mContenxt, ClientSendMsgAct.class);
                intent.putExtras(bun);
                startActivity(intent);
            }
        });
        startSearch = (Button) findViewById(R.id.startSearch);
        startSearch.setOnClickListener(this);
    }

    private void scanBluetooth() {
        mBluetoothAdapter.startDiscovery();
        scanStatus = true;
        mHandler.sendEmptyMessageDelayed(STOP_SCAN, 1000 * 60);
    }

    private void initData() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //mBluetoothAdapter.setName("blueTestPhone"); //设置蓝牙名称
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "没有检测到蓝牙设备", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        boolean originalBluetooth = (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled());
        if (originalBluetooth) {
            log1.setText("选择连接的设备!");
            scanBluetooth();
        } else if (originalBluetooth == false) {
            mBluetoothAdapter.enable();
        }
    }

    private void getBluetoothMsg() {
        try {
            StringBuilder sb = new StringBuilder();
            //获取本机蓝牙名称
            String name = mBluetoothAdapter.getName();
            //获取本机蓝牙地址
            String address = mBluetoothAdapter.getAddress();
            //获取已配对蓝牙设备
            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
            sb.append("本机名称：" + name).append("\r\n");
            sb.append("本机地址:" + address).append("\r\n");
            log.setText(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STOP_SCAN:
                    if (scanStatus) {
                        mBluetoothAdapter.cancelDiscovery();
                        scanStatus = false;
                        log1.setText("结束扫描.");
                    }
                    break;
            }
        }
    };

    private void initSearchBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        //发现设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备配对状态改变
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //蓝牙设备状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //开始扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //其它设备请求配对
        intentFilter.addAction(ACTION_PAIRING_REQUEST);
        //intentFilter.addAction(BluetoothAdapter.CONNECTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, intentFilter);
    }

    private void findDevice(Intent intent) throws  Exception{
        //获取到设备对象
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String str = device.getName() + "|" + device.getAddress();
        Logger.e("扫描到设备：" + str);
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {//判断当前设备地址下的device是否已经配对
            if (!bondDevices.contains(device)) {
                bondDevices.add(device);
            }
        } else {
            if (!unbondDevices.contains(device)) {
                unbondDevices.add(device);
            }
            if (device.getName().equals(TEST_DEVICE_NAME)) {
                boolean bondStatus = ClsUtils.createBond(device.getClass(), device);
                Logger.i(TAG + " bondStatus:" + bondStatus);
            }
        }
        Log.e("error", "搜索完毕，准备刷新!");
        bondAdapter.notifyDataSetChanged();
        unbondAdapter.notifyDataSetChanged();
    }


    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.e(TAG + "mBluetoothReceiver action =" + action);
            try {
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {//开始扫描
                    setProgressBarIndeterminateVisibility(true);
                    log1.setText("正在扫描设备，请稍候...");
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {//结束扫描
                    Logger.e(TAG + "设备搜索完毕");
                    setProgressBarIndeterminateVisibility(false);
                    log1.setText("扫描完成");
                    bondAdapter.notifyDataSetChanged();
                    unbondAdapter.notifyDataSetChanged();
                    scanStatus = false;
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {//发现设备
                    findDevice(intent);
                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {//蓝牙配对状态的广播
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Logger.e(TAG + device.getName() + "蓝牙配对广播:" + device.getBondState());
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            Logger.e(TAG + device.getName() + "蓝牙配对广播 正在配对......");
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            Logger.e(TAG + device.getName() + "蓝牙配对广播 完成配对,本机自动配对");
                            bondDevices.add(device);
                            unbondDevices.remove(device);
                            bondAdapter.notifyDataSetChanged();
                            unbondAdapter.notifyDataSetChanged();
                            break;
                        case BluetoothDevice.BOND_NONE:
                            Logger.e(TAG + device.getName() + "蓝牙配对广播 取消配对");
                            unbondDevices.add(device);
                            bondDevices.remove(device);
                            unbondAdapter.notifyDataSetChanged();
                            bondAdapter.notifyDataSetChanged();
                        default:
                            break;
                    }
                } else if (action.equals(ACTION_PAIRING_REQUEST)) {//其它设备蓝牙配对请求
                    BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE); //当前的配对的状态
                    try {
                        String path = Environment.getExternalStorageDirectory() + "/blueTest/";
                        String deviceName = btDevice.getName();
                        Logger.e(TAG + "蓝牙 匹配信息：" + deviceName + "," + btDevice.getAddress() + ",state:" + state);
                        if(deviceName.equals(TEST_DEVICE_NAME)){//TEST_DEVICE_NAME  为被匹配蓝牙设备的名称，自己手动定义
                            Object object = ClsUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);
                            abortBroadcast();
                            boolean ret = ClsUtils.setPin(btDevice.getClass(), btDevice, PWD);
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Toast.makeText(mContenxt, "error:" + btDevice + "," + state, Toast.LENGTH_LONG).show();
                    }
                } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {//蓝牙开关状态
                    //    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int statue = mBluetoothAdapter.getState();
                    switch (statue) {
                        case BluetoothAdapter.STATE_OFF:
                            Logger.e("蓝牙状态：,蓝牙关闭");
                            ClsUtils.closeDiscoverableTimeout(mBluetoothAdapter);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Logger.e("蓝牙状态：,蓝牙打开");
                            ClsUtils.setDiscoverableTimeout(1000 * 60, mBluetoothAdapter);
                            scanBluetooth();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Logger.e("蓝牙状态：,蓝牙正在关闭");
                            mBluetoothAdapter.cancelDiscovery();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Logger.e("蓝牙状态：,蓝牙正在打开");
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private final int STOP_SCAN = 1;
    private boolean scanStatus = false;
    private Button startSearch;
    private MBluetoothAdapter bondAdapter, unbondAdapter;
    private List<BluetoothDevice> unbondDevices = new ArrayList<BluetoothDevice>(); // 用于存放未配对蓝牙设备
    private List<BluetoothDevice> bondDevices = new ArrayList<BluetoothDevice>();  // 用于存放已配对蓝牙设备
    private ListView unbondDevicesListView;
    private ListView bondDevicesListView;
    private ProgressDialog progressDialog = null;
    private static final int REQUEST_DISCOVERABLE_BLUETOOTH = 3;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContenxt;
    private TextView log, log1;
    private final String TAG = "BlueSearchAct", PWD = "0000", TEST_DEVICE_NAME = "jianglq";
    private final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";//其它设备蓝牙配对请求
}
