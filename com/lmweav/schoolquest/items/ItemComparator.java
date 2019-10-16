package com.lmweav.schoolquest.items;

import java.io.Serializable;
import java.util.Comparator;

/*
 * School Quest: ItemComparator
 * This class is an implementation of the Comparator interface used to compare item objects.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class ItemComparator implements Comparator<Item>, Serializable {

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public int compare(Item item1, Item item2) {
        if (item1.isKeyItem() == item2.isKeyItem()) {
            return ((Integer) item1.getId()).compareTo(item2.getId());
        }
        else if (item1.isKeyItem()) { return -1; }
        else { return 1; }
    }
}
