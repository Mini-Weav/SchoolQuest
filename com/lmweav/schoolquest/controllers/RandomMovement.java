package com.lmweav.schoolquest.controllers;


import java.io.Serializable;

import static com.lmweav.schoolquest.Constants.RANDOM_MOVE_CHANCE;
import static com.lmweav.schoolquest.Constants.RANDOM_STOP_CHANCE;

/*
 * School Quest: RandomMovemnt
 *
 * This class handles the NPC steering behaviour between random points.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public class RandomMovement implements Controller, Serializable {

    private double chance;
    private Action action;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public RandomMovement(boolean stop) {
        action = new Action();
        if (stop) { chance = RANDOM_STOP_CHANCE; }
        else { chance = RANDOM_MOVE_CHANCE; }
        action.setStop(stop);
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public Action action() {
        action.setDirection(-1);
        if (Math.random() < chance) { action.setDirection((int) (Math.random() * 4)); }
        return action;
    }
}
