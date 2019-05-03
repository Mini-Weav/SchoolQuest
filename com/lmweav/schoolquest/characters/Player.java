package com.lmweav.schoolquest.characters;


import android.content.Context;
import android.graphics.Point;

import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.minigames.LessonC;
import com.lmweav.schoolquest.minigames.MiniGame;
import com.lmweav.schoolquest.tiles.TileMap;
import com.lmweav.schoolquest.tiles.TileSet;
import com.lmweav.schoolquest.utilities.TextBoxStructure;
import com.lmweav.schoolquest.utilities.pathfinding.AStarPathFinder;
import com.lmweav.schoolquest.utilities.pathfinding.Path;
import com.lmweav.schoolquest.tiles.InteractiveTile;
import com.lmweav.schoolquest.tiles.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.lmweav.schoolquest.Game.GAME;
import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: Player
 * This class holds data for the player controlled game character.
 *
 * Methods in this class handle both logical and rendering aspects of the player character.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class Player extends GameCharacter {

    private static final long serialVersionUID = 1L;

    private int responseIndex;
    private int condition;

    private transient Point newGoal;

    private boolean cancelMovement;
    private boolean eaten;

    private int[] buffs = new int[] { -1, -1 };

    private static HashMap<String, Integer> imgIds = new HashMap<>();

    static {
        imgIds.put("default", R.drawable._tilesets_objects_player);
        imgIds.put("pe", R.drawable._tilesets_objects_player_pe);
    }

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public Player(Context context, int x, int y, String name) {
        super(context, R.drawable._tilesets_objects_player, x, y);
        this.name = name;

        for (Map.Entry<String, Integer> imgId : imgIds.entrySet()) {
            TileSet tileSet = new TileSet(context, imgId.getValue());
            ArrayList<Tile> tiles = tileSet.createGameCharacterTileList();

            tileSets.put(imgId.getKey(), tiles);
        }

        tile = tiles.get(OBJECT_TILESET_UP_INDEX);
        pathIndex = 1;
        animIndex = 1;
        speed = 2;
    }

    public Player(Context context, Player player) {
        super(context, R.drawable._tilesets_objects_player, player.x, player.y);
        this.name = player.name;

        for (Map.Entry<String, Integer> imgId : imgIds.entrySet()) {
            TileSet tileSet = new TileSet(context, imgId.getValue());
            ArrayList<Tile> tiles = tileSet.createGameCharacterTileList();

            tileSets.put(imgId.getKey(), tiles);
        }

        pathIndex = 1;
        animIndex = 1;
        direction = player.direction;
        tile = tiles.get(OBJECT_TILESET_UP_INDEX);
        rotate(direction);

        speed = player.speed;
        condition = player.condition;
        eaten = player.eaten;
        buffs[0] = player.buffs[0];
        buffs[1] = player.buffs[1];
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getResponseIndex() { return responseIndex; }
    public void setResponseIndex(int responseIndex) { this.responseIndex = responseIndex; }

    public int getCondition() { return condition; }
    public void setCondition(int condition) { this.condition = condition; }

    public void resetMoving() {
        moving = false;
    }

    public boolean hasEaten() { return eaten; }
    public void setEaten(boolean eaten) { this.eaten = eaten; }

    public int getBuff(int index) {
        if (index > 1) { return -1; }
        return buffs[index];
    }
    public void setBuff(int id, int index) {
        if (index > 1) { return; }
        buffs[index] = id;
    }
    public void resetBuffs() {
        buffs[0] = -1;
        buffs[1] = -1;
    }

    public boolean hasBuff(int id) {
        for (int i: buffs) {
            if (i == id) { return true; }
        }
        return false;
    }

    public void setNewGoal(Point goal) {
        newGoal = goal;
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void move() {

        moving = true;

        setDirection();

        if (path == null) {
            moving = false;
            return;
        }

        final int pathX = path.getX(pathIndex);
        final int pathY = path.getY(pathIndex);

        final TileMap tileMap = GAME.getTileMap();

        Tile tile = tileMap.getTile(pathX, pathY);

        if (GAME.isGameCharacterInMap(pathX, pathY) && !(pathX == goalX && pathY == goalY)
                && GAME.getScript() == null) {
            if (gX / SCALED_TILE_SIZE == pathX && gY / SCALED_TILE_SIZE == pathY) {
                animIndex = 0;
                gX = x * SCALED_TILE_SIZE;
                gY = y * SCALED_TILE_SIZE;
            }
            path = new AStarPathFinder(tileMap, 30).
                    findPath(x, y, goalX, goalY);
            pathIndex = 1;
            if (path != null && pathIndex == path.getLength() - 1) { path = null; }
            if (path == null) {
                rotate(direction);
                moving = false;
                animIndex = 1;
                GAME.setDestination(null);
                return;
            }
            return;
        }

        if (tileMap.isCollidable(pathX, pathY) && GAME.getScript() == null) {
            rotate(direction);
            final MiniGame miniGame = GAME.getMiniGame();
            if (GAME.getMiniGame() != null) {
                if (miniGame instanceof LessonC) {
                    if (((LessonC) miniGame).isCurrentPoint(pathX, pathY)) {
                        GameActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((LessonC) miniGame).foundPoint();
                            }
                        });
                    }
                }
            }
            else {
                if (tile instanceof InteractiveTile || tileMap.isDoorLocked(pathX, pathY)) {
                    GameActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextBoxStructure text;
                            if(GAME.getProgressDataStructure().isCatchInteractiveTile()) {
                                text = new TextBoxStructure(
                                        GAME.getProgressDataStructure().getCatchInteractiveTileText());
                            }
                            else { text = tileMap.getText(pathX, pathY); }
                            if (text == null) { return; }
                            if (text.getText().equals(TEXTBOX_AUTO_RUN)) {
                                text.getRunnable1().run();
                            } else { GameActivity.getInstance().displayTextBox(text); }
                        }
                    });
                }
                else if (GAME.isGameCharacterInMap(pathX, pathY)) {
                    final GameCharacter object = GAME.getGameCharacterFromMap(pathX, pathY);
                    if (object instanceof NPC) {
                        if (((NPC) object).willWait()) {
                            GameActivity.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    object.setEmotion(-1);
                                    ((NPC) object).rotate(GAME.getPlayer());
                                    GameActivity.getInstance().displayTextBox(
                                            ((NPC) object).getText());
                                    if (((NPC) object).getId() < 5 &&
                                            GAME.getFriendScore(((NPC) object).getId()) == 0) {
                                        GameActivity.getInstance().addPoints(FRIEND_INCREASE,
                                                ((NPC) object).getId(), 1);
                                        GAME.increaseFriendScore(((NPC) object).getId(), 1);
                                        GAME.save();
                                    }
                                    ((NPC) object).setWillWait(false);
                                    GAME.setWaitingChar(null);
                                }
                            });
                        }
                    }
                }
                else { moving = false; }
                path = null;
                pathIndex = 1;
                animIndex = 1;
                walkAnimation(direction, 0);
                GAME.setDestination(null);
                cancelMovement = false;
                return;
            }
            resetPath();
            cancelMovement = false;
            return;
        }

        MiniGame miniGame = GAME.getMiniGame();

        switch (direction) {
            case OBJECT_DIRECTION_UP:
                GAME.getCamera().setGY(GAME.getCamera().getGY() - (SCALED_TILE_SIZE / TILE_FACTOR)
                        * speed);
                gY -= (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) {
                    animIndex = animIndex % 4;
                    flip = !flip;
                }

                if (gY % SCALED_TILE_SIZE == 0) {
                    GAME.getCamera().setY(GAME.getCamera().getY() - 1);
                    GAME.getCamera().setBoundingBox();
                    GAME.getTileMap().setCollision(x, y, 0);
                    GAME.removeGameCharacterFromMap(this);
                    y -= 1;
                    if (miniGame != null) {
                        if (miniGame instanceof LessonC) {
                            ((LessonC) miniGame).step();
                        }
                    }
                    GAME.getTileMap().setCollision(x, y, 2);
                    GAME.addGameCharacterToMap(this);
                    pathIndex++;
                    if (cancelMovement) { resetPath(); }
                    tileMap.update();
                    cancelMovement = false;
                    if (gY != y * SCALED_TILE_SIZE) { y = gY / SCALED_TILE_SIZE; }
                }
                break;
            case OBJECT_DIRECTION_DOWN:
                GAME.getCamera().setGY(GAME.getCamera().getGY() + (SCALED_TILE_SIZE / TILE_FACTOR)
                        * speed);
                gY += (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) {
                    animIndex = animIndex % 4;
                    flip = !flip;
                }

                if (gY % SCALED_TILE_SIZE == 0) {
                    GAME.getCamera().setY(GAME.getCamera().getY() + 1);
                    GAME.getCamera().setBoundingBox();
                    GAME.getTileMap().setCollision(x, y, 0);
                    GAME.removeGameCharacterFromMap(this);
                    y += 1;
                    if (miniGame != null) {
                        if (miniGame instanceof LessonC) {
                            ((LessonC) miniGame).step();
                        }
                    }
                    GAME.getTileMap().setCollision(x, y, 2);
                    GAME.addGameCharacterToMap(this);
                    pathIndex++;
                    if (cancelMovement) { resetPath(); }
                    tileMap.update();
                    cancelMovement = false;
                    if (gY != y * SCALED_TILE_SIZE) { y = gY / SCALED_TILE_SIZE; }
                }
                break;
            case OBJECT_DIRECTION_LEFT:
                GAME.getCamera().setGX(GAME.getCamera().getGX() - (SCALED_TILE_SIZE / TILE_FACTOR)
                        * speed);
                gX -= (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) { animIndex = animIndex % 4; }

                if (gX % SCALED_TILE_SIZE == 0) {
                    GAME.getCamera().setX(GAME.getCamera().getX() - 1);
                    GAME.getCamera().setBoundingBox();
                    GAME.getTileMap().setCollision(x, y, 0);
                    GAME.removeGameCharacterFromMap(this);
                    x -= 1;
                    if (miniGame != null) {
                        if (miniGame instanceof LessonC) {
                            ((LessonC) miniGame).step();
                        }
                    }
                    GAME.getTileMap().setCollision(x, y, 2);
                    GAME.addGameCharacterToMap(this);
                    pathIndex++;
                    if (cancelMovement) { resetPath(); }
                    tileMap.update();
                    cancelMovement = false;
                    if (gX != x * SCALED_TILE_SIZE) { x = gX / SCALED_TILE_SIZE; }
                }
                break;
            case OBJECT_DIRECTION_RIGHT:
                GAME.getCamera().setGX(GAME.getCamera().getGX() + (SCALED_TILE_SIZE / TILE_FACTOR)
                        * speed);
                gX += (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) { animIndex = animIndex % 4; }

                if (gX % SCALED_TILE_SIZE == 0) {
                    GAME.getCamera().setX(GAME.getCamera().getX() + 1);
                    GAME.getCamera().setBoundingBox();
                    GAME.getTileMap().setCollision(x, y, 0);
                    GAME.removeGameCharacterFromMap(this);
                    x += 1;
                    if (miniGame != null) {
                        if (miniGame instanceof LessonC) {
                            ((LessonC) miniGame).step();
                        }
                    }
                    GAME.getTileMap().setCollision(x, y, 2);
                    GAME.addGameCharacterToMap(this);
                    pathIndex++;
                    if (cancelMovement) { resetPath(); }
                    tileMap.update();
                    cancelMovement = false;
                    if (gX != x * SCALED_TILE_SIZE) { x = gX / SCALED_TILE_SIZE; }
                }
                break;
        }

        if (path == null && newGoal != null) {
            AStarPathFinder pathFinder =
                    new AStarPathFinder(GAME.getTileMap(), 30);
            Path path = pathFinder.findPath(GAME.getPlayer().getX(), GAME.getPlayer().getY(),
                    newGoal.x, newGoal.y);

            if (path != null) {
                if (GAME.isGameCharacterInMap(newGoal.x, newGoal.y)) {
                    GameCharacter object = GAME.getGameCharacterFromMap(newGoal.x, newGoal.y);
                    if (object instanceof NPC) { ((NPC) object).setWillWait(true);}
                }
                GAME.playSFX(SFX_MOVE);
                GAME.setDestination(newGoal);
                GAME.getPlayer().setGoal(newGoal.x, newGoal.y);
                GAME.getPlayer().setPath(path);
                pathIndex = 1;
                animIndex = 1;
                moving = true;
            } else { GAME.playSFX(SFX_CLICK); }
        }

        if (path != null && pathIndex == path.getLength()) {
            resetPath();
            GAME.setDestination(null);
        }

        if (GAME.isGameCharacterInMap(gX / SCALED_TILE_SIZE, gY / SCALED_TILE_SIZE) &&
                !(GAME.getGameCharacterFromMap(gX / SCALED_TILE_SIZE, gY / SCALED_TILE_SIZE)
                        instanceof Player)) {
            switch (direction) {
                case OBJECT_DIRECTION_UP:
                    gX += gX % SCALED_TILE_SIZE;
                    break;
                case OBJECT_DIRECTION_DOWN:
                    gX -= gX % SCALED_TILE_SIZE;
                    break;
                case OBJECT_DIRECTION_LEFT:
                    gY += gY % SCALED_TILE_SIZE;
                    break;
                case OBJECT_DIRECTION_RIGHT:
                    gY -= gY % SCALED_TILE_SIZE;
                    break;
            }
            GAME.getCamera().reset();
        }
    }

    public void cancelMovement() {
        if (!moving) { return; }
        cancelMovement = true;
        newGoal = null;
    }

    public void setPoint(int x, int y) {
        this.x = x;
        this.y = y;
        gX = x * SCALED_TILE_SIZE;
        gY = y * SCALED_TILE_SIZE;

        path = null;
        pathIndex = 1;
        animIndex = 1;
        cancelMovement = false;
        moving = false;
        emotion = null;
        GAME.getCamera().reset();
    }

    public void resetPath() {
        path = null;
        pathIndex = 1;
        moving = false;
        animIndex = 1;
        walkAnimation(direction, 0);
        GAME.setDestination(null);
    }

    private void setDirection() {

        int x = path.getX(pathIndex);
        int y = path.getY(pathIndex);

        if (this.x == x) {
            if (this.y > y) { direction = OBJECT_DIRECTION_UP; }
            else if (this.y < y) { direction = OBJECT_DIRECTION_DOWN; }
            return;
        }
        if (this.y == y) {
            if (this.x > x) { direction = OBJECT_DIRECTION_LEFT; }
            else { direction = OBJECT_DIRECTION_RIGHT; }
        }
    }

    @Override
    public void update() {
        if (GAME.isPlayerSpottedByNPC()) {
            resetPath();
            GAME.setDestination(null);
        }
        else {
            if (path != null) {
                move();
                GameActivity.getInstance().enableCancelButton();
            } else {
                GameActivity.getInstance().disableCancelButton();
            }
        }
    }
}
