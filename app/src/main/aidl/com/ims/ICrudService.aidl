// IAddService.aidl
package com.ims;

import com.ims.content.model.Item;

interface ICrudService {

    boolean create(in Item item);

    Item get(in int id);

    List<Item> getAll();

    boolean update(in Item item);

    boolean delete(in Item item);

    boolean save(in Item item, in String file_extension, in String body);

    List<Item> getByText(String text);

    List<Item> find(String text);

}
