package com.lmweav.schoolquest.scripting;

import com.lmweav.schoolquest.characters.GameCharacter;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.characters.Player;
import com.lmweav.schoolquest.tiles.TileMap;
import com.lmweav.schoolquest.utilities.pathfinding.AStarPathFinder;
import com.lmweav.schoolquest.utilities.pathfinding.Path;

/*
 * School Quest: PathCommand
 *
 * This class is a script command that moves a game character along a path.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class PathCommand extends Command {

    private Path path;

    private int speed;

    private boolean started;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    PathCommand(TileMap tileMap, GameCharacter actor, int startX, int startY,
                       int destinationX, int destinationY, int speed) {
        this.actor = actor;
        this.speed = speed;
        AStarPathFinder pathFinder = new AStarPathFinder(tileMap, 30);
        path = pathFinder.findPath(startX, startY, destinationX, destinationY);
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void execute() {
        actor.setSpeed(speed);
        if (!started) {
            actor.setPath(path);
            started = true;
        }
        else {
            if (actor.getPath() != null) {
                if (actor instanceof NPC) { ((NPC) actor).movePath(); }
                else if (actor instanceof Player) { actor.move(); }
            }
        }
        if (actor.isAtGoal()) {
            finished = true;
            started = false;
        }
    }

    @Override
    public void reset() {
        finished = false;
        started = false;
    }
}
