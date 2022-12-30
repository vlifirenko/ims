package com.ims.content.views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ims.R;
import com.ims.content.model.Item;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@EViewGroup(R.layout.list_item)
public class ItemView extends LinearLayout {

    @ViewById
    TextView updated;

    @ViewById
    TextView text;

    @ViewById
    TextView tags;

    public ItemView(Context context) {
        super(context);
    }

    public void bind(Item item) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
        Date formatDate = Calendar.getInstance().getTime();
        updated.setText(df.format(formatDate));
        text.setText(item.text);
        StringBuilder sb = new StringBuilder();
        if (item.tags != null && item.tags.size() > 0) {
            sb.append(item.tags.get(0));
            for (int i = 1; i < item.tags.size(); i++) {
                sb.append(", ");
                sb.append(item.tags.get(i));
            }
            tags.setText(sb.toString());
        }
    }

}
