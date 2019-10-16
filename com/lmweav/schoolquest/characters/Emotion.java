package com.lmweav.schoolquest.characters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.tiles.Tile;
import com.lmweav.schoolquest.tiles.TileSet;

import java.util.ArrayList;

import static com.lmweav.schoolquest.Game.GAME;
import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: Emotion
 * This class is used to display an emoticon above a game character or tile.
 *
 * Methods in this class handle both logical and rendering aspects of the emotion.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class Emotion {

    private int gX;
    private int gY;

    private int currentFrame = 0;

    private boolean auto = true;

    private Tile tile;
    private GameCharacter character;

    private static ArrayList<Tile> emotions = new ArrayList<>();

    /*--------------------------------------------------------
    Constructors
    --------------------------------------------------------*/

    Emotion(int id, GameCharacter character) {
        tile = emotions.get(id);

        this.gX = character.gX;
        this.gY = character.gY - SCALED_TILE_SIZE;

        this.character = character;
    }

    public Emotion(int id, int x, int y) {
        tile = emotions.get(id);

        this.gX = x * SCALED_TILE_SIZE;
        this.gY = (y - 1) * SCALED_TILE_SIZE;
    }

    /*--------------------------------------------------------
    Getters and Setters
    --------------------------------------------------------*/

    public boolean isAuto() { return auto; }
    public void turnOffAuto() { auto = false; }

    /*--------------------------------------------------------
    Methods
    --------------------------------------------------------*/

    public static void loadEmotions(Context context) {
        TileSet emotionTileSet = new TileSet(context, R.drawable._tilesets_objects_emotion);

        emotions = emotionTileSet.createGameCharacterTileList();
    }

    public static int getEmotionIndex(String key) {
        switch (key) {
            case "surprise":
                return EMOTION_SURPRISE_INDEX;
            case "question":
                return EMOTION_QUESTION_INDEX;
            case "happy":
                return EMOTION_HAPPY_INDEX;
            case "love":
                return EMOTION_LOVE_INDEX;
            case "sick":
                return EMOTION_SICK_INDEX;
            case "sad":
                return EMOTION_SAD_INDEX;
            case "speech":
                return EMOTION_SPEECH_INDEX;
            case "sigh":
                return EMOTION_SIGH_INDEX;
            case "anger":
                return EMOTION_ANGER_INDEX;
            case "distress":
                return EMOTION_DISTRESS_INDEX;
            case "idea":
                return EMOTION_IDEA_INDEX;
            case "thought":
                return EMOTION_THOUGHT_INDEX;
        }
        return -1;
    }

    public void update() {
        if (auto) { currentFrame++; }
        this.gX = character.gX;
        this.gY = character.gY - SCALED_TILE_SIZE;
        if (currentFrame >= EMOTION_FRAMES) { character.emotion = null; }
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(tile.getImage(), gX - GAME.getCamera().getGX(),
                gY - GAME.getCamera().getGY(), paint);
    }
}
