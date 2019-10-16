package com.lmweav.schoolquest.tiles;

import android.graphics.Bitmap;
import android.util.Pair;

import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: Tile
 * This class holds the data for a logical co-ordinate in the game. Each co-ordinate isn't an
 * instance of this class, rather there is one instance that is looked up via a character key
 * in the tile map.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class Tile {

    protected char key;
    boolean collision;

    protected Bitmap image;

    protected Pair<Character, Integer> animation = null;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    @SafeVarargs
    Tile(Bitmap image, boolean collision, char key, Pair<Character, Integer>... animation) {
        this.image = image;
        this.collision = collision;
        this.key = key;

        if (animation.length > 0) { this.animation = animation[0]; }
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public Bitmap getImage() { return image; }
    public void setImage(Bitmap image) { this.image = image; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/
    void animateTile(TileMap map, int x, int y) {
        if (GAME.getTick() % animation.second == 0) {
            char temp = map.getTileInView(x, y);
            map.setTileInView(x, y, animation.first);
            map.setAnimatedTile(x, y, temp);
        }
    }
}
