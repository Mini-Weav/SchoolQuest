package com.lmweav.schoolquest.tiles;

import java.util.HashMap;
import java.util.LinkedHashMap;

/*
 * School Quest: TileBehaviourStructure
 * This class is a data structure for tile behaviour. The behaviour is defined in a runnable, and
 * the trigger for the runnable is defined in a condition. An example tile behaviour in the game
 * is an arrow appearing on an invisible door tile when the player character is on the adjacent
 * tile.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
class TileBehaviourStructure {

    private HashMap<String, Runnable> runnables = new LinkedHashMap<>();

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    TileBehaviourStructure() { }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    HashMap<String, Runnable> getRunnables() { return runnables; }

    void setRunnables(String condition, Runnable runnable) {
        runnables.put(condition, runnable);
    }

}
