package com.blue.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.blue.util.Constant;
import com.orhanobut.logger.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/8/29.
 */

public class BluetoothClientThread implements Runnable {
    public BluetoothClientThread(Handler mHandler, Context mContext, String sendMessage, BluetoothSocket socket) {
        this.mHandler = mHandler;
        this.sendMessage = sendMessage;
        this.socket = socket;
        this.imagePath= Environment.getExternalStorageDirectory()+"/blueTest"+"/1.jpg";
    }
    @Override
    public void run() {
        int result =MSG_WAIT;
        try {
            if(type==1){
                int f_len = sendMessage.getBytes("UTF-8").length; //消息长度
                long totalLen = 4 + 1 + 1 + f_len;//数据的总长度
                byte[] data = new byte[f_len];
                data = sendMessage.getBytes("UTF-8");
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                if(dataOutputStream!=null){
                    dataOutputStream.writeLong(totalLen); //1.写入数据的总长度
                    dataOutputStream.writeByte(type);//2.写入类型
                    dataOutputStream.writeByte(f_len); //3.写入消息的长度
                    dataOutputStream.write(data);    //4.写入消息数据
                    dataOutputStream.flush();
                }
            }else if(type==2){
                fins=new FileInputStream(Constant.FILE_PATH+imagePath);
                long fileDataLen = fins.available(); //文件的总长度
                int f_len=imagePath.getBytes("UTF-8").length; //文件名长度
                byte[] data=new byte[f_len];
                data=imagePath.getBytes("UTF-8");
                long totalLen = 4+1+1+f_len+fileDataLen;//数据的总长度
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream.writeLong(totalLen); //1.写入数据的总长度
                dataOutputStream.writeByte(type);//2.写入类型
                dataOutputStream.writeByte(f_len); //3.写入文件名的长度
                dataOutputStream.write(data);    //4.写入文件名的数据
                dataOutputStream.flush();
                //发送文件
                byte[] buffer=new byte[1024*10];
                downbl=0;
                int size=0;
                long sendlen=0;
                float tspeed=0;
                int i=0;
                long time1= Calendar.getInstance().getTimeInMillis();
                while((size=fins.read(buffer, 0, 1024*10))!=-1)
                {
                    dataOutputStream.write(buffer, 0, size);
                    dataOutputStream.flush();
                    sendlen+=size;
                    Log.v("调试" , "fileDataLen:"+fileDataLen);
                    i++;
                    if(i%10==0){
                        long time2=Calendar.getInstance().getTimeInMillis();
                        tspeed=sendlen/(time2-time1)*1000/1024;
                    }
                    downbl = ((sendlen * 100) / fileDataLen);
                    Logger.e(TAG+" 发送速度："+tspeed+",downbl:"+downbl);
                    Message msg = mHandler.obtainMessage(SEND_PROGRESS);
                    Bundle bun = new Bundle();
                    bun.putFloat("tspeed",downbl);
                    msg.setData(bun);
                    mHandler.sendMessage(msg);
                }
            }
            result = MSG_ING;
            //读取结束标志
            byte[] eofBuffer = new byte[3];
            dataInputStream.read(eofBuffer);//读取消息
            String eof = new String(eofBuffer);
            if("EOF".equals(eof)){
                Logger.e(TAG+"接收到服务端返回的结束标识:"+sendMessage);
            }
        } catch (Exception e) {
            e.getMessage();
            Logger.e("java error:"+e);
            result=MSG_SEND_FAIL;
        } finally {
            close();
            mHandler.sendEmptyMessage(result);
        }
    }



    public void close() {
        if(fins!=null){
            try {
                fins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                fins = null;
            }
        }
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dataInputStream  != null) {
            try {
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
                Log.v("调试", "clientsocket已关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket=null;
        }
    }
    public void setImagePath(String imagePath){
        this.imagePath=imagePath;
    }
    public void setType(byte type){
        this.type=type;
    }
    private byte type =1;
    private String imagePath="";
    private String sendMessage;
    private static final int   MSG_ING=8,
            MSG_SUCCESS=9,MSG_WAIT=10,MSG_SEND_FAIL=11,SEND_PROGRESS=12,RECEIVE_PROGRESS=13;
    private DataInputStream dataInputStream;        //对象输入流
    private DataOutputStream dataOutputStream;    //对象输出流
    private FileInputStream fins;
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;        //用于通信的Socket
    private Handler mHandler;
    private boolean status = true;
    private final String TAG = "BluetoothClientThread ";
    private long downbl;
}
