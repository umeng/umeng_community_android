
package com.umeng.comm.ui.imagepicker.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MBaseAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected ArrayList<T> mDataSet;

    public MBaseAdapter(Context context, ArrayList<T> models) {
        this.mContext = context;
        if (models == null) {
            this.mDataSet = new ArrayList<T>();
        } else {
            this.mDataSet = models;
        }
    }

    @Override
    public int getCount() {
        if (mDataSet != null) {
            return mDataSet.size();
        }
        return 0;
    }

    @Override
    public T getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void update(List<T> models) {
        if (models == null) {
            return;
        }
        this.mDataSet.clear();
        for (T t : models) {
            this.mDataSet.add(t);
        }
        notifyDataSetChanged();
    }

    public ArrayList<T> getItems() {
        return mDataSet;
    }

}
