package com.lmweav.schoolquest.tiles;

import android.graphics.Point;

/*
 * School Quest: DoorTileStructure
 * This class is a data structure for door tile behaviour. Behaviours include the destination map
 * and co-ordinate, a lock condition and a runnable that is triggered when the door is used. An
 * example door tile behaviour in the game is the doors that lead out of the school building:
 * they are locked unless the game time is after school, and using these doors set the time to
 * evening.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
class DoorTileStructure {

    private int destMap;

    private String lockCondition = "FALSE";
    private Point destCoordinate;

    private Runnable effect = null;


    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    DoorTileStructure(int destMap, Point destCoordinate) {
        this.destMap = destMap;
        this.destCoordinate = destCoordinate;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    int getDestMap() { return destMap; }

    String getLockCondition() { return lockCondition; }
    void setLockCondition(String lockCondition) { this.lockCondition = lockCondition; }

    Point getDestCoordinate() { return destCoordinate; }

    Runnable getEffect() { return effect; }
    void setEffect(Runnable effect) { this.effect = effect; }

}
