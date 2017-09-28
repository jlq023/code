package com.blue.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.blue.util.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/9/27.
 */

public class WelcomeAct extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        checkPermission();
    }

    private void imageCopy(){
        sharedPreferences = getSharedPreferences("blue",Activity.MODE_PRIVATE);
        int copyStatus=sharedPreferences.getInt("copyStatus",0);
        com.orhanobut.logger.Logger.e("WelcomeAct  copyStatus:"+copyStatus);
        if(copyStatus==0){
            copy(mContext,"i1.jpg", Constant.FILE_PATH,"1.jpg");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("copyStatus",1);
            editor.commit();
        }
    }


    public static void copy(Context myContext, String ASSETS_NAME,
                            String savePath, String saveName) {
        String filename = savePath + "/" + saveName;

        File dir = new File(savePath);
        boolean status = false;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            // 如果目录不中存在，创建这个目录
            if (!dir.exists()) {
                status = dir.mkdirs();
            }
            if (!(new File(filename)).exists()) {
                is = myContext.getClass().getClassLoader().getResourceAsStream("assets/" + ASSETS_NAME);
                fos = new FileOutputStream(filename);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
           try{
               if(fos!=null){
                   fos.close();
               }
               if(is!=null){
                   is.close();;
               }
           }catch (Exception e){
               e.printStackTrace();
           }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            check();
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean isLocat = isLocationOpen(getApplicationContext());
            Toast.makeText(mContext, "isLo:" + isLocat, Toast.LENGTH_LONG).show();
            //开启位置服务，支持获取ble蓝牙扫描结果
            if (!isLocat) {
                Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableLocate, 1);
            } else {
                check();
            }
        }else{
            imageCopy( );
            Intent intent = new Intent(mContext,BlueSearchAct.class);
            startActivity(intent);
            finish();
        }
    }


    /**
     * 判断位置信息是否开启
     *
     * @param context
     * @return
     */
    private static boolean isLocationOpen(final Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider || isNetWorkProvider;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case SD_CARD:
                if(grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    imageCopy();
                    check();
                }else{
                    Toast.makeText(mContext,"您拒绝了程序访问存储卡",Toast.LENGTH_LONG).show();
                }
                break;
            case COARES_LOCATION:
                break;
        }
    }

    private void check() {
        if (Build.VERSION.SDK_INT >= 23) {
            int write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            //动态请求读写sd卡权限
            if (write != PackageManager.PERMISSION_GRANTED || read != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, SD_CARD);
            } else {
                String name = "blueTest";
                File file1 = new File(Environment.getExternalStorageDirectory(), name);
                file1.mkdirs();//创建测试目录

                Intent intent = new Intent(mContext,BlueSearchAct.class);
                startActivity(intent);
                finish();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        COARES_LOCATION);
            }
        }else{

        }
    }
    private SharedPreferences sharedPreferences;
    private final int SD_CARD=300,COARES_LOCATION=301;
    private Context mContext;
}
