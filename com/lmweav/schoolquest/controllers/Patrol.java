package com.lmweav.schoolquest.controllers;

import com.lmweav.schoolquest.characters.GameCharacter;
import com.lmweav.schoolquest.utilities.SerializablePoint;

import java.io.Serializable;

import static com.lmweav.schoolquest.Constants.OBJECT_DIRECTION_DOWN;
import static com.lmweav.schoolquest.Constants.OBJECT_DIRECTION_LEFT;
import static com.lmweav.schoolquest.Constants.OBJECT_DIRECTION_RIGHT;
import static com.lmweav.schoolquest.Constants.OBJECT_DIRECTION_UP;
import static com.lmweav.schoolquest.Constants.PATROL_STOP_TICKS;
import static com.lmweav.schoolquest.Constants.SCALED_TILE_SIZE;

/*
 * School Quest: Patrol
 * This class handles the NPC steering behaviour between specified points.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class Patrol implements Controller, Serializable {

    private int index;
    private double step;
    private Action action;
    private GameCharacter object;

    private SerializablePoint[] points;
    private int[] directions = {OBJECT_DIRECTION_UP, OBJECT_DIRECTION_RIGHT,
            OBJECT_DIRECTION_DOWN, OBJECT_DIRECTION_LEFT};

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public Patrol(SerializablePoint... points) {
        action = new Action();
        this.points = new SerializablePoint[points.length];
        System.arraycopy(points, 0, this.points, 0, points.length);
        index = 0;
        step = 0;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public void setObject(GameCharacter object) { this.object = object; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public Action action() {
        SerializablePoint point = points[index];
        if (object.getGY() != point.y * SCALED_TILE_SIZE) {
            action.setStop(false);
            if (object.getGY() > point.y * SCALED_TILE_SIZE) {
                action.setDirection(OBJECT_DIRECTION_UP);
            }
            else { action.setDirection(OBJECT_DIRECTION_DOWN); }
        }
        else if (object.getGX() != point.x * SCALED_TILE_SIZE){
            action.setStop(false);
            if (object.getGX() > point.x * SCALED_TILE_SIZE) {
                action.setDirection(OBJECT_DIRECTION_LEFT);
            }
            else { action.setDirection(OBJECT_DIRECTION_RIGHT); }
        }
        else {
            action.setStop(true);
            step += (1 / (float) PATROL_STOP_TICKS);
            action.setDirection(directions[(int) step % 4]);
            if (step == 4) {
                step = 0;
                index++;
            }
        }
        if (index == points.length) { index = 0; }
        return action;
    }

    public void reset() {
        index = 0;
        step = 0;
    }
}
