package com.blue.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.blue.R;

import java.util.List;

/**
 * Created by Administrator on 2017/8/25.
 */

public class MBluetoothAdapter extends AbstractAdapter {

    public MBluetoothAdapter(Context mContext, List list, Handler mHandler) {
        super(mContext, list, mHandler);
    }

    @Override
    public View buildView(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.lv_bluetooth, null);
    }

    @Override
    public Object buildViewHolder(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.bluetoothName = (TextView) view.findViewById(R.id.bluetoothName);
        viewHolder.bluetoothAddress = (TextView) view.findViewById(R.id.bluetoothAddress);
     //   viewHolder.select= (Button) view.findViewById(R.id.select);
        return viewHolder;
    }

    @Override
    public void initData(View view, Object viewHolder, int position) {
        BluetoothDevice bd = (BluetoothDevice) listData.get(position);
        ViewHolder vh = (ViewHolder) viewHolder;
        vh.bluetoothName.setText(bd.getName());
        vh.bluetoothAddress.setText(bd.getAddress());
    }

    private class ViewHolder {
        TextView bluetoothName, bluetoothAddress;
        Button select;
    }
}
