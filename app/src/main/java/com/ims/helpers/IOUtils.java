package com.ims.helpers;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.ims.content.model.Item;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOUtils {

    public static File createAppFolder(String sub) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/ims/" + sub);
        if (!folder.exists()) {
            if (folder.mkdir())
                return folder;
            else
                return null;
        }
        return folder;
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    public static boolean saveFile(File file, String data) throws IOException {
        if (!file.createNewFile())
            return false;
        FileOutputStream fOut = new FileOutputStream(file);
        OutputStreamWriter myOutWriter =
                new OutputStreamWriter(fOut);
        myOutWriter.append(data);
        myOutWriter.close();
        fOut.close();
        return true;
    }

    public static final String[] filePatterns =
            {"pdf", "doc", "docx", "xls", "xlsx", "fb2", "mobi", "jpg", "jpeg"};

    public static List<String> result = new ArrayList<String>();

    public static void scan(File dir) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    scan(listFile[i]);
                } else {
                    if (Arrays.asList(filePatterns).contains(getFileExtension(listFile[i]))) {
                        result.add(listFile[i].getParent());
                    }
                }
            }
        }
    }

    public static String getMimeType(String filename) {
        String extension = "." + getFileExtension(new File(filename));
        if (extension.length() > 0) {
            String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
            if (webkitMimeType != null)
                return webkitMimeType;
        }
        return "*/*";
    }

    public static String readFile(File file) {
        file = new File(createAppFolder(""), file.toString());
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static List<Item> getAllItems() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/ims");
        File[] filelist = dir.listFiles();
        List<Item> items = new ArrayList<Item>();
        if (filelist == null || filelist.length == 0)
            return null;
        for (File f : filelist) {
            if (!IOUtils.getFileExtension(f).equals("json"))
                continue;
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String json = text.toString();
            try {
                items.add(JsonUtils.deserializeItem(json));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return items;
    }

}
