package com.ims.addons;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.ValueCallback;

import com.ims.content.model.Item;
import com.ims.helpers.IOUtils;
import com.ims.ui.MainActivity;
import com.ims.ui.SettingsActivity;
import com.uc.addon.sdk.remote.AbstractEventReceiver;
import com.uc.addon.sdk.remote.Browser;
import com.uc.addon.sdk.remote.EventBase;
import com.uc.addon.sdk.remote.EventIds;
import com.uc.addon.sdk.remote.EventPageFinished;

import org.androidannotations.annotations.EBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EBean
public class PageFinishEventReceiver extends AbstractEventReceiver {
    private static final String LOG_TAG = PageFinishEventReceiver.class.getName();

    @Override
    public void onEvent(int eventId, EventBase event) {
        if (eventId == EventIds.EVENT_PAGE_FINISH) {
            SharedPreferences settings = getApplicationContext().getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
            if (!settings.getBoolean("trackPages", false))
                return;

            EventPageFinished eventD = (EventPageFinished) event;
            if (event == null) {
                return;
            }

            Browser browser = getBrowser();
            final Item item = new Item();
            item.type = Item.TYPE_PAGE;
            item.extensions.put(Item.KEY_URL, eventD.url);
            File folder = IOUtils.createAppFolder("");
            if (folder != null) {
                final File file = new File(folder, item.getId() + ".html");
                item.extensions.put(Item.KEY_PATH, file.toString());
                browser.util.saveCurrentPage(file.toString(), 0, new ValueCallback() {
                    @Override
                    public void onReceiveValue(Object o) {
                        parseFile(item, file);
                    }
                });
            }
        }
    }

    private void parseFile(Item item, File file) {
        boolean title = false;
        boolean keywords = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Pattern patternTitle = Pattern.compile("<title>(.*?)</title>");
            Pattern patternKeywords = Pattern.compile("<meta.*?name=\"keywords\".*?content=\"(.*?)\".*?>");
            while ((line = br.readLine()) != null) {
                Log.d(LOG_TAG, line);
                line = line.replaceAll("\\s+", " ");
                if (!title) {
                    Matcher m = patternTitle.matcher(line);
                    while (m.find() == true) {
                        item.text = m.group(1);
                        title = true;
                    }
                }
                if (!keywords) {
                    Matcher m = patternKeywords.matcher(line);
                    while (m.find() == true) {
                        item.tags = Arrays.asList(m.group(1).split(","));
                        keywords = true;
                    }
                }
                if (title && keywords)
                    break;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (MainActivity.service != null) {
            try {
                List<Item> items = MainActivity.service.getByText(item.text);
                if (items.size() > 0) {
                    items.get(0).accessed = new Date().getTime();
                    MainActivity.service.update(items.get(0));
                } else {
                    MainActivity.service.create(item);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
