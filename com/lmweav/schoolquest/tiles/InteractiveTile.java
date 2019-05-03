package com.lmweav.schoolquest.tiles;

import android.graphics.Bitmap;
import android.util.Pair;

import com.lmweav.schoolquest.characters.Emotion;

import static com.lmweav.schoolquest.Constants.EMOTION_SELL_INDEX;
import static com.lmweav.schoolquest.Constants.EMOTION_SURPRISE_INDEX;

/*
 * School Quest: InteractiveTile
 * This class is a sub type of tile that is used for tiles the play can interactive with. Instances
 * of this class are mainly used as an identifier, as the information of each interaction is stored
 * in a map of InteractiveTileStructures.
 *
 * This class also has an additional Emotion variable, which can be used to draw the player's
 * attention to a certain interaction.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class InteractiveTile extends Tile {

    private Emotion emotion;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    InteractiveTile(Bitmap image, boolean collision, char key) {
        super(image, collision, key);
    }

    InteractiveTile(Bitmap image, boolean collision, char key, Pair<Character, Integer> animation) {
        super(image, collision, key, animation);
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public Emotion getEmotion() { return emotion; }
    private void setEmotion(int id, int x, int y) {
        if (id > -1) { emotion = new Emotion(id, x, y); }
        else { emotion = null; }
    }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/
    void setEvent(String tag, int x, int y) {
        if (tag == null) {
            emotion = null;
            return;
        }
        switch (tag) {
            case "event":
                setEmotion(EMOTION_SURPRISE_INDEX, x, y);
                break;
            case "sell":
                setEmotion(EMOTION_SELL_INDEX, x, y);
                break;
        }
        emotion.turnOffAuto();
    }
}
