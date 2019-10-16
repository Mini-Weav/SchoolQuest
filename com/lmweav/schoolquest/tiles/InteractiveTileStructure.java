package com.lmweav.schoolquest.tiles;

import com.lmweav.schoolquest.utilities.TextBoxStructure;

import java.util.HashMap;
import java.util.LinkedHashMap;

/*
 * School Quest: InteractiveTileStructure
 * This class is a data structure for interactive tile behaviour. Behaviours is defined in a
 * text box structure, which is determined by a condition. As text boxes can contain their own
 * runnables, runnables aren't a variable in this class.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class InteractiveTileStructure {

    private HashMap<String, TextBoxStructure> texts = new LinkedHashMap<>();

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    InteractiveTileStructure() { }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    HashMap<String, TextBoxStructure> getTexts() { return texts; }

    void setText(String condition, TextBoxStructure textBox) {
        texts.put(condition, textBox);
    }
    public TextBoxStructure getText(String condition) { return texts.get(condition); }

}
