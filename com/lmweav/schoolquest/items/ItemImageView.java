package com.lmweav.schoolquest.items;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

/*
 * School Quest: ItemImageView
 * This class is an implementation of an image view with an item variable. It is used to render the
 * inventory menu.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class ItemImageView extends AppCompatImageView {

    private Item item;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public ItemImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ItemImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemImageView(Context context) {
        super(context);
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public void setItem(Item item) { this.item = item; }
    public Item getItem() { return item; }

}
