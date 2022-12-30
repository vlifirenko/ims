package com.ims.helpers;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtils {
    private static final String TAG = StringUtils.class.getName();

    public static String toJson(List<?> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    public static List<Long> fromJson(String str) {
        if (str == null || str.equals("null")) {
            return Arrays.asList(new Long[]{});
        }
        Gson gson = new Gson();
        Long[] result = null;
        result = gson.fromJson(str, Long[].class);
        if (result == null) {
            return Arrays.asList(new Long[]{});
        }
        return Arrays.asList(result);
    }

    public static String[] toStringArray(String convertible) {
        if (TextUtils.isEmpty(convertible)) return new String[0];
        return convertible.split(",");
    }

    public static ArrayList<String> toStringList(String convertible) {
        String[] array = StringUtils.toStringArray(convertible);
        if (array == null) return new ArrayList<String>();
        return new ArrayList<String>(Arrays.asList(array));
    }

    public static ArrayList<Long> toLongList(String convertible) {
        String[] array = StringUtils.toStringArray(convertible);
        if (array == null) return new ArrayList<Long>();
        ArrayList<Long> result = new ArrayList<Long>();
        for (String str : array) {
            result.add(Long.parseLong(str));
        }
        return result;
    }

    public static String toString(List<String> list) {
        if (list == null) return "";
        String result = "";
        for (int i = 0; i < list.size() - 1; i++) {
            result += list.get(i) + ",";
        }
        if (list.size() > 0) {
            result += list.get(list.size() - 1);
        }
        return result;
    }

    public static String toString(List<String> list, String prefix, String postfix, boolean wrap) {
        String leftParentheses, rightParentheses;
        if (wrap) {
            leftParentheses = "(";
            rightParentheses = ")";
        } else {
            leftParentheses = rightParentheses = "";
        }
        if (list == null || list.size() == 0) return leftParentheses + rightParentheses;
        String result = leftParentheses;
        for (int i = 0; i < list.size() - 1; i++) {
            result += prefix + list.get(i) + postfix + ",";
        }
        result += prefix + list.get(list.size() - 1);
        result += postfix + rightParentheses;
        return result;
    }

    public static <T> String serialize(List<T> list) {
        if (list == null || list.size() == 0)
            return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    public static <T> String serialize(T obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static String serialize(Map map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    public static Map deserializeMap(String str) {
        if (str == null || str.equals("null")) {
            return new HashMap();
        }
        Gson gson = new Gson();
        try {
            Map result = gson.fromJson(str, new TypeToken<Map<String, String>>() {
            }.getType());
            if (result == null) {
                return new HashMap();
            }
            return result;
        } catch (JsonSyntaxException e) {
            return new HashMap();
        }
    }

    public static <T> T deserialize(String str) {
        if (str == null || str.equals("null")) {
            return null;
        }
        Gson gson = new Gson();
        Type objType = new TypeToken<T>() {
        }.getType();
        try {
            T result = gson.fromJson(str, objType);
            return result;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static ArrayList<String> deserializeString(String str) {
        if (str == null || str.equals("null")) {
            return null;
        }
        Gson gson = new Gson();
        Type objType = new TypeToken<List<String>>() {
        }.getType();
        try {
            ArrayList<String> result = gson.fromJson(str, objType);
            return result;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static List<String> getFileTags(String file) {
        if (file.length() == 0)
            return null;
        file = file.substring(1);
        List<String> list = Arrays.asList(file.split("/"));
        List<String> result = new ArrayList<String>();
        for (String st : list) {
            if (!st.equals("mnt") && !st.equals("sdcard"))
                result.add(st);
        }
        return result;
    }

}
