package com.lmweav.schoolquest.utilities;

import com.lmweav.schoolquest.characters.NPC;

/*
 * School Quest: TextBoxStructure
 * This class is a data structure for an instance of a text box.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class TextBoxStructure {

    private boolean noButton;

    private String text;
    private String buttonText1;
    private String buttonText2;
    private Runnable runnable1;
    private Runnable runnable2;
    private NPC npc;

    private String tag;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public TextBoxStructure(String text) {
        this.text = text;
    }

    public TextBoxStructure(String text, NPC npc) {
        this.text = text;
        this.npc = npc;
    }

    public TextBoxStructure(String text, Runnable runnable1, boolean noButton, NPC npc) {
        this.text = text;
        this.runnable1 = runnable1;
        this.noButton = noButton;
        this.npc = npc;
    }

    public TextBoxStructure(String text, String buttonText1, String buttonText2,
                            Runnable runnable1, Runnable runnable2, NPC npc) {
        this.text = text;
        this.buttonText1 = buttonText1;
        this.buttonText2 = buttonText2;
        this.runnable1 = runnable1;
        this.runnable2 = runnable2;
        this.npc = npc;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public boolean isNoButton() { return noButton; }

    public String getText() { return text; }
    public String getButtonText1() { return buttonText1; }
    public String getButtonText2() { return buttonText2; }

    public Runnable getRunnable1() { return runnable1; }
    public Runnable getRunnable2() { return runnable2; }

    public NPC getNpc() { return npc; }

    public void setTag(String tag) { this.tag = tag; }
    public String getTag() { return tag; }

}
