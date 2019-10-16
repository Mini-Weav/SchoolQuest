package com.lmweav.schoolquest.characters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;

import com.lmweav.schoolquest.Game;
import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.controllers.Action;
import com.lmweav.schoolquest.controllers.Controller;
import com.lmweav.schoolquest.controllers.Patrol;
import com.lmweav.schoolquest.items.Item;
import com.lmweav.schoolquest.items.ItemComparator;
import com.lmweav.schoolquest.minigames.LessonA;
import com.lmweav.schoolquest.minigames.LessonB;
import com.lmweav.schoolquest.minigames.LessonC;
import com.lmweav.schoolquest.scripting.Script;
import com.lmweav.schoolquest.utilities.ExpressionAnalyser;
import com.lmweav.schoolquest.utilities.TextBoxRunnable;
import com.lmweav.schoolquest.utilities.TextBoxStructure;
import com.lmweav.schoolquest.tiles.Tile;
import com.lmweav.schoolquest.tiles.TileMap;
import com.lmweav.schoolquest.tiles.TileSet;
import com.lmweav.schoolquest.utilities.pathfinding.AStarPathFinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.lmweav.schoolquest.Constants.*;

import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: NPC
 * This class holds data for computer controlled game characters.
 *
 * Methods in this class handle both logical and rendering aspects of NPCs, as well as reading
 * external resources needed for the creation of NPC objects.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class NPC extends GameCharacter {

    private static final long serialVersionUID = 1L;

    private static SparseArray<String> names;
    private static SparseArray<ArrayList<NPCItemStructure>> npcItems;
    private static HashMap<String, NPCDataStructure> npcData = new HashMap<>();

    private int id;
    private int defaultDirection;
    private int defaultX;
    private int defaultY;
    private int itemResponseIndex;

    private boolean hostile;
    private boolean waiting;
    private boolean willWait;
    private transient boolean spottedPlayer;

    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;

    private String code;

    private Controller ctrl;

    private transient Bitmap textBoxImg;

    private transient HashMap<String, TextBoxStructure> texts = new LinkedHashMap<>();
    private transient HashMap<Item, String> shopItems;
    private transient HashMap<Item, Integer> itemScores;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public NPC(Context context, int x, int y, NPCDataStructure data, String name, String code,
               int defaultDirection, boolean hostile, Controller ctrl) {
        super(context, data.getImgId(), x, y);
        this.id = data.getNpcId();
        this.defaultDirection = defaultDirection;
        this.direction = defaultDirection;
        this.defaultX = x;
        this.defaultY = y;

        this.name = name;
        this.code = code;

        for (Map.Entry<String, Integer> imgId : data.getImgIds().entrySet()) {
            TileSet tileSet = new TileSet(context, imgId.getValue());
            ArrayList<Tile> tiles = tileSet.createGameCharacterTileList();

            tileSets.put(imgId.getKey(), tiles);
        }

        this.hostile = hostile;
        this.ctrl = ctrl;

        if (ctrl instanceof Patrol) { ((Patrol) ctrl).setObject(this);}

        int tileIndex = 0;
        switch (direction) {
            case OBJECT_DIRECTION_UP:
                tileIndex = OBJECT_TILESET_UP_INDEX;
                break;
            case OBJECT_DIRECTION_DOWN:
                tileIndex = OBJECT_TILESET_DOWN_INDEX;
                break;
            case OBJECT_DIRECTION_LEFT:
                tileIndex = OBJECT_TILESET_LEFT_INDEX;
                break;
            case OBJECT_DIRECTION_RIGHT:
                tileIndex = OBJECT_TILESET_RIGHT_INDEX;
                break;
        }

        setTexts(context, data.getTxtId());
        initItems();

        if (data.getShopId() > -1) { readNPCShop(context, data.getShopId());}


        tile = tiles.get(tileIndex);
        animIndex = 1;
        speed = 1;

        int textBoxImgId = data.getTextBoxImgId();

        if (textBoxImgId > -1) {
            textBoxImg = BitmapFactory.decodeResource(context.getResources(), textBoxImgId);
        } else {
            textBoxImg = null;
        }
    }

    public NPC(Context context, NPC npc) {
        super(context, npcData.get(npc.code).getImgId(), npc.x, npc.y);
        NPCDataStructure data = npcData.get(npc.code);
        id = data.getNpcId();
        defaultDirection = npc.defaultDirection;
        direction = npc.direction;
        defaultX = x;
        defaultY = y;

        name = npc.name;
        code = npc.code;

        for (Map.Entry<String, Integer> imgId : data.getImgIds().entrySet()) {
            TileSet tileSet = new TileSet(context, imgId.getValue());
            ArrayList<Tile> tiles = tileSet.createGameCharacterTileList();

            tileSets.put(imgId.getKey(), tiles);
        }

        hostile = npc.hostile;
        ctrl = npc.ctrl;

        if (ctrl instanceof Patrol) { ((Patrol) ctrl).setObject(this);}

        int tileIndex = 0;
        switch (direction) {
            case OBJECT_DIRECTION_UP:
                tileIndex = OBJECT_TILESET_UP_INDEX;
                break;
            case OBJECT_DIRECTION_DOWN:
                tileIndex = OBJECT_TILESET_DOWN_INDEX;
                break;
            case OBJECT_DIRECTION_LEFT:
                tileIndex = OBJECT_TILESET_LEFT_INDEX;
                break;
            case OBJECT_DIRECTION_RIGHT:
                tileIndex = OBJECT_TILESET_RIGHT_INDEX;
                break;
        }

        setTexts(context, data.getTxtId());
        initItems();

        if (data.getShopId() > -1) { readNPCShop(context, data.getShopId());}


        tile = tiles.get(tileIndex);
        animIndex = 1;
        speed = 1;

        int textBoxImgId = data.getTextBoxImgId();

        if (textBoxImgId > -1) {
            textBoxImg = BitmapFactory.decodeResource(context.getResources(), textBoxImgId);
        } else {
            textBoxImg = null;
        }
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public static String getName(int index) { return names.get(index); }

    public static NPCDataStructure getData(String key) { return npcData.get(key); }

    public int getId() { return id; }

    public int getItemResponseIndex() { return itemResponseIndex; }

    boolean willWait() { return willWait; }
    public void setWillWait(boolean willWait) { this.willWait = willWait; }

    public Bitmap getTextBoxImg() { return textBoxImg; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public static void initialiseNPCData() {
        npcData.put("athlete", new NPCDataStructure(ATHLETE_INDEX,
                R.drawable._tilesets_objects_friend_athlete, R.raw._texts_npcs_athlete, NO_DATA,
                R.drawable._ui_friend_athlete,
                new Pair<>("pe", R.drawable._tilesets_objects_friend_athlete_pe)));
        npcData.put("classmate", new NPCDataStructure(CLASSMATE_INDEX,
                R.drawable._tilesets_objects_friend_classmate, R.raw._texts_npcs_classmate,
                NO_DATA, R.drawable._ui_friend_classmate));
        npcData.put("nerd", new NPCDataStructure(NERD_INDEX,
                R.drawable._tilesets_objects_friend_nerd, R.raw._texts_npcs_nerd,
                NO_DATA, R.drawable._ui_friend_nerd));
        npcData.put("delinquent", new NPCDataStructure(DELINQUENT_INDEX,
                R.drawable._tilesets_objects_friend_delinquent, R.raw._texts_npcs_delinquent,
                R.raw._shop_delinquent, R.drawable._ui_friend_delinquent));
        npcData.put("tutee", new NPCDataStructure(TUTEE_INDEX,
                R.drawable._tilesets_objects_friend_tutee, R.raw._texts_npcs_tutee, NO_DATA,
                R.drawable._ui_friend_tutee));

        npcData.put("dt", new NPCDataStructure(DT_TEACHER_INDEX,
                R.drawable._tilesets_objects_teacher_dt, R.raw._texts_npcs_teacher, NO_DATA,
                NO_DATA));
        npcData.put("ft", new NPCDataStructure(FT_TEACHER_INDEX,
                R.drawable._tilesets_objects_teacher_ft, R.raw._texts_npcs_teacher, NO_DATA,
                NO_DATA));
        npcData.put("pe", new NPCDataStructure(PE_TEACHER_INDEX,
                R.drawable._tilesets_objects_teacher_pe, R.raw._texts_npcs_teacher, NO_DATA,
                NO_DATA));
        npcData.put("chemistry", new NPCDataStructure(CHEM_TEACHER_INDEX,
                R.drawable._tilesets_objects_teacher_science, R.raw._texts_npcs_teacher, NO_DATA,
                NO_DATA));
        npcData.put("ict", new NPCDataStructure(ICT_TEACHER_INDEX,
                R.drawable._tilesets_objects_teacher_ict, R.raw._texts_npcs_teacher, NO_DATA,
                NO_DATA));

        npcData.put("boy", new NPCDataStructure(BOY_INDEX,
                R.drawable._tilesets_objects_student_boy, R.raw._texts_npcs_static, NO_DATA,
                NO_DATA, new Pair<>("pe", R.drawable._tilesets_objects_student_boy_pe)));
        npcData.put("girl", new NPCDataStructure(GIRL_INDEX,
                R.drawable._tilesets_objects_student_girl, R.raw._texts_npcs_static, NO_DATA,
                NO_DATA));
        npcData.put("woman", new NPCDataStructure(WOMAN_INDEX,
                R.drawable._tilesets_objects_dinner_lady, R.raw._texts_npcs_woman,
                R.raw._shop_woman, NO_DATA));

        npcData.put("man", new NPCDataStructure(MAN_INDEX,
                R.drawable._tilesets_objects_shopkeeper, R.raw._texts_npcs_man,
                R.raw._shop_man, NO_DATA));
    }

    private boolean isValidLocation(int x, int y) {
        TileMap tileMap = GAME.getTileMap();
        boolean invalid = x < 0 || y < 0 || x >= tileMap.getCols() || y >= tileMap.getRows()
                || tileMap.isCollidable(x, y) || tileMap.isDoorPoint(x, y)
                || (x == GAME.getPlayer().x && y == GAME.getPlayer().y);

        if (!(ctrl instanceof Patrol) && !invalid) {
            invalid = x > defaultX + 4 || x < defaultX - 4 || y > defaultY + 4 || y < defaultY - 4;
        }

        return !invalid;
    }

    public void reset() {
        x = defaultX;
        y = defaultY;
        gX = x * SCALED_TILE_SIZE;
        gY = y * SCALED_TILE_SIZE;
        direction = defaultDirection;
        rotate(direction);
        if (ctrl instanceof Patrol) { ((Patrol) ctrl).reset(); }
        emotion = null;
        path = null;
        pathIndex = 1;
        animIndex = 1;
    }

    private void initItems() {
        ArrayList<NPCItemStructure> itemStructures = npcItems.get(id);
        if (itemStructures == null) { return; }
        itemScores = new LinkedHashMap<>();
        for (NPCItemStructure itemStructure: itemStructures) {
            itemScores.put(itemStructure.getItem(), itemStructure.getScore());
        }
    }

    public boolean canGive() {
        if (GAME.givenTo(this)) { return false; }
        for (Item item: itemScores.keySet()) {
            if (GAME.hasItem(item)) { return true; }
        }
        return false;
    }

    public boolean canGive(Item item) {
        return itemScores.containsKey(item);
    }

    public void give(Item item) {
        final NPC npc = this;
        boolean accepted = true;
        final int oldPoint = GAME.getFriendScore(id);

        itemResponseIndex = itemScores.get(item);

        final boolean gf;

        if (itemResponseIndex == 5) {
            if (GAME.getFriendScore(id) >= 20) {
                GameActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GameActivity.getInstance().addPoints(FRIEND_INCREASE,
                                id, itemResponseIndex);
                    }
                });
                GAME.increaseFriendScore(id, itemResponseIndex);
                GAME.setGfIndex(id);
                GAME.save();
                gf = true;
            }
            else {
                accepted = false;
                gf = false;
            }
        }
        else {
            GameActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GameActivity.getInstance().addPoints(FRIEND_INCREASE,
                            id, itemResponseIndex);
                }
            });
            GAME.increaseFriendScore(id, itemResponseIndex);
            GAME.save();
            gf = false;
        }

        final int newPoint = GAME.getFriendScore(id);

        switch (itemResponseIndex) {
            case 1:
            case 3:
                setEmotion(EMOTION_HAPPY_INDEX);
                break;
            case 5:
                if (accepted) { setEmotion(EMOTION_LOVE_INDEX); }
                else { setEmotion(EMOTION_SAD_INDEX); }
                break;
            default:
        }

        if (accepted) {
            GAME.removeItem(item);
            GAME.addGivenNPC(this);
        }
        GameActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (gf) {
                    final Runnable gf = new Runnable() {
                        @Override
                        public void run() {
                            GAME.playJingle(R.raw._jingle_rank_up);
                            GameActivity.getInstance().displayTextBox(
                                    new TextBoxStructure("> You are now in a relationship with " +
                                            name + "!"));
                        }
                    };
                    GameActivity.getInstance().displayTextBox(new TextBoxStructure(
                            npc.getText().getText(), gf, true, null
                    ));
                }
                if (oldPoint < 20 && newPoint >= 20) {
                    final Runnable maxFP = new Runnable() {
                        @Override
                        public void run() {
                            GAME.playJingle(R.raw._jingle_rank_up);
                            GameActivity.getInstance().displayTextBox(
                                    new TextBoxStructure("> You are now good friends with " +
                                            name + "!"));
                        }
                    };
                    GameActivity.getInstance().displayTextBox(new TextBoxStructure(
                            npc.getText().getText(), maxFP, true, null
                    ));
                } else {
                    GameActivity.getInstance().displayTextBox(npc.getText());
                }
            }
        });
        itemResponseIndex = 0;
    }

    public boolean hasTextBoxImg() { return textBoxImg != null; }

    @Override
    public void move() {
        if (movingUp) {
            if (gY % SCALED_TILE_SIZE == 0) {
                GAME.getTileMap().setCollision(x, y, 0);
                GAME.removeGameCharacterFromMap(this);
                y -= 1;
                GAME.getTileMap().setCollision(x, y, 2);
                GAME.addGameCharacterToMap(this);
            }
            gY -= (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

            walkAnimation(direction, (int) animIndex);
            animIndex += speed;
            if (animIndex > 3) {
                animIndex = animIndex % 4;
                flip = !flip;
            }

            if (gY % SCALED_TILE_SIZE == 0) { movingUp = false; }
        }
        else if (movingDown) {
            if (gY % SCALED_TILE_SIZE == 0) {
                GAME.getTileMap().setCollision(x, y, 0);
                GAME.removeGameCharacterFromMap(this);
                y += 1;
                GAME.getTileMap().setCollision(x, y, 2);
                GAME.addGameCharacterToMap(this);
            }
            gY += (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

            walkAnimation(direction, (int) animIndex);
            animIndex += speed;
            if (animIndex > 3) {
                animIndex = animIndex % 4;
                flip = !flip;
            }

            if (gY % SCALED_TILE_SIZE == 0) { movingDown = false; }
        }
        else if (movingLeft) {
            if (gX % SCALED_TILE_SIZE == 0) {
                GAME.getTileMap().setCollision(x, y, 0);
                GAME.removeGameCharacterFromMap(this);
                x -= 1;
                GAME.getTileMap().setCollision(x, y, 2);
                GAME.addGameCharacterToMap(this);
            }
            gX -= (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

            walkAnimation(direction, (int) animIndex);
            animIndex += speed;
            if (animIndex > 3) { animIndex = animIndex % 4; }

            if (gX % SCALED_TILE_SIZE == 0) { movingLeft = false; }
        }
        else if (movingRight) {
            if (gX % SCALED_TILE_SIZE == 0) {
                GAME.getTileMap().setCollision(x, y, 0);
                GAME.removeGameCharacterFromMap(this);
                x += 1;
                GAME.getTileMap().setCollision(x, y, 2);
                GAME.addGameCharacterToMap(this);
            }
            gX += (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

            walkAnimation(direction, (int) animIndex);
            animIndex += speed;
            if (animIndex > 3) { animIndex = animIndex % 4; }

            if (gX % SCALED_TILE_SIZE == 0) { movingRight = false; }
        }
    }

    public void movePath() {
        if (emotion != null) { return; }
        moving = true;

        setDirection();

        if (path == null) { return; }

        final int pathX = path.getX(pathIndex);
        final int pathY = path.getY(pathIndex);

        final TileMap tileMap = GAME.getTileMap();

        if (GAME.isGameCharacterInMap(pathX, pathY) && !(pathX == goalX && pathY == goalY)
                && GAME.getScript() == null) {
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

        if (tileMap.isCollidable(pathX, pathY)  && GAME.getScript() == null) {
            rotate(direction);

            path = null;
            pathIndex = 1;
            moving = false;
            animIndex = 1;
            walkAnimation(direction, 0);
            GAME.setDestination(null);

            return;
        }

        switch (direction) {
            case OBJECT_DIRECTION_UP:
                gY -= (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) {
                    animIndex = animIndex % 4;
                    flip = !flip;
                }

                if (gY % SCALED_TILE_SIZE == 0) {
                    tileMap.update();
                    y -= 1;
                    pathIndex++;
                }
                break;
            case OBJECT_DIRECTION_DOWN:
                gY += (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) {
                    animIndex = animIndex % 4;
                    flip = !flip;
                }

                if (gY % SCALED_TILE_SIZE == 0) {
                    tileMap.update();
                    y += 1;
                    pathIndex++;
                }
                break;
            case OBJECT_DIRECTION_LEFT:
                gX -= (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) { animIndex = animIndex % 4; }

                if (gX % SCALED_TILE_SIZE == 0) {
                    tileMap.update();
                    pathIndex++;
                    x -= 1;
                }
                break;
            case OBJECT_DIRECTION_RIGHT:
                gX += (SCALED_TILE_SIZE / TILE_FACTOR) * speed;

                walkAnimation(direction, (int) animIndex);
                animIndex += (float) speed / 2;
                if (animIndex > 3) { animIndex = animIndex % 4; }

                if (gX % SCALED_TILE_SIZE == 0) {
                    tileMap.update();
                    pathIndex++;
                    x += 1;
                }
                break;
        }

        if (pathIndex == path.getLength()) {
            path = null;
            pathIndex = 1;
            moving = false;
            animIndex = 1;
        }
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
            if (this.x > x) { direction = OBJECT_DIRECTION_LEFT;
            } else {
                direction = OBJECT_DIRECTION_RIGHT;
            }
        }
    }

    protected void rotate(Player player) {
        if (player.x != x) {
            if (player.x < x) { rotate(OBJECT_DIRECTION_LEFT); }
            else { rotate(OBJECT_DIRECTION_RIGHT); }
        }
        if (player.y != y) {
            if (player.y < y) { rotate(OBJECT_DIRECTION_UP); }
            else { rotate(OBJECT_DIRECTION_DOWN); }
        }
    }

    public TextBoxStructure getText() {
        for (Map.Entry<String, TextBoxStructure> entry : texts.entrySet()) {
            if (ExpressionAnalyser.analyse(entry.getKey(), this)) { return entry.getValue(); }
        }
        return null;
    }

    public boolean hasEvent() {
        try {
            return GAME.getScript() == null && getText().getTag() != null &&
                    GameActivity.getInstance().findViewById(R.id.textbox).getVisibility() == View.GONE;
        } catch (NullPointerException e) {
            return false;
        }

    }
    public void setEvent() {
        switch (getText().getTag()) {
            case "event":
                setEmotion(EMOTION_SURPRISE_INDEX);
                break;
            case "sell":
                setEmotion(EMOTION_SELL_INDEX);
                break;
        }
        emotion.turnOffAuto();
    }

    private void setHostileTiles(int direction) {
        switch (direction) {
            case OBJECT_DIRECTION_UP:
                for (int i = y - 1; i > y - 4; i--) {
                    try {
                        if (GAME.getTileMap().isCollidable(x, i) ||
                                GAME.getTileMap().isDoorPoint(x, i)) {
                            if (GAME.getPlayer().x == x && GAME.getPlayer().y == i) { spot(); }
                            else { break; }
                        }
                        GAME.getTileMap().setHostileTile(x, i);
                    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                        //Ignore empty tiles
                    }
                }
                break;
            case OBJECT_DIRECTION_DOWN:
                for (int i = y + 1; i < y + 4; i++) {
                    try {
                        if (GAME.getTileMap().isCollidable(x, i) ||
                                GAME.getTileMap().isDoorPoint(x, i)) {
                            if (GAME.getPlayer().x == x && GAME.getPlayer().y == i) { spot();}
                            else { break; }
                        }
                        GAME.getTileMap().setHostileTile(x, i);
                    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                        //Ignore empty tiles
                    }
                }
                break;
            case OBJECT_DIRECTION_LEFT:
                for (int i = x - 1; i > x - 4; i--) {
                    try {
                        if (GAME.getTileMap().isCollidable(i, y) ||
                                GAME.getTileMap().isDoorPoint(i, y)) {
                            if (GAME.getPlayer().y == y && GAME.getPlayer().x == i) { spot(); }
                            else { break; }
                        }
                        GAME.getTileMap().setHostileTile(i, y);
                    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                        //Ignore empty tiles
                    }
                }
                break;
            case OBJECT_DIRECTION_RIGHT:
                for (int i = x + 1; i < x + 4; i++) {
                    try {
                        if (GAME.getTileMap().isCollidable(i, y) ||
                                GAME.getTileMap().isDoorPoint(i, y)) {
                            if (GAME.getPlayer().y == y && GAME.getPlayer().x == i) { spot(); }
                            else { break; }
                        }
                        GAME.getTileMap().setHostileTile(i, y);
                    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                        //Ignore empty tiles
                    }
                }
                break;
        }
    }

    private void spot() {
        GAME.setPlayerSpottedByNPC(true);
        spottedPlayer = true;

        setEmotion(EMOTION_SURPRISE_INDEX);

        path = new AStarPathFinder(GAME.getTileMap(), 30).findPath(x, y,
                GAME.getPlayer().x, GAME.getPlayer().y);
    }

    private void setTexts(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);

        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException();
        }
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replace("\\Name", ((GameActivity) context).getPlayerName()).
                        replace("\\NPC-Name", name).replace("\\n", "\n");
                String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");

                String[] keyData = split[0].split(";");

                String conditions = keyData[0];
                String tag = null;
                if (keyData.length > 1) { tag = keyData[1]; }

                String[] textData = split[1].split("\\|");
                String text;
                final NPC npc;
                final NPC thisNPC = this;
                if (textData.length > 1 && textData[0].equals("notice")) {
                    npc = null;
                    text = textData[1];
                }
                else {
                    npc = this;
                    text = textData[0];
                }

                if (split.length < 3) {
                    texts.put(conditions, new TextBoxStructure(text, npc));
                } else {
                    final String[] runnableData = split[2].split("\\|");

                    final String[] emotionData;
                    if (split.length > 3) { emotionData = split[3].split("\\|"); }
                    else { emotionData = null; }

                    Runnable runnable1, runnable2;
                    final int npcId = this.id;
                    switch (runnableData[0]) {
                        case "dialogue":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.getPlayer().setResponseIndex(
                                            Integer.parseInt(runnableData[3]));
                                    GameActivity.getInstance().displayTextBox(getText());
                                    GAME.getPlayer().setResponseIndex(0);
                                    if (emotionData != null) {
                                        setEmotion(Emotion.getEmotionIndex(emotionData[0]));
                                    }
                                }
                            };
                            runnable2 = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.getPlayer().setResponseIndex(
                                            Integer.parseInt(runnableData[4]));
                                    GameActivity.getInstance().displayTextBox(getText());
                                    GAME.getPlayer().setResponseIndex(0);
                                    if (emotionData != null && emotionData.length > 1) {
                                        setEmotion(Emotion.getEmotionIndex(emotionData[1]));
                                    }
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnableData[1],
                                    runnableData[2], runnable1, runnable2, npc));
                            break;

                        case "dialogue_1":
                            final String responseId = runnableData[3];
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.getPlayer().setResponseIndex(Integer.parseInt(responseId));
                                    GameActivity.getInstance().displayTextBox(getText());
                                    GAME.getPlayer().setResponseIndex(0);
                                    if (emotionData != null) {
                                        setEmotion(Emotion.getEmotionIndex(emotionData[0]));
                                    }
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnableData[1],
                                    runnableData[2], runnable1, null, npc));
                            break;


                        case "activity":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    final int pointIncrease;
                                    final int oldPoint = GAME.getFriendScore(npcId);
                                    Boolean heist = Boolean.parseBoolean(runnableData[1]);
                                    setEmotion(EMOTION_HAPPY_INDEX);
                                    switch (npcId) {
                                        case ATHLETE_INDEX:
                                            GAME.loadCutscene(
                                                    Script.getScript(TRACK_CLUB_CUTSCENE));
                                            pointIncrease = (GAME.getGradeScore(PE_INDEX) / 10) + 1;
                                            break;
                                        case NERD_INDEX:
                                            if (heist) {
                                                GAME.loadCutscene(
                                                        Script.getScript(CHEMISTRY_HEIST_CUTSCENE));
                                                GAME.addItem(Item.getItem(KEY3_INDEX));
                                            }
                                            else {
                                                GAME.loadCutscene(
                                                        Script.getScript(CHEMISTRY_CUTSCENE));
                                                switch (GAME.getGradeScore(CHEMISTRY_INDEX) / 10) {
                                                    case 1:
                                                        GAME.addItem(Item.getItem(DRINK0_INDEX));
                                                        break;
                                                    case 2:
                                                        GAME.addItem(Item.getItem(DRINK1_INDEX));
                                                        break;
                                                    default:
                                                        GAME.addItem(Item.getItem(DRINK2_INDEX));
                                                        break;
                                                }
                                            }
                                            pointIncrease = (GAME.getGradeScore(
                                                    CHEMISTRY_INDEX) / 10) + 1;
                                            break;
                                        case DELINQUENT_INDEX:
                                            GAME.loadCutscene(
                                                    Script.getScript(DT_HEIST_CUTSCENE));
                                            GAME.addItem(Item.getItem(KEY0_INDEX));
                                            pointIncrease = (GAME.getGradeScore(DT_INDEX) / 10) + 1;
                                            break;
                                        case TUTEE_INDEX:
                                            if (heist) {
                                                GAME.loadCutscene(
                                                        Script.getScript(TUTORING_HEIST_CUTSCENE));
                                                GAME.addItem(Item.getItem(KEY4_INDEX));
                                            }
                                            else {
                                                GAME.loadCutscene(
                                                        Script.getScript(TUTORING_CUTSCENE));
                                            }
                                            pointIncrease = (GAME.getGradeScore(ICT_INDEX) / 10)
                                                    + 1;
                                            break;
                                        default:
                                            pointIncrease = 0;
                                            break;
                                    }
                                    rotate(defaultDirection);
                                    willWait = false;
                                    waiting = false;
                                    GAME.getScript().setEndRunnable(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    GAME.addPointChange(FRIEND_INCREASE, npcId,
                                                            pointIncrease);
                                                    GAME.increaseFriendScore(npcId, pointIncrease);
                                                    GAME.resetDaysSince(npcId);
                                                    if (oldPoint < 20 && GAME.getFriendScore(npcId) >= 20) {
                                                        GAME.setLoadingScreenEndRunnable(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                GAME.playJingle(R.raw._jingle_rank_up);
                                                                GameActivity.getInstance().
                                                                        displayTextBox(new TextBoxStructure(
                                                                                "> You are now good friends with " + name + "!"));
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                    );
                                    GAME.loadMap(GAME.getScript().getMapId());

                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnable1,
                                    true, npc));
                            break;

                        case "lesson":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    int x = 0;
                                    int y = 0;
                                    int direction = -1;
                                    int lessonIndex = -1;
                                    int time = 0;
                                    switch (npcId) {
                                        case DT_TEACHER_INDEX:
                                            lessonIndex = DT_INDEX;
                                            x = DT_X;
                                            y = DT_Y;
                                            direction = OBJECT_DIRECTION_DOWN;
                                            time = TIME_DT;
                                            break;
                                        case FT_TEACHER_INDEX:
                                            lessonIndex = FT_INDEX;
                                            x = FT_X;
                                            y = FT_Y;
                                            direction = OBJECT_DIRECTION_UP;
                                            time = TIME_FT;
                                            break;
                                        case PE_TEACHER_INDEX:
                                            lessonIndex = PE_INDEX;
                                            x = PE_X;
                                            y = PE_Y;
                                            direction = OBJECT_DIRECTION_DOWN;
                                            break;
                                        case CHEM_TEACHER_INDEX:
                                            lessonIndex = CHEMISTRY_INDEX;
                                            x = CHEM_X;
                                            y = CHEM_Y;
                                            direction = OBJECT_DIRECTION_UP;
                                            time = TIME_CHEM;
                                            break;
                                        case ICT_TEACHER_INDEX:
                                            lessonIndex = ICT_INDEX;
                                            x = ICT_X;
                                            y = ICT_Y;
                                            direction = OBJECT_DIRECTION_UP;
                                            time = TIME_ICT;
                                            break;
                                    }

                                    GAME.getPlayer().setPoint(x, y);
                                    GAME.getPlayer().rotate(direction);
                                    GameActivity gameActivity = GameActivity.getInstance();
                                    if (lessonIndex == CHEMISTRY_INDEX || lessonIndex == ICT_INDEX) {
                                        GAME.setMiniGame(new LessonA(lessonIndex));
                                        gameActivity.displayLessonAUI();
                                        ((LessonA) GAME.getMiniGame()).refreshHUD();
                                    } else if (lessonIndex == DT_INDEX || lessonIndex == FT_INDEX) {
                                        GAME.setMiniGame(new LessonB(lessonIndex));
                                        GameActivity.getInstance().displayLessonBUI();
                                        ((LessonB) GAME.getMiniGame()).setUpSliderAnimation();
                                        ((LessonB) GAME.getMiniGame()).refreshHUD();
                                        ((LessonB) GAME.getMiniGame()).setCraftBarWidth();
                                    } else {
                                        GAME.setMiniGame(new LessonC());
                                        gameActivity.displayLessonCUI();
                                        GAME.loadMap(MAP_PE_ID);
                                        GAME.getPlayer().changeTile("pe");
                                        GAME.getPlayer().rotate(direction);
                                        ((LessonC) GAME.getMiniGame()).refreshHUD();
                                        gameActivity.findViewById(R.id.game_layout).setAlpha(0.5f);
                                        gameActivity.findViewById(R.id.lesson_c_map_menu).
                                                setVisibility(View.VISIBLE);
                                        gameActivity.setUpLessonCMapPointAnimation();
                                    }

                                    int oldTime = GAME.getTime();
                                    GAME.setTime(time);
                                    GAME.reloadMap();
                                    GAME.changeBGM(R.raw._music_activity);
                                    GAME.setTime(oldTime);
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnable1,
                                    false, npc));
                            break;

                        case "buy":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    GameActivity.getInstance().displayBuyMenu(
                                            thisNPC.getShopItems(),
                                            thisNPC);
                                }
                            };
                            runnable2 = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.getPlayer().setResponseIndex(
                                            Integer.parseInt(runnableData[4]));
                                    GameActivity.getInstance().displayTextBox(getText());
                                    GAME.getPlayer().setResponseIndex(0);
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnableData[1],
                                    runnableData[2], runnable1, runnable2, npc));
                            break;

                        case "shop":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    GameActivity.getInstance().displayShopMenu(
                                            thisNPC.getShopItems(),
                                            new ArrayList<>(GAME.getItemSet()), thisNPC);
                                }
                            };
                            runnable2 = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.getPlayer().setResponseIndex(
                                            Integer.parseInt(runnableData[4]));
                                    GameActivity.getInstance().displayTextBox(getText());
                                    GAME.getPlayer().setResponseIndex(0);
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnableData[1],
                                    runnableData[2], runnable1, runnable2, npc));
                            break;

                        case "receive":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    GameActivity.getInstance().displayTextBox(
                                            new TextBoxStructure(runnableData[1]));
                                    switch (npcId) {
                                        case ATHLETE_INDEX:
                                            GAME.addItem(Item.getItem(KEY2_INDEX));
                                            GameActivity.getInstance().runOnUiThread(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            GameActivity.getInstance().
                                                                    enableRunButton();
                                                        }
                                                    }
                                            );
                                            break;
                                        case CLASSMATE_INDEX:
                                            GAME.addItem(Item.getItem(KEY1_INDEX));
                                            break;
                                    }
                                    GAME.playJingle(R.raw._jingle_get_item);
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnable1,
                                    true, npc));
                            break;

                        case "notice":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    GameActivity.getInstance().displayTextBox(new TextBoxStructure(
                                            runnableData[1]
                                    ));
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnable1,
                                    true, npc));
                            break;

                        case "give":
                            runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    GameActivity.getInstance().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            GameActivity.getInstance().displayInventory(
                                                    INVENTORY_GIVE, thisNPC);
                                        }
                                    });
                                }
                            };
                            runnable2 = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.addGivenNPC(thisNPC);
                                    GameActivity.getInstance().displayTextBox(getText());
                                    GAME.removeGivenNPC(thisNPC);
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnableData[1],
                                    runnableData[2], runnable1, runnable2, npc));
                            break;

                        case "lose heist":
                            runnable1 = new TextBoxRunnable(new TextBoxStructure(
                                    "> The teachers caught you in the act! You're suspended " +
                                            "from after school activities!",
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            final String before =
                                                    Game.getTimeKey(TIME_HEIST_PHASE_1).toUpperCase();
                                            GAME.setTime(
                                                    GAME.getProgressDataStructure().getTimeBeforeHeist());
                                            GAME.getProgressDataStructure().setLostHeist();
                                            final String after =
                                                    Game.getTimeKey(GAME.getTime()).toUpperCase();
                                            GameActivity.getInstance().
                                                    setSlideLoadingTransition(before, after);
                                            GAME.resetEventBGM();
                                            GAME.reloadMap();
                                            GameActivity.getInstance().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    GameActivity.getInstance().refreshHUD();
                                                }
                                            });
                                        }
                                    }, true, null)
                            ) {
                                @Override
                                public void run(){
                                    GAME.loseHeist();
                                    GameActivity.getInstance().displayTextBox(textBox);
                                }
                            };
                            texts.put(conditions, new TextBoxStructure(text, runnable1,
                                    true, npc));


                    }
                    if (texts.get(conditions) != null) { texts.get(conditions).setTag(tag); }

                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void readNPCShop(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);

        shopItems = new HashMap<>();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");
                String key = split[0];
                Item item = Item.getItem(Integer.parseInt(split[1]));
                shopItems.put(item, key);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Item> getShopItems() {
        ArrayList<Item> items = new ArrayList<>();
        for (Map.Entry<Item, String> entry : shopItems.entrySet()) {
            if (ExpressionAnalyser.analyse(entry.getValue(), this)) {
                items.add(entry.getKey());
            }
        }
        Collections.sort(items, new ItemComparator());
        return items;
    }

    @Override
    public void update() throws NullPointerException {
        moving = movingUp || movingDown || movingLeft || movingRight;
        if (GAME.isPlayerSpottedByNPC() && !spottedPlayer) { return; }
        if (!moving && willWait) { waiting = true; }
        if (waiting) {
            rotate(GAME.getPlayer());
            if (!willWait) { waiting = false; }
        }
        else if (path != null) {
            movePath();
            if (spottedPlayer && path == null) {
                GameActivity.getInstance().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                GameActivity.getInstance().displayTextBox(getText());
                                spottedPlayer = false;
                            }
                        }
                );
            }
            return;
        }
        else if (ctrl != null) {
            Action action = ctrl.action();
            if (action.getDirection() >= 0) {
                direction = action.getDirection();

                if (!moving) {
                    if (action.isMoving()) {
                        switch (direction) {
                            case OBJECT_DIRECTION_UP:
                                movingUp = isValidLocation(x, y - 1);
                                break;
                            case OBJECT_DIRECTION_DOWN:
                                movingDown = isValidLocation(x, y + 1);
                                break;
                            case OBJECT_DIRECTION_LEFT:
                                movingLeft = isValidLocation(x - 1, y);
                                break;
                            case OBJECT_DIRECTION_RIGHT:
                                movingRight = isValidLocation(x + 1, y);
                                break;
                        }
                    } else { rotate(direction); }
                }
            }
            move();
        }

        if (hostile) {
            setHostileTiles(direction);
        }
    }

    public static void readNPCNames(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw._npcs_names);

        names = new SparseArray<>();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(",");
                names.put(Integer.parseInt(split[0]), split[1]);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void readNPCItems(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw._npcs_items);

        npcItems = new SparseArray<>();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");
                int key = -1;
                switch (split[0]) {
                    case "classmate":
                        key = CLASSMATE_INDEX;
                        break;
                    case "delinquent":
                        key = DELINQUENT_INDEX;
                        break;
                    case "tutee":
                        key = TUTEE_INDEX;
                        break;
                }
                String[] itemInfo = split[1].split(",");
                if (npcItems.get(key) == null) { npcItems.put(key,
                        new ArrayList<NPCItemStructure>()); }
                npcItems.get(key).add(
                        new NPCItemStructure(Item.getItem(Integer.parseInt(itemInfo[0])),
                                Integer.parseInt(itemInfo[1])));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
