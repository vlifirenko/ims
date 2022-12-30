package com.ims.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.ims.R;
import com.ims.prefs.ImsPrefs_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity
public class SettingsActivity extends PreferenceActivity {

    public static final String PREF_NAME = "ImsPrefs";

    @Pref
    ImsPrefs_ imsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREF_NAME);
        addPreferencesFromResource(R.xml.preferences);
    }
}
