package com.ims.prefs;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface ImsPrefs {

    public boolean trackPages();

    public boolean trackPhotos();

    public boolean trackFiles();

    public boolean trackMessages();

    public boolean trackCalls();
}
