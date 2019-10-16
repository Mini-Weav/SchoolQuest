package com.lmweav.schoolquest.items;

import com.lmweav.schoolquest.characters.NPC;

/*
 * School Quest: ItemRunnable
 * This class is an implementation of an image view with an item variable. It is for runnables
 * accessible from inventory menu (such as buy, sell, craft, etc.).
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class ItemRunnable implements Runnable {

    private int type;

    private Item item = null;
    private Runnable runnable = null;

    private NPC receiver = null;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public ItemRunnable() {}

    public ItemRunnable(Item item, ItemRunnable itemRunnable) {
        this.item = item;
        this.runnable = itemRunnable.getRunnable();
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public Runnable getRunnable() { return runnable; }
    public void setRunnable(Runnable runnable) { this.runnable = runnable; }

    public NPC getReceiver() { return receiver; }
    public void setReceiver(NPC receiver) { this.receiver = receiver; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void run() {
        if (runnable != null) { runnable.run(); }
    }
}
