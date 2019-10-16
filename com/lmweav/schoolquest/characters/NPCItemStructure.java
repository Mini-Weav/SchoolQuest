package com.lmweav.schoolquest.characters;

import com.lmweav.schoolquest.items.Item;

/*
 * School Quest: NPCItemStructure
 * This class is a data structure used for determining how much points are rewarded fro given NPCs
 * items.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class NPCItemStructure {
    private int score;

    private Item item;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    NPCItemStructure(Item item, int score) {
        this.item = item;
        this.score = score;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    int getScore() { return score; }

    public Item getItem() { return item; }

}
