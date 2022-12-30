package com.ims.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ims.content.model.Item;
import com.ims.content.views.ItemView;
import com.ims.content.views.ItemView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

@EBean
public class ItemListAdapter extends BaseAdapter {

    List<Item> items = new ArrayList<Item>();

    @RootContext
    Context context;

    @AfterInject
    void initAdapter() {
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemView itemView;
        if (convertView == null) {
            itemView = ItemView_.build(context);
        } else {
            itemView = (ItemView) convertView;
        }
        itemView.bind(getItem(position));
        return itemView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
