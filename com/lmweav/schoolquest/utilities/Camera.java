package com.lmweav.schoolquest.utilities;

import android.graphics.Rect;

import static com.lmweav.schoolquest.Constants.*;

import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: Camera
 * This class is a virtual camera that determines what is rendered in the game.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class Camera {

    private int x;
    private int y;
    private int gX;
    private int gY;

    private int diffX = gX - (x * SCALED_TILE_SIZE);
    private int diffY = gY - (y * SCALED_TILE_SIZE);

    private Rect boundingBox;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public Camera(int x, int y) {
        this.x = x;
        this.y = y;
        gX = x * SCALED_TILE_SIZE;
        gY = y * SCALED_TILE_SIZE;

        setBoundingBox();
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getGX() { return gX; }
    public void setGX(int gX) { this.gX = gX; }

    public int getGY() { return gY; }
    public void setGY(int gY) { this.gY = gY; }

    public int getDiffX() { return diffX; }

    public int getDiffY() { return diffY; }

    public Rect getBoundingBox() { return boundingBox; }
    public void setBoundingBox() {
        boundingBox = new Rect(x - X_PADDING, y - Y_PADDING,
                x + CAMERA_WIDTH + X_PADDING, y + CAMERA_HEIGHT + Y_PADDING);
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public void update() {
        diffX = gX - (x * SCALED_TILE_SIZE);
        diffY = gY - (y * SCALED_TILE_SIZE);
    }

    public void reset() {
        this.x = GAME.getPlayer().getX() - (CAMERA_WIDTH / 2);
        this.y = GAME.getPlayer().getY() - (CAMERA_HEIGHT / 2);
        gX = x * SCALED_TILE_SIZE;
        gY = y * SCALED_TILE_SIZE;
    }
}
