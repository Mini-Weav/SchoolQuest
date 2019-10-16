package com.lmweav.schoolquest.utilities.pathfinding;

/*
 * School Quest: AStarHeuristic
 * This interface is to be implemented for heuristics used in A Star path-finding.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public interface AStarHeuristic {

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    float getCost(int startX, int startY, int goalX, int goalY);

}
