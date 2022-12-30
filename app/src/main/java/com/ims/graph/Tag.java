package com.ims.graph;

import com.ims.content.model.Item;

import java.util.ArrayList;
import java.util.List;

public class Tag {

    public String name;
    public List<Item> items = new ArrayList<Item>();

    public Tag(String name, Item item) {
        this.name = name;
        items.add(item);
    }
}
