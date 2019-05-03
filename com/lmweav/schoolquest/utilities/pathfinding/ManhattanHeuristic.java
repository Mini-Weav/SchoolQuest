package com.lmweav.schoolquest.utilities.pathfinding;

/*
 * School Quest: ManhattanHeuristic
 * This class is an implementation of the Manhattan heuristic.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class ManhattanHeuristic implements AStarHeuristic{

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public float getCost(int startX, int startY, int goalX, int goalY) {
        int dx = Math.abs(goalX - startX);
        int dy = Math.abs(goalY - startY);

        return dx + dy;
    }
}
