package com.lmweav.schoolquest.utilities;

import com.lmweav.schoolquest.GameActivity;

/*
 * School Quest: TextBoxRunnable
 * This class is an implementation of the Runnable interface that is used to determine whether to
 * close a text box. If a text box's runnable is a TextBoxRunnable, the default text box runnable
 * (which closes the text box, re-enables player-character movement etc.) will not be run. It is
 * used when another text box needs to be displayed immediately after interacting with a text box.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public class TextBoxRunnable implements Runnable {

    public TextBoxStructure textBox;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    protected TextBoxRunnable(TextBoxStructure textBox) {
        this.textBox = textBox;
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void run() {
        GameActivity.getInstance().displayTextBox(textBox);
    }

}
