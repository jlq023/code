package com.blue.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAdapter extends BaseAdapter implements Serializable {
	public AbstractAdapter(Context mContext) {
		listData = new ArrayList();
		context = mContext;
		layoutInflater = LayoutInflater.from(mContext);
	}

	public AbstractAdapter(Context mContext, List list, Handler mHandler) {
		context = mContext;
		listData = list;
		this.handler = mHandler;
		layoutInflater = LayoutInflater.from(mContext);
	}

	public AbstractAdapter(Context mContext, List list) {
		context = mContext;
		listData = list;
		layoutInflater = LayoutInflater.from(mContext);
	}

	public abstract View buildView(LayoutInflater layoutInflater);

	public abstract Object buildViewHolder(View view);

	public abstract void initData(View view, Object viewHolder, int position);

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		Object viewHolder = null;
		this.viewGroup = viewGroup;
//		Util.printLog("getView  position:"+view, 2);
		if (view == null) {
			this.position=position;
			view = buildView(layoutInflater);
			viewHolder = buildViewHolder(view);
			view.setTag(viewHolder);
		} else {
			viewHolder = view.getTag();
		}
		
		initData(view, viewHolder, position);
		
		return view;
	}

	@Override
	public int getCount() {
		if (listData != null) {
			return listData.size();
		} else {
			return 0;
		}

	}

	@Override
	public Object getItem(int i) {
		if (listData != null) {
			return listData.get(i);
		} else {
			return null;
		}

	}

	@Override
	public long getItemId(int i) {
		return (long) i;
	}

	public List getListData() {
		return listData;
	}

	public List getRoot() {
		return listData;
	}

	public void setListData(List list) {
		listData = list;
	}
	public void clearData(){
		if(listData!=null){
			listData.clear();
		}
	}

	protected int position;
	protected ViewGroup viewGroup;
	protected Handler handler;
	protected Context context;
	protected LayoutInflater layoutInflater;
	protected List listData;

}
