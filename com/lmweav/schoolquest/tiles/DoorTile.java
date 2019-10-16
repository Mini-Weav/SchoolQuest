package com.lmweav.schoolquest.tiles;


import android.graphics.Bitmap;
import android.util.Pair;

/*
 * School Quest: DoorTile
 * This class is a sub type of tile that is used for doors. Instances of this class can solely be
 * used as an identifier, as the information of each door is stored in a map of DoorTileStructures.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
class DoorTile extends Tile {

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    DoorTile(Bitmap image, char key) {
        super(image, false, key);
    }

    DoorTile(Bitmap image, char key, Pair<Character, Integer> animation) {
        super(image, false, key, animation);
    }

}
