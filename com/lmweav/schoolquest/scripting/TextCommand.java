package com.lmweav.schoolquest.scripting;

import android.view.View;

import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.characters.GameCharacter;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.utilities.TextBoxStructure;

/*
 * School Quest: TextCommand
 *
 * This class is a script command that creates and displays a text box.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class TextCommand extends Command {

    private boolean started;

    private TextBoxStructure textBox;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    TextCommand(GameCharacter actor, String text) {
        if (actor instanceof NPC) {
            textBox = new TextBoxStructure(text, (NPC) actor);
        } else { textBox = new TextBoxStructure(text); }
    }

    TextCommand(GameCharacter actor, String text, Runnable runnable) {
        if (actor instanceof NPC) {
            textBox = new TextBoxStructure(text, runnable, true, (NPC) actor);
        } else { textBox = new TextBoxStructure(text, runnable, true, null); }
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void execute() {
        if (!started) {
            GameActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GameActivity.getInstance().displayTextBox(textBox);
                }
            });
            started = true;
        } else {
            if (GameActivity.getInstance().findViewById(R.id.textbox).getVisibility() == View.GONE) {
                finished = true;
                started = false;
            }
        }

    }

    @Override
    public void reset() {
        finished = false;
        started = false;
    }
}
