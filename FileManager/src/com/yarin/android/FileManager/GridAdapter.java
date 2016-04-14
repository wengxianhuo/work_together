package com.yarin.android.FileManager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {

	Context context =null;
	List<IconifiedText> items = null;
	
	public GridAdapter(Context context,ArrayList<IconifiedText> items){
	
		this.context = context;
		this.items = items;
	}
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = (LayoutInflater.from(context)).inflate(R.layout.griditem, null);
		
			ImageView imgView = (ImageView) view.findViewById(R.id.iamgeView);
			TextView textView = (TextView) view.findViewById(R.id.textView);
			imgView.setImageDrawable(items.get(position).getIcon());
			textView.setText(items.get(position).getText());
		
		return view;
	}

}
