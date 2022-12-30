package com.ims.helpers;

import com.ims.content.model.Item;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static Item deserializeItem(String json) throws JSONException, Exception {
        JSONObject jsonObject = new JSONObject(json);
        Item item = new Item(jsonObject.getString("id"));
        item.created = Long.valueOf(jsonObject.getString("created"));
        item.type = jsonObject.getString("type");
        item.lat = Double.valueOf(jsonObject.getString("lat"));
        item.lng = Double.valueOf(jsonObject.getString("lng"));
        if (jsonObject.getJSONObject("extensions").length() > 0) {
            JSONObject jsonExtensions = jsonObject.getJSONObject("extensions");
            for (int i = 0; i < jsonExtensions.names().length(); i++) {
                item.extensions.put(jsonExtensions.names().getString(i), jsonExtensions.get(jsonExtensions.names().getString(i)));
            }
        }
        if (!jsonObject.isNull("text"))
            item.text = jsonObject.getString("text");
        return item;
    }

}
