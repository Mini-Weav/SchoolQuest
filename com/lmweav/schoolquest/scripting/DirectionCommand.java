package com.lmweav.schoolquest.scripting;

import com.lmweav.schoolquest.characters.GameCharacter;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.characters.Player;
import com.lmweav.schoolquest.tiles.TileMap;
import com.lmweav.schoolquest.utilities.pathfinding.AStarPathFinder;
import com.lmweav.schoolquest.utilities.pathfinding.Path;

import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: DirectionCommand
 *
 * This class is a script command that moves a game character in a specified direction for a number
 * of steps. If the supplied steps is 0, the character rotates to the specified direction.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class DirectionCommand extends Command {

    private Path path;

    private int direction;
    private int speed;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    DirectionCommand(TileMap tileMap, GameCharacter actor, int direction, int speed,
                            int steps) {
        this.actor = actor;
        this.direction = direction;
        this.speed = speed;

        AStarPathFinder pathFinder = new AStarPathFinder(tileMap, 30);

        int destinationX = actor.getX();
        int destinationY = actor.getY();
        switch (direction) {
            case OBJECT_DIRECTION_UP:
                destinationY = actor.getY() - steps;
                break;

            case OBJECT_DIRECTION_DOWN:
                destinationY = actor.getY() + steps;
                break;

            case OBJECT_DIRECTION_LEFT:
                destinationX = actor.getX() - steps;
                break;

            case OBJECT_DIRECTION_RIGHT:
                destinationX = actor.getX() + steps;
                break;
        }
        path = pathFinder.findPath(actor.getX(), actor.getY(), destinationX, destinationY);
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void execute() {
        actor.rotate(direction);
        actor.setSpeed(speed);
        if (path == null) { finished = true; }
        else {
            if (actor instanceof NPC) { ((NPC) actor).movePath(); }
            else if (actor instanceof Player) { actor.move(); }
        }
    }

    @Override
    public void reset() { finished = false; }
}
