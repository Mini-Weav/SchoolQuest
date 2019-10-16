package com.lmweav.schoolquest.utilities.pathfinding;

import androidx.annotation.NonNull;

import com.lmweav.schoolquest.tiles.TileMap;

import java.util.ArrayList;
import java.util.Collections;

/*
 * School Quest: AStarPathfinder
 * This class is an implementation of the A Star search algorithm.
 *
 * There are 2 support classes declared here: An implementation of A Star's 'node' and a
 * sorted list.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class AStarPathFinder {

    private ArrayList<Node> closed = new ArrayList<>();
    private SortedList<Node> open = new SortedList<>();

    private AStarHeuristic heuristic;
    private TileMap tileMap;

    private int[][] collisionMatrix;
    private Node[][] nodes;

    private int maxSearchDistance = 30;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public AStarPathFinder(TileMap tileMap, int maxSearchDistance) {
        this(tileMap, maxSearchDistance, new ManhattanHeuristic());
    }

    private AStarPathFinder(TileMap tileMap, int maxSearchDistance, AStarHeuristic heuristic) {
        this.heuristic = heuristic;
        this.tileMap = tileMap;
        this.collisionMatrix = tileMap.getCollisionMatrix();
        this.maxSearchDistance = maxSearchDistance;

        nodes = new Node[collisionMatrix.length][collisionMatrix[0].length];
        for (int y = 0; y < collisionMatrix.length; y++) {
            for (int x = 0; x < collisionMatrix[0].length; x++) {
                nodes[y][x] = new Node(x, y);
            }
        }
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public Path findPath(int startX, int startY, int goalX, int goalY) {

        if (goalX < 0 || goalY < 0 || goalX >= collisionMatrix[0].length
                || goalY >= collisionMatrix.length) {
            return null;
        }


        nodes[startY][startX].setCost(0);
        nodes[startY][startX].setDepth();

        closed.clear();
        open.clear();
        open.add(nodes[startY][startX]);

        int maxDepth = 0;
        while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
            Node current = open.getFirst();
            if (current == nodes[goalY][goalX]) { break; }

            open.remove(current);
            closed.add(current);

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if ((x == 0) && (y ==0)) { continue; }

                    if ((x != 0) && (y != 0)) { continue; }

                    int xp = x + current.getX();
                    int yp = y + current.getY();


                    if (isValidLocation(startX, startY, goalX, goalY, xp, yp)) {
                        float nextStepCost = current.getCost() + 1;
                        Node neighbour = nodes[yp][xp];

                        if (nextStepCost < neighbour.getCost()) {
                            open.remove(neighbour);
                            closed.remove(neighbour);
                        }

                        if (!open.contains(neighbour) && !closed.contains(neighbour)) {
                            neighbour.setCost(nextStepCost);
                            neighbour.setHeuristic(heuristic.getCost(xp, yp, goalX, goalY));
                            maxDepth = Math.max(maxDepth, neighbour.setParent(current));
                            open.add(neighbour);
                        }
                    }
                }
            }
        }

        if (nodes[goalY][goalX].getParent() == null) { return null; }

        Path path = new Path();
        Node goal;
        goal = nodes[goalY][goalX];
        while (goal != nodes[startY][startX]) {
            path.prependStep(goal.getX(), goal.getY());
            goal = goal.getParent();
        }
        path.prependStep(startX, startY);
        return path;
    }

    private boolean isValidLocation(int startX, int startY, int goalX, int goalY, int x, int y) {
        boolean invalid = x < 0 || y < 0 || x >= collisionMatrix[0].length
                || y >= collisionMatrix.length;

        if (!invalid && (startX != x || startY != y)) {
            invalid = ((tileMap.isCollidable(x, y) ||
                    tileMap.isDoorPoint(x, y)) && (goalX != x || goalY != y));
        }

        return !invalid;
    }
}

class SortedList<E extends Comparable<E>> extends ArrayList<E> {

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    E getFirst() { return get(0); }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public boolean add(E o) {
        boolean result = super.add(o);
        if (result) { Collections.sort(this); }

        return result;
    }

}

class Node implements Comparable<Node> {

    private int x;
    private int y;
    private int depth;
    private float cost;
    private float heuristic;

    private Node parent;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    Node(int x, int y) {
        this.x = x;
        this.y = y;
        parent = null;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getX() { return x; }

    public int getY() { return y; }

    float getCost() { return cost; }
    void setCost(float cost) { this.cost = cost; }

    void setDepth() { this.depth = 0; }

    void setHeuristic(float heuristic) { this.heuristic = heuristic; }

    public Node getParent() { return parent; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    int setParent(Node parent) {
        depth = parent.depth + 1;
        this.parent = parent;

        return depth;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int compareTo(@NonNull Node o) {

        float f = heuristic + cost;
        float otherF = o.heuristic + o.cost;

        return Float.compare(f, otherF);
    }

}
