package com.lmweav.schoolquest.utilities.pathfinding;

/*
 * School Quest: AStarHeuristic
 * This interface is to be implemented for heuristics used in A Star path-finding.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public interface AStarHeuristic {

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    float getCost(int startX, int startY, int goalX, int goalY);

}
