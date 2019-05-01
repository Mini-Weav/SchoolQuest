package com.lmweav.schoolquest.utilities.pathfinding;

import java.util.ArrayList;

/*
 * School Quest: Path
 * This class is an implementation of a path to be used in path finding.
 *
 * There is 1 support class declared here: Step - which makes up a path object.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public class Path {

    private ArrayList<Step> steps = new ArrayList<>();

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    Path() { }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getLength() { return steps.size(); }

    private Step getStep(int index) { return steps.get(index); }

    public int getX(int index) { return getStep(index).getX(); }

    public int getY(int index) { return getStep(index).getY(); }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    void prependStep(int x, int y) { steps.add(0, new Step(x, y)); }

    @Override
    public String toString() {
        StringBuilder path = new StringBuilder();
        for (Step step : steps) {
            path.append(step);
            if (steps.indexOf(step) != steps.size() - 1) { path.append(" -> "); }
        }
        return path.toString();
    }
}

class Step {

    private int x;
    private int y;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    Step (int x, int y) {
        this.x = x;
        this.y = y;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getX() { return x; }

    public int getY() { return y; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Step) {
            Step other = (Step) o;

            return (other.x == x) && (other.y == y);
        }

        return false;
    }

}
