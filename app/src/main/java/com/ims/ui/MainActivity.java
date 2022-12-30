package com.ims.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;
import com.ims.ICrudService;
import com.ims.R;
import com.ims.adapters.ItemListAdapter;
import com.ims.content.model.Item;
import com.ims.helpers.FileScanner;
import com.ims.helpers.IOUtils;
import com.ims.prefs.ImsPrefs_;
import com.ims.receivers.SmsObserver;
import com.ims.services.CrudService;
import com.ims.services.FileScannerService_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_aidl)
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();

    public static ICrudService service;
    private AddServiceConnection connection;

    @ViewById
    ListView listView;

    @Bean
    ItemListAdapter adapter;

    @Pref
    ImsPrefs_ imsPrefs;

    @ViewById
    EditText search;

    private List<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "318e5b3f");
    }

    class AddServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = ICrudService.Stub.asInterface(boundService);
            Log.i(TAG, "onServiceConnected(): Connected");
            initList();
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Log.i(TAG, "onServiceDisconnected(): Disconnected");
        }
    }

    @AfterViews
    void afterViews() {
        initServices();
        initSmsObserver();
        initFileObserver();

        if (!imsPrefs.trackCalls().get() && !imsPrefs.trackFiles().get() && !imsPrefs.trackMessages().get()
                && !imsPrefs.trackPages().get() && !imsPrefs.trackPhotos().get()) {
            startActivity(new Intent(this, SettingsActivity_.class));
        }

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                try {
                    items = service.find(charSequence.toString());
                    adapter.setItems(items);
                    listView.setAdapter(adapter);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Background
    void initFileObserver() {
        FileScanner.scan(this);
    }

    private void initSmsObserver() {
        final Uri SMS_STATUS_URI = Uri.parse("content://sms");
        SmsObserver smsSentObserver = new SmsObserver(new Handler(), this);
        this.getContentResolver().registerContentObserver(SMS_STATUS_URI, true, smsSentObserver);
    }

    private void initServices() {
        Log.i(TAG, "initServices()");
        connection = new AddServiceConnection();
        Intent i = new Intent();
        i.setClassName("com.ims", CrudService.class.getName());
        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "initService() bound value: " + ret);
        FileScannerService_.intent(getApplication()).start();
    }

    private void testItems() {
        // 1
        items = new ArrayList<Item>();
        Item it = new Item();
        it.text = "node1";
        it.tags.add("tag1");
        it.tags.add("tag2");
        it.tags.add("tag4");
        it.tags.add("tag3");
        items.add(it);
        // 2
        it = new Item();
        it.text = "node2";
        it.tags.add("tag4");
        it.tags.add("tag5");
        items.add(it);
        // 3
        it = new Item();
        it.text = "node3";
        it.tags.add("tag2");
        it.tags.add("tag5");
        items.add(it);
        // 4
        it = new Item();
        it.text = "node4";
        it.tags.add("tag3");
        it.tags.add("tag4");
        items.add(it);

        for (Item i : items)
            i.save(this);
    }

    private void initList() {
        try {
            items = service.getAll();
            // test items
            if (items.size() == 0)
                testItems();
            //
            if (items == null)
                return;
            adapter.setItems(items);
            listView.setAdapter(adapter);
        } catch (RemoteException e) {
            Log.i(TAG, "Data fetch failed with: " + e);
            e.printStackTrace();
        }
    }

    @ItemClick
    void listViewItemClicked(final Item item) {
        if (item.type.equals(Item.TYPE_FILE)) {
            Uri path = Uri.fromFile(new File(item.text));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setDataAndType(path, IOUtils.getMimeType(item.text));
            startActivity(intent);
        } else if (item.type.equals(Item.TYPE_PAGE)) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.setData(Uri.parse((String) item.extensions.get(Item.KEY_URL)));
            i.setClassName("com.UCMobile.intl", "com.UCMobile.main.UCMobile");
            try {
                startActivity(i);
            } catch (Exception e) {
                i = new Intent(Intent.ACTION_VIEW);
                i.addCategory(Intent.CATEGORY_BROWSABLE);
                i.setData(Uri.parse((String) item.extensions.get(Item.KEY_URL)));
                startActivity(i);
            }
        } else if (item.type.equals(Item.TYPE_CONTACT)) {

        } else if (item.type.equals(Item.TYPE_MESSAGE)) {
            Intent intent = new Intent(this, TextViewerActivity_.class);
            File file = new File(String.format("/sms/%s.sms", item.getId()));
            intent.putExtra(TextViewerActivity.EXTRA_TEXT, IOUtils.readFile(file));
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                initList();
                return true;
            case R.id.scan:
                initFileObserver();
                return true;
            case R.id.graph:
                startActivity(new Intent(this, GraphActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity_.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void releaseServices() {
        unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
        FileScannerService_.intent(getApplication()).stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseServices();
        BugSenseHandler.closeSession(this);
    }


}
