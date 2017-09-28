package com.blue.util;

import android.bluetooth.BluetoothDevice;
import android.os.Environment;

import java.util.UUID;

/**
 * Created by Administrator on 2017/8/26.
 */

public class Constant {
    //蓝牙通信标识
    public static final UUID PRIVATE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothDevice bluetoothDevice;
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + "/blueTest/";

    public static BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public static void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        Constant.bluetoothDevice = bluetoothDevice;
    }

    public static final int PORT = 9090, FILE_PORT = 9091;
}
