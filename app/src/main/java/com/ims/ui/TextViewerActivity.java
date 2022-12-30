package com.ims.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.ims.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_textviewer)
public class TextViewerActivity extends Activity {
    private static final String TAG = TextViewerActivity.class.getName();

    public static final String EXTRA_TEXT = "text";

    @ViewById(R.id.text)
    TextView text;

    @AfterViews
    void afterViews() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            text.setText(extras.getString(EXTRA_TEXT));
        }
    }
}
