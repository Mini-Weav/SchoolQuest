package com.lmweav.schoolquest.controllers;

import java.io.Serializable;

/*
 * School Quest: Action
 * This class is contains the actions that are available to AI controllers.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    private int direction = -1;
    private boolean stop;

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getDirection() { return direction; }
    void setDirection(int direction) { this.direction = direction; }

    public boolean isMoving() { return !stop; }
    void setStop(boolean stop) { this.stop = stop; }

}
