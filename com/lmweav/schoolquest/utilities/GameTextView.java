package com.lmweav.schoolquest.utilities;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import static android.graphics.Typeface.NORMAL;

/*
 * School Quest: GameTextView
 * This class is a sub class of TextView that displays a custom font.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class GameTextView extends AppCompatTextView {

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public GameTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public GameTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameTextView(Context context) {
        super(context);
        init();
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/press_start_2p.ttf");
        setTypeface(tf ,NORMAL);
    }
}
