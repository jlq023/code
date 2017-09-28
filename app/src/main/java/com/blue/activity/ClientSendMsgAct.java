package com.blue.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blue.thread.BluetoothClientThread;
import com.blue.thread.BluetoothServerThread;
import com.blue.util.Constant;
import com.orhanobut.logger.Logger;

import com.blue.R;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/9/27.
 */

public class ClientSendMsgAct extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_clientsendmsg);
        serverDevice= Constant.getBluetoothDevice();
        setTitle("蓝牙地址:"+serverDevice.getName());
        Logger.e(TAG+"蓝牙地址:"+serverDevice.getName());
        mContext = this;
        initView();
        initData();
        startConnectServer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMsg:
                sendMsg("");
                break;
            case R.id.sendImage:
                sendImage();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if(socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private void sendMsg(String sendMsg){
        String msg = content.getText().toString();
        recentMsg = msg;
        if(!sendMsg.equals("")){
            msg = sendMsg;
        }
        if(msg==null){
            Toast.makeText(mContext,"msg is null!",Toast.LENGTH_LONG).show();
        }
        try {
            socket = serverDevice.createInsecureRfcommSocketToServiceRecord(Constant.PRIVATE_UUID);
            socket.connect();
            Logger.e("蓝牙地址:"+serverDevice.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BluetoothClientThread bluetoothClientThread = new BluetoothClientThread(clientHandler,mContext,msg,socket);
        new Thread(bluetoothClientThread).start();
    }
    private void sendImage(){
        try {
            socket = serverDevice.createInsecureRfcommSocketToServiceRecord(Constant.PRIVATE_UUID);
            socket.connect();
            Logger.e("蓝牙地址:"+serverDevice.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BluetoothClientThread bluetoothClientThread = new BluetoothClientThread(clientHandler,mContext,"",socket);
        bluetoothClientThread.setType((byte)2);
        String mfileName= fileName.getText().toString();
        if(mfileName.equals("1")){
            bluetoothClientThread.setImagePath("1.jpg");
        }else if(mfileName.equals("2")){
            bluetoothClientThread.setImagePath("2.rar");
        }else{
            bluetoothClientThread.setImagePath(mfileName);
        }
        new Thread(bluetoothClientThread).start();
    }
    private void initData(){
    }
    private void startConnectServer() {
        bluetoothServerThread = new BluetoothServerThread(clientHandler,mContext);
        clientThread = new Thread(bluetoothServerThread);
        clientThread.start();

    }

    private void initView() {
        sendMsg = (Button) findViewById(R.id.sendMsg);
        sendMsg.setOnClickListener(this);
        sendImage = (Button) findViewById(R.id.sendImage);
        sendImage.setOnClickListener(this);
        content = (EditText) findViewById(R.id.content);
        msg = (TextView) findViewById(R.id.msg);
        fileName = (EditText) findViewById(R.id.fileName);
    }

    private Handler clientHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bun = msg.getData();
            String info="";
            switch (msg.what) {
                case CONNECT_FAIL:
                    info = "创建连接失败.";
                    if(bun!=null&&bun.getString("msg")!=null){
                        info=bun.getString("msg");
                    }
                    setLog(info,0);
                    break;
                case MSG:
                    info = "有新的消息.";
                    if(bun!=null&&bun.getString("msg")!=null){
                        info=bun.getString("msg");
                    }
                    setLog(info,0);
//                    if(!info.startsWith("no")){
//                        sendMsg("no对方已收到信息"+info);
//                    }
                    break;
                case MSG_ERROR:
                    info = "出错了.";
                    if(bun!=null&&bun.getString("msg")!=null){
                        info=bun.getString("msg");
                    }
                    setLog(info,0);
                    sendMsg("noerror!");
                    break;
                case MSG_ING:
                    setLog(recentMsg+" 发送成功",1);
                    sendImage.setText("发送文件");
                    break;
                case MSG_SEND_FAIL:
                    setLog("发送失败",1);
                    break;
                case CREATE_SUCCESS:
                    setLog("创建连接成功:"+serverDevice.getName(),0);
                    break;
                case SEND_PROGRESS:
                    float progress = bun.getFloat("tspeed");
                    if(progress>=99){
                        sendImage.setText("发送文件");
                    }else{
                        sendImage.setText("发送文件:"+progress);
                    }
                    break;
            }
        }
    };

    private void setLog(String message,int type){
        String info = msg.getText().toString();
        if(type==1){
            info = info + "  "+message;
        }else{
            SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm:ss");
            message = sdf.format(new Date())+":"+message;
            info = info + ","+message;
        }
        msg.setText(info);
    }
    private String recentMsg=  "";
    private BluetoothSocket socket;
    private Thread clientThread, readThread, writerThread;
    private final int CONNECT_SUCCESS = 1, CONNECT_FAIL = 2,CREATE_SUCCESS = 4,MSG=6,MSG_ERROR=7,MSG_ING=8,MSG_SUCCESS=9,MSG_WAIT=10,
            MSG_SEND_FAIL=11,SEND_PROGRESS=12,RECEIVE_PROGRESS=13;
    private Button sendMsg,sendImage;
    private EditText content,fileName;
    private Context mContext;
    private TextView msg;
    private BluetoothDevice serverDevice;
    private String TAG="ClientSendMsgAct ";

    private BluetoothServerThread bluetoothServerThread;
}
