package com.blue.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.blue.util.Constant;
import com.orhanobut.logger.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/8/29.
 */

public class BluetoothServerThread implements Runnable {
    public BluetoothServerThread(Handler mHandler, Context mContext) {
        this.mHandler = mHandler;
        this.mContext = mContext;
    }

    @Override
    public void run() {
        try {
            adapter = BluetoothAdapter.getDefaultAdapter();
            serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("myBluetooth", Constant.PRIVATE_UUID);
            mHandler.obtainMessage(CREATE_SUCCESS).sendToTarget();
            while (status) {
                socket = serverSocket.accept();
                doWork();
            }
        } catch (Exception e) {
            e.printStackTrace();
            setActivityMsg(CREATE_FAIL, "创建服务线程出现异常:" + e.getMessage());
        }
    }

    private void doWork() {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            long totalLen = dataInputStream.readLong();//总长度
            byte type = dataInputStream.readByte();//类型
            String msg = "";
            float downbl = 0;
            if (type == 1) {
                byte len = dataInputStream.readByte();//消息长度
                byte[] ml = new byte[len];
                int size = 0;
                int receivelen = 0;
                while (receivelen < len) {
                    size = dataInputStream.read(ml, 0, ml.length);
                    receivelen += size;
                }
                msg = new String(ml, "UTF-8");
                setActivityMsg(MSG, msg);
            } else if (type == 2) {
                byte len = dataInputStream.readByte();//文件名长度
                byte[] fn = new byte[len];
                dataInputStream.read(fn);//读取文件名
                String filename = new String(fn, "UTF-8");
                Logger.e(TAG + "fileName:" + filename);
                //接收文件名等相关信息
                long datalength = totalLen - 1 - 4 - 1 - fn.length;//文件数据
                String savePath = Constant.FILE_PATH + System.currentTimeMillis() + filename;

                fileOutputStream = new FileOutputStream(savePath, false);
                byte[] buffer = new byte[1024 * 1024];
                int size = -1;
                long receivelen = 0;
                int i = 0;
                float tspeed = 0;
                long time1 = Calendar.getInstance().getTimeInMillis();
                while (receivelen < datalength) {
                    size = dataInputStream.read(buffer);
                    fileOutputStream.write(buffer, 0, size);
                    receivelen += size;
                    i++;
                    if (i % 10 == 0) {
                        long time2 = Calendar.getInstance().getTimeInMillis();
                        tspeed = receivelen / (time2 - time1) * 1000 / 1024;
                    }
                    downbl = (receivelen * 100) / datalength;
                }
                Logger.e("接收完成,receivelen:" + receivelen);
                fileOutputStream.flush();
                setActivityMsg(MSG, filename + "接收完成");
            }
            String endflag = "EOF";
            dataOutputStream.write(endflag.getBytes());
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            setActivityMsg(MSG_ERROR, "处理消息出现异常:" + e.getMessage());
        } finally {
            close();
        }
    }

    private void reportMsg(String msg) throws IOException {
        int f_len = msg.getBytes().length; //消息长度
        long totalLen = 4 + 1 + 1 + f_len;//数据的总长度
        byte[] data = new byte[f_len];
        data = msg.getBytes();
        dataOutputStream.writeLong(totalLen); //1.写入数据的总长度
        dataOutputStream.writeByte(1);//2.写入类型
        dataOutputStream.writeByte(f_len); //3.写入消息的长度
        dataOutputStream.write(data);    //4.写入消息数据
        dataOutputStream.flush();
    }

    private void setActivityMsg(int what, String msg) {
        Bundle bun = new Bundle();
        bun.putString("msg", msg);
        Message message = mHandler.obtainMessage(what);
        message.setData(bun);
        mHandler.sendMessage(message);
    }

    public void close() {
        //关闭流
        if (dataInputStream != null) {
            try {
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
                Log.v("调试", "socket已关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private FileOutputStream fileOutputStream;
    private static final int   CREATE_SUCCESS = 4, CREATE_FAIL = 5, MSG = 6, MSG_ERROR = 7;
    public DataInputStream dataInputStream;        //对象输入流
    public DataOutputStream dataOutputStream;    //对象输出流
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;        //用于通信的Socket
    private BluetoothServerSocket serverSocket;
    private Handler mHandler;
    private Context mContext;
    private boolean status = true;
    private final String TAG = "BluetoothServerThread ";
}
