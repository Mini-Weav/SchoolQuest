package com.lmweav.schoolquest.characters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.tiles.TileSet;
import com.lmweav.schoolquest.tiles.Tile;
import com.lmweav.schoolquest.utilities.pathfinding.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static com.lmweav.schoolquest.Constants.*;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: GameCharacter
 *
 * This abstract class holds common data for different types of game character.
 *
 * Methods in this class handle both logical and rendering aspects of game character objects.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public abstract class GameCharacter implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final char KEY = '*';

    protected int x;
    protected int y;
    int gX;
    int gY;
    int goalX;
    int goalY;

    int direction;
    protected int speed;
    int pathIndex;

    float animIndex;

    boolean moving;
    protected boolean flip;

    protected String name;

    protected transient Path path;

    protected transient Emotion emotion;

    transient Tile tile;
    protected transient ArrayList<Tile> tiles;

    private transient ArrayList<Bitmap> upSprites1 = new ArrayList<>();
    private transient ArrayList<Bitmap> upSprites2 = new ArrayList<>();
    private transient ArrayList<Bitmap> downSprites1 = new ArrayList<>();
    private transient ArrayList<Bitmap> downSprites2 = new ArrayList<>();
    private transient ArrayList<Bitmap> leftSprites = new ArrayList<>();
    private transient ArrayList<Bitmap> rightSprites = new ArrayList<>();

    transient HashMap<String, ArrayList<Tile>> tileSets = new HashMap<>();


    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public GameCharacter(Context context, int imgId, int x, int y) {
        TileSet tileSet = new TileSet(context, imgId);
        tiles = tileSet.createGameCharacterTileList();

        setAllSprites(tiles);

        this.x = x;
        this.y = y;
        gX = x * SCALED_TILE_SIZE;
        gY = y * SCALED_TILE_SIZE;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public static char getKey() { return KEY; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getGX() { return gX; }

    public int getGY() { return gY; }

    public void setGoal(int x, int y) {
        goalX = x;
        goalY = y;
    }

    public int getDirection() { return direction; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public boolean isMoving() { return moving; }

    public String getName() { return name; }

    private void setAllSprites(ArrayList<Tile> tiles) {
        setSprites(upSprites1, tiles, UP_ANIMATION_1);
        setSprites(upSprites2, tiles, UP_ANIMATION_2);
        setSprites(downSprites1, tiles, DOWN_ANIMATION_1);
        setSprites(downSprites2, tiles, DOWN_ANIMATION_2);
        setSprites(leftSprites, tiles, LEFT_ANIMATION);
        setSprites(rightSprites, tiles, RIGHT_ANIMATION);
    }

    private void setSprites(ArrayList<Bitmap> target, ArrayList<Tile> tiles, int[] indices) {
        target.clear();
        for (int i : indices) { target.add(tiles.get(i).getImage()); }
    }

    public Path getPath() { return path; }
    public void setPath(Path path) {
        this.path = path;
        pathIndex = 1;
        setGoal(path.getX(path.getLength() - 1), path.getY(path.getLength() - 1));
    }

    public Emotion getEmotion() { return emotion; }
    public void setEmotion(int id) {
        if (id > -1) { emotion = new Emotion(id, this); }
        else { emotion = null; }
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public boolean isAtGoal() { return x == goalX && y == goalY; }

    public void changeTile(String key) {
        tiles = tileSets.get(key);
        setAllSprites(tileSets.get(key));
    }

    public void rotate(int direction) {
        this.direction = direction;
        switch (direction) {
            case OBJECT_DIRECTION_UP:
                tile.setImage(upSprites1.get(0));
                break;
            case OBJECT_DIRECTION_DOWN:
                tile.setImage(downSprites1.get(0));
                break;
            case OBJECT_DIRECTION_LEFT:
                tile.setImage(leftSprites.get(0));
                break;
            case OBJECT_DIRECTION_RIGHT:
                tile.setImage(rightSprites.get(0));
                break;
        }
    }

    void walkAnimation(int direction, int index) {
        switch (direction) {
            case OBJECT_DIRECTION_UP:
                if (flip) { tile.setImage(upSprites1.get(index)); }
                else { tile.setImage(upSprites2.get(index)); }
                break;
            case OBJECT_DIRECTION_DOWN:
                if (flip) { tile.setImage(downSprites1.get(index)); }
                else { tile.setImage(downSprites2.get(index)); }
                break;
            case OBJECT_DIRECTION_LEFT:
                tile.setImage(leftSprites.get(index));
                break;
            case OBJECT_DIRECTION_RIGHT:
                tile.setImage(rightSprites.get(index));
                break;
        }
    }

    public void draw(final Canvas canvas, final Paint paint) {
        canvas.drawBitmap(tile.getImage(), gX - GAME.getCamera().getGX(),
                gY - GAME.getCamera().getGY(), paint);
        if (emotion != null && GAME.getMiniGame() == null) {
            GameActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    emotion.draw(canvas, paint);
                }
            });
        }
    }

    public abstract void move();
    public abstract void update() throws NullPointerException;

}
