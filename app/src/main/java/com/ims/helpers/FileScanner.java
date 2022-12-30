package com.ims.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.FileObserver;
import android.os.RemoteException;
import android.util.Log;

import com.ims.content.model.Item;
import com.ims.ui.MainActivity;
import com.ims.ui.SettingsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class FileScanner {
    private static final String TAG = FileScanner.class.getName();

    private static ArrayList<DirectoryObserver> fileObservers;

    public static void scan(final Context context) {

        IOUtils.scan(Environment.getExternalStorageDirectory());
        List<String> files = IOUtils.result;
        IOUtils.result = new ArrayList<String>();
        HashSet hs = new HashSet();
        hs.addAll(files);
        files.clear();
        files.addAll(hs);
        if (fileObservers != null) {
            for (FileObserver fileObserver : fileObservers) {
                fileObserver.stopWatching();
            }
        }
        fileObservers = new ArrayList<DirectoryObserver>();
        for (String file : files) {
            DirectoryObserver fileObserver = new DirectoryObserver(file, (FileObserver.CREATE | FileObserver.OPEN)) {
                @Override
                public void onEvent(int event, String file) {

                    SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
                    if (!settings.getBoolean("trackFiles", false))
                        return;

                    if (!Arrays.asList(IOUtils.filePatterns).contains(IOUtils.getFileExtension(new File(file))))
                        return;

                    Log.d(TAG, file + " " + event);

                    if (MainActivity.service == null)
                        return;

                    if (event == FileObserver.CREATE) {
                        Item item = new Item();
                        item.type = Item.TYPE_FILE;
                        item.tags = StringUtils.getFileTags(this.getPath());
                        item.text = this.getPath() + file;
                        try {
                            MainActivity.service.create(item);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else if (event == FileObserver.OPEN) {
                        try {
                            List<Item> items = MainActivity.service.getByText(this.getPath() + file);
                            if (items.size() > 0) {
                                items.get(0).accessed = new Date().getTime();
                                MainActivity.service.update(items.get(0));
                            } else {
                                Item item = new Item();
                                item.type = Item.TYPE_FILE;
                                item.tags = StringUtils.getFileTags(this.getPath());
                                item.text = this.getPath() + "/" + file;
                                MainActivity.service.create(item);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            fileObserver.startWatching();
            fileObservers.add(fileObserver);
        }

    }

}
