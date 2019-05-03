package com.lmweav.schoolquest.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;

import com.lmweav.schoolquest.characters.GameCharacter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: TileSet
 * This class holds the images used for a game character or tile map. A tile map's tile set
 * also holds data used to determine the properties of each tile (such as type, collision,
 * animation, etc.).
 *
 * Methods in this class read the data needed to create the tile set, then creates and maps the
 * tile objects.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class TileSet {

    private Bitmap[] images;
    private ArrayList<ArrayList<String>> tileSetData;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    TileSet(Context context, int imgId, int datId) {
        tileSetData = readTileData(context, datId);
        Bitmap tileset = BitmapFactory.decodeResource(context.getResources(), imgId);
        int rows = tileset.getHeight() / RAW_TILE_SIZE;
        int cols = tileset.getWidth() / RAW_TILE_SIZE;
        images = new Bitmap[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Bitmap rawImage = Bitmap.createBitmap(tileset, j * RAW_TILE_SIZE,
                        i * RAW_TILE_SIZE, RAW_TILE_SIZE, RAW_TILE_SIZE);
                images[(i * cols) + j] = Bitmap.createScaledBitmap(rawImage,
                        SCALED_TILE_SIZE, SCALED_TILE_SIZE, false);
                rawImage.recycle();
            }
        }
    }

    public TileSet(Context context, int imgId) {
        Bitmap tileset = BitmapFactory.decodeResource(context.getResources(), imgId);
        int rows = tileset.getHeight() / RAW_TILE_SIZE;
        int cols = tileset.getWidth() / RAW_TILE_SIZE;
        images = new Bitmap[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Bitmap rawImage = Bitmap.createBitmap(tileset, j * RAW_TILE_SIZE,
                        i * RAW_TILE_SIZE, RAW_TILE_SIZE,
                        RAW_TILE_SIZE);
                images[(i * cols) + j] = Bitmap.createScaledBitmap(rawImage,
                        SCALED_TILE_SIZE, SCALED_TILE_SIZE, false);
            }
        }
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    private static ArrayList<ArrayList<String>> readTileData(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        ArrayList<ArrayList<String>> matrix = new ArrayList<>();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(",");
                ArrayList<String> row = new ArrayList<>(Arrays.asList(split));
                matrix.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    private Tile createTile(ArrayList<String> data, Bitmap image) {
        boolean collision;
        Pair<Character, Integer> animation = null;
        if (data.size() >= 4) {
            String[] animationData = data.get(3).split("\\|");
            Character key = animationData[0].charAt(0);
            Integer tick = Integer.parseInt(animationData[1]);

            animation = new Pair<>(key, tick);
        }
        switch (Integer.parseInt(data.get(0))) {
            case 0:
                collision = Integer.parseInt(data.get(1)) != 0;
                if (animation == null) { return new Tile(image, collision, data.get(2).charAt(0)); }
                else { return new Tile(image, collision, data.get(2).charAt(0), animation); }
            case 1:
                if (animation == null) { return new DoorTile(image, data.get(1).charAt(0)); }
                else { return new DoorTile(image, data.get(1).charAt(0), animation); }
            case 2:
                collision = Integer.parseInt(data.get(1)) != 0;
                if (animation == null) {
                    return new InteractiveTile(image, collision, data.get(2).charAt(0));
                } else {
                    return new InteractiveTile(image, collision, data.get(2).charAt(0), animation);
                }
            default:
                return null;
        }
    }

    private Tile createGameCharacterTile(Bitmap image) {
        return new Tile(image, true, GameCharacter.getKey());
    }

    public ArrayList<Tile> createGameCharacterTileList() {
        ArrayList<Tile> tiles = new ArrayList<>();
        for (Bitmap image : images) {
            tiles.add(createGameCharacterTile(image));
        }
        return tiles;
    }

    HashMap<Character, Tile> mapTiles() {
        HashMap<Character, Tile> tiles = new HashMap<>();
        Tile tile;
        for (int i = 0; i < images.length; i++) {
            try {
                tile = createTile(tileSetData.get(i), images[i]);
            } catch (IndexOutOfBoundsException e) {
                tile = createTile(tileSetData.get(0), images[0]);
            }
            assert tile != null;
            tiles.put(tile.key, tile);
        }
        return tiles;
    }
}
