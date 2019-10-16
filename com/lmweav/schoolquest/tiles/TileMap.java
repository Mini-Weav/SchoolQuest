package com.lmweav.schoolquest.tiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.lmweav.schoolquest.Game;
import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.characters.NPCDataStructure;
import com.lmweav.schoolquest.characters.Player;
import com.lmweav.schoolquest.controllers.Controller;
import com.lmweav.schoolquest.controllers.Patrol;
import com.lmweav.schoolquest.controllers.RandomMovement;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.items.Item;
import com.lmweav.schoolquest.minigames.LessonA;
import com.lmweav.schoolquest.minigames.LessonB;
import com.lmweav.schoolquest.minigames.LessonC;
import com.lmweav.schoolquest.utilities.ExpressionAnalyser;
import com.lmweav.schoolquest.utilities.GameTextView;
import com.lmweav.schoolquest.utilities.SerializablePoint;
import com.lmweav.schoolquest.utilities.TextBoxStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.lmweav.schoolquest.Game.GAME;
import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: TileMap
 * This class holds the data for a map in the game.
 *
 * Methods in this class initialise external data needed, update and render the map.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class TileMap {

    private static SparseArray<TileMap> gameMaps;

    private int id;
    private int miniMapId;

    private int[][] collisionMatrix;
    private int[][] hostileMatrix;

    private ArrayList<ArrayList<Character>> matrix;
    private ArrayList<Integer> bgm = new ArrayList<>();


    private HashMap<Character, Tile> tiles;
    private HashMap<Point, DoorTileStructure> doorPoint = new HashMap<>();
    private HashMap<Point, InteractiveTileStructure> interactivePoints = new HashMap<>();
    private HashMap<Point, TileBehaviourStructure> behaviourPoints = new HashMap<>();
    private HashMap<Point, Tile> animatedTiles = new HashMap<>();

    private Tile[][] tilesInView = new Tile[CAMERA_HEIGHT + (2 * Y_PADDING) + 1]
            [CAMERA_WIDTH + (2 * X_PADDING) + 1];

    private SparseArray<ArrayList<NPC>> npcs = new SparseArray<>();


    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public TileMap(Context context, int id, int imgId, int txtId, int datId,
                   int doorDatId, int interactiveDatId, int npcDatId, int miniMapId, int bgmId,
                   int behaviourDatId) {
        this.id = id;
        this.miniMapId = miniMapId;

        InputStream inputStream = context.getResources().openRawResource(txtId);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        matrix = new ArrayList<>();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                ArrayList<Character> row = new ArrayList<>();
                for (char ch : line.toCharArray()) { row.add(ch); }
                matrix.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        TileSet tileSet = new TileSet(context, imgId, datId);
        tiles = tileSet.mapTiles();

        collisionMatrix = new int[matrix.size()][matrix.get(0).size()];
        hostileMatrix = new int[matrix.size()][matrix.get(0).size()];
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(0).size(); j++) {
                if (tiles.get(matrix.get(i).get(j)).collision) {
                    collisionMatrix[i][j] = 1;
                } else {
                    collisionMatrix[i][j] = 0;
                }
            }
        }

        bgm.add(bgmId);

        if (doorDatId >= 0) { initialiseDoorPoints(context, doorDatId); }
        if (interactiveDatId >= 0) { initialiseInteractivePoints(context, interactiveDatId); }
        if (npcDatId >= 0) { initialiseNPCs(context, npcDatId); }
        if (behaviourDatId >= 0) { initialiseBehaviourPoints(context, behaviourDatId); }

    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getId() { return id; }

    public int getMiniMapId() { return miniMapId; }

    char getTileInView(int x, int y) { return tilesInView[y][x].key; }
    void setTileInView(int x, int y, char key) {
        tilesInView[y][x] = tiles.get(key);
    }

    public int getRows() { return matrix.size(); }

    public int getCols() { return matrix.get(0).size(); }

    public int[][] getCollisionMatrix() {
        int[][] copy = new int[collisionMatrix.length][collisionMatrix[0].length];

        for (int i = 0; i < collisionMatrix.length; i++) {
            System.arraycopy(collisionMatrix[i], 0, copy[i], 0, collisionMatrix[i].length);
        }
        return copy;
    }

    private boolean getCollision(int x, int y) { return collisionMatrix[y][x] > 0; }
    public void setCollision(int x, int y, int value) { collisionMatrix[y][x] = value; }
    public void removeNPCCollisions() {
        for (int i = 0; i < collisionMatrix.length; i++) {
            for (int j = 0; j < collisionMatrix[0].length; j++) {
                if (collisionMatrix[i][j] == 2) { collisionMatrix[i][j] = 0;}
            }
        }
    }

    public Tile getTile(int x, int y) { return tiles.get(matrix.get(y).get(x)); }

    public static TileMap getMap(int id) { return gameMaps.get(id); }

    public boolean isDoorPoint(int x, int y) { return doorPoint.containsKey(new Point(x, y)); }
    public boolean isDoorLocked(int x, int y) {
        Point point = new Point(x, y);
        return doorPoint.containsKey(point) &&
                ExpressionAnalyser.analyse(doorPoint.get(point).getLockCondition(), null);
    }
    public Pair<Integer, Point> getDoorDestination(int x, int y) {
        DoorTileStructure door = doorPoint.get(new Point(x, y));
        return new Pair<>(door.getDestMap(), door.getDestCoordinate());
    }

    public boolean isCollidable(int x, int y) {
        return getCollision(x, y) || isDoorLocked(x, y);
    }

    void setAnimatedTile(int x, int y, char key) {
        animatedTiles.put(new Point(x, y), tiles.get(key));
    }

    public TextBoxStructure getText(int x, int y) {
        InteractiveTileStructure interactiveTile = interactivePoints.get(new Point(x, y));
        for (Map.Entry<String, TextBoxStructure> entry : interactiveTile.getTexts().entrySet()) {
            if (ExpressionAnalyser.analyse(entry.getKey(), null)) { return entry.getValue(); }
        }
        return null;
    }

    public ArrayList<NPC> getNPCs(int time) { return npcs.get(time); }

    private boolean hasBehaviour(int x, int y) {
        return behaviourPoints.get(new Point(x, y)) != null;
    }

    public int getHostileMatrixRows() { return hostileMatrix.length; }
    public int getHostileMatrixCols() { return hostileMatrix[0].length; }
    public boolean isHostile(int x, int y) { return hostileMatrix[y][x] == 1; }
    public void setHostileTile(int x, int y) { hostileMatrix[y][x] = 1; }
    public void resetHostileMatrix() { hostileMatrix = new int[matrix.size()][matrix.get(0).size()]; }

    public int getBGM() { return bgm.get(0); }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public static void loadMaps(Context context) {
        gameMaps = new SparseArray<>();

        gameMaps.put(MAP_SCHOOL_HALL_G_ID, new TileMap(context, MAP_SCHOOL_HALL_G_ID,
                R.drawable._tilesets_maps_school_hall,
                R.raw._maps_hall_g, R.raw._tilesets_school_hall, R.raw._doors_school_hall_g,
                R.raw._interactive_school_hall_g, R.raw._npcs_school_hall_g,
                0, R.raw._music_school, R.raw._behaviour_hall_g));

        gameMaps.put(MAP_SCHOOL_HALL_1F_ID, new TileMap(context, MAP_SCHOOL_HALL_1F_ID,
                R.drawable._tilesets_maps_school_hall,
                R.raw._maps_hall_1f, R.raw._tilesets_school_hall, R.raw._doors_school_hall_1f,
                R.raw._interactive_school_hall_1f, R.raw._npcs_school_hall_1f,
                1, R.raw._music_school, R.raw._behaviour_hall_1f));

        gameMaps.put(MAP_SCHOOL_CLASSROOM_DT_ID, new TileMap(context, MAP_SCHOOL_CLASSROOM_DT_ID,
                R.drawable._tilesets_maps_school_classroom,
                R.raw._maps_classroom_dt, R.raw._tilesets_school_classroom,
                R.raw._doors_school_classroom_dt, R.raw._interactive_school_classroom_dt,
                R.raw._npcs_school_classroom_dt, 0,
                R.raw._music_school, R.raw._behaviour_classroom_dt));

        gameMaps.put(MAP_SCHOOL_CLASSROOM_FT_ID, new TileMap(context, MAP_SCHOOL_CLASSROOM_FT_ID,
                R.drawable._tilesets_maps_school_classroom,
                R.raw._maps_classroom_ft, R.raw._tilesets_school_classroom,
                R.raw._doors_school_classroom_ft, R.raw._interactive_school_classroom_ft,
                R.raw._npcs_school_classroom_ft, 0,
                R.raw._music_school, R.raw._behaviour_classroom_ft));

        gameMaps.put(MAP_SCHOOL_CLASSROOM_1F_ID, new TileMap(context, MAP_SCHOOL_CLASSROOM_1F_ID,
                R.drawable._tilesets_maps_school_classroom,
                R.raw._maps_classroom_1f, R.raw._tilesets_school_classroom,
                R.raw._doors_school_classroom_1f, R.raw._interactive_school_classroom_1f,
                R.raw._npcs_school_classroom_1f, 1, R.raw._music_school,
                R.raw._behaviour_classroom_1f));

        gameMaps.put(MAP_SCHOOL_CANTEEN_ID, new TileMap(context, MAP_SCHOOL_CANTEEN_ID,
                R.drawable._tilesets_maps_school_canteen,
                R.raw._maps_canteen, R.raw._tilesets_school_canteen, R.raw._doors_school_canteen,
                R.raw._interactive_school_canteen, R.raw._npcs_school_canteen, 0,
                R.raw._music_school, R.raw._behaviour_canteen));

        gameMaps.put(MAP_SCHOOL_YARD_ID, new TileMap(context, MAP_SCHOOL_YARD_ID,
                R.drawable._tilesets_maps_school_yard,
                R.raw._maps_yard, R.raw._tilesets_school_yard, R.raw._doors_school_yard,
                R.raw._interactive_school_yard, R.raw._npcs_school_yard, 0,
                R.raw._music_school, R.raw._behaviour_yard));

        gameMaps.put(MAP_BEDROOM_ID, new TileMap(context, MAP_BEDROOM_ID,
                R.drawable._tilesets_maps_bedroom,
                R.raw._maps_bedroom, R.raw._tilesets_bedroom, NO_DATA,
                R.raw._interactive_bedroom, NO_DATA, 0, R.raw._music_bedroom, NO_DATA));

        gameMaps.put(MAP_SCHOOL_STAFFROOM_ID, new TileMap(context, MAP_SCHOOL_STAFFROOM_ID,
                R.drawable._tilesets_maps_school_staffroom,
                R.raw._maps_staffroom, R.raw._tilesets_school_staffroom,
                R.raw._doors_school_staffroom, R.raw._interactive_school_staffroom,
                R.raw._npcs_school_staffroom, 1, R.raw._music_theme, R.raw._behaviour_staffroom));

        gameMaps.put(MAP_PE_ID, new TileMap(context, MAP_PE_ID,
                R.drawable._tilesets_maps_pe, R.raw._maps_pe, R.raw._tilesets_pe, NO_DATA, NO_DATA,
                NO_DATA, 0, R.raw._music_activity, NO_DATA));

        gameMaps.put(MAP_SCHOOL_EXAM_HALL_ID, new TileMap(context, MAP_SCHOOL_EXAM_HALL_ID,
                R.drawable._tilesets_maps_exam_hall, R.raw._maps_exam_hall,
                R.raw._tilesets_school_exam, NO_DATA, NO_DATA, R.raw._npcs_school_exam_hall, 0,
                R.raw._music_theme, NO_DATA));

        gameMaps.put(MAP_SCHOOL_SHOP_ID, new TileMap(context, MAP_SCHOOL_SHOP_ID,
                R.drawable._tilesets_maps_school_shop, R.raw._maps_shop,
                R.raw._tilesets_school_shop, R.raw._doors_school_shop, R.raw._interactive_school_shop,
                R.raw._npcs_school_shop, 0, R.raw._music_school, R.raw._behaviour_shop));
    }

    private void initialiseDoorPoints(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");

                Point point;
                String[] coordinates = split[0].split(",");
                point = new Point(Integer.parseInt(coordinates[0]),
                        Integer.parseInt(coordinates[1]));
                String[] doorData = split[1].split(",");
                int destMap = Integer.parseInt(doorData[0]);
                Point destCoordinate = new Point(Integer.parseInt(doorData[1]),
                        Integer.parseInt(doorData[2]));
                DoorTileStructure door = new DoorTileStructure(destMap, destCoordinate);

                if (split.length > 2) { door.setLockCondition(split[2]); }

                if (split.length > 3) {
                    Runnable effect = null;
                    switch (split[3]) {
                        case "evening":
                            effect = new Runnable() {
                                @Override
                                public void run() {
                                    final String before = Game.getTimeKey(GAME.getTime()).
                                            toUpperCase();
                                    GAME.setTime(TIME_EVENING);
                                    final String after = Game.getTimeKey(GAME.getTime()).
                                            toUpperCase();
                                            GameActivity.getInstance().
                                                    setSlideLoadingTransition(before, after);
                                }
                            };
                            break;
                        case "staff room":
                            effect = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.removeItem(Item.getItem(KEY0_INDEX));
                                    GAME.playJingle(R.raw._jingle_get_item);
                                    GameActivity.getInstance().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            GameActivity.getInstance().displayTextBox(
                                                    new TextBoxStructure(
                                                    "> You unlocked the door the staff room..." +
                                                            "But your key broke in the lock!"
                                            ));
                                        }
                                    });
                                    GAME.getProgressDataStructure().setEnteredStaffRoom();
                                }
                            };
                            break;

                        case "win heist":
                            effect = new Runnable() {
                                @Override
                                public void run() {
                                    GAME.winHeist();
                                }
                            };
                    }
                    door.setEffect(effect);
                }

                doorPoint.put(point, door);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void initialiseInteractivePoints(Context context, final int id) {
        InputStream inputStream = context.getResources().openRawResource(id);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                final String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");

                Point point;
                try {
                    String[] coordinates = split[0].split(",");
                    String tag = null;

                    point = new Point(Integer.parseInt(coordinates[0]),
                            Integer.parseInt(coordinates[1]));
                    if (interactivePoints.get(point) == null) { interactivePoints.put(point,
                            new InteractiveTileStructure());
                    }

                    String[] keyData = split[1].split(";");
                    String condition = keyData[0];
                    if (keyData.length > 1) { tag = keyData[1]; }

                    if (split.length < 4) {
                        interactivePoints.get(point).setText(condition,
                                new TextBoxStructure(split[2]));
                    } else {
                        final String[] runnableData = split[3].split("\\|");
                        Runnable runnable1 = null;
                        final int mapId = this.id;
                        switch (runnableData[0]) {
                            case "study":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        ConstraintLayout gameLayout = GameActivity.getInstance().
                                                findViewById(R.id.game_layout);
                                        ConstraintLayout studyMenu = GameActivity.getInstance().
                                                findViewById(R.id.study_menu);
                                        gameLayout.setAlpha(0.5f);
                                        studyMenu.setVisibility(View.VISIBLE);
                                        GAME.getPlayer().resetMoving();
                                    }
                                };
                                break;

                            case "game":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        GAME.game();
                                    }
                                };
                                break;

                            case "sleep":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (GAME.getDay() < 30) { GAME.newDay(); }
                                        else {
                                            GAME.setLoadingScreen(true, -1);
                                            GAME.pauseMusic();
                                            GameActivity.getInstance().displayTextBox(
                                                    GAME.startExam(
                                                            (GAME.getDay() + 1) % NUMBER_OF_DAYS));
                                        }
                                    }
                                };
                                break;

                            case "craft":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayList<Item> items = new ArrayList<>();
                                        switch (mapId) {
                                            case MAP_SCHOOL_CLASSROOM_DT_ID:
                                                if (GAME.getProgressDataStructure().isMadeCraftD()) {
                                                    items.add(Item.getItem(CRAFT_D_INDEX));
                                                }
                                                if (GAME.getProgressDataStructure().isMadeCraftC()) {
                                                    items.add(Item.getItem(CRAFT_C_INDEX));
                                                }
                                                if (GAME.getProgressDataStructure().isMadeCraftB()) {
                                                    items.add(Item.getItem(CRAFT_B_INDEX));
                                                }
                                                if (GAME.getProgressDataStructure().isMadeCraftA()) {
                                                    items.add(Item.getItem(CRAFT_A_INDEX));
                                                }
                                                break;
                                            case MAP_SCHOOL_CLASSROOM_FT_ID:
                                                if (GAME.getProgressDataStructure().isMadeSnackD()) {
                                                    items.add(Item.getItem(FOOD_D_INDEX));
                                                }
                                                if (GAME.getProgressDataStructure().isMadeSnackC()) {
                                                    items.add(Item.getItem(FOOD_C_INDEX));
                                                }
                                                if (GAME.getProgressDataStructure().isMadeSnackB()) {
                                                    items.add(Item.getItem(FOOD_B_INDEX));
                                                }
                                                if (GAME.getProgressDataStructure().isMadeSnackA()) {
                                                    items.add(Item.getItem(FOOD_A_INDEX));
                                                }
                                                if (GAME.hasItem(Item.getItem(KEY1_INDEX))) {
                                                    items.add(Item.getItem(KEY5_INDEX));
                                                }
                                                break;
                                        }
                                        GAME.getPlayer().resetMoving();
                                        GameActivity.getInstance().displayCraftMenu(items);
                                    }
                                };
                                break;

                            case "heist":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        GAME.startHeist();
                                    }
                                };
                                break;

                            case "hack":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        GAME.hack();
                                    }
                                };
                                break;

                            case "warp":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        Player player = GAME.getPlayer();
                                        int x = player.getX();
                                        int y = player.getY();
                                        switch (player.getDirection()) {
                                            case OBJECT_DIRECTION_UP:
                                                y--;
                                                break;
                                            case OBJECT_DIRECTION_DOWN:
                                                y++;
                                                break;
                                            case OBJECT_DIRECTION_LEFT:
                                                x--;
                                                break;
                                            case OBJECT_DIRECTION_RIGHT:
                                                x++;
                                                break;
                                        }
                                        Pair<Integer, Point> doorDestination =
                                                GAME.getTileMap().getDoorDestination(x, y);
                                        GAME.getTileMap().runDoorEffect(x, y);
                                        GAME.loadMap(doorDestination);
                                    }
                                };
                                break;

                            case "book":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        GameActivity.getInstance().displayTextBox(new TextBoxStructure(
                                                "> ...? You found a weird page taped inside. " +
                                                        "Maybe an old student wrote it?",
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        GAME.playJingle(R.raw._jingle_get_item);
                                                        GameActivity.getInstance().displayTextBox(
                                                                new TextBoxStructure(
                                                                        "> You discovered " +
                                                                        "the heist plan!")
                                                        );
                                                        GAME.getProgressDataStructure().
                                                                setHeistPlan();
                                                        GameActivity.getInstance().enableHeistUI();
                                                    }
                                                }, true, null));
                                    }
                                };
                                break;

                            case "npc":
                                runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        int id = Integer.parseInt(split[4]);
                                        for (NPC npc : getNPCs(GAME.getTime())) {
                                            if (npc.getId() == id) {
                                                GameActivity.getInstance().displayTextBox(
                                                        npc.getText()
                                                );
                                                return;
                                            }
                                        } GameActivity.getInstance().displayTextBox(
                                                new TextBoxStructure(
                                                        "ERROR - NPC " + id + " NOT FOUND")
                                        );
                                    }
                                };
                                break;

                            case "whiteboard":
                                final int lessonIndex;
                                switch (split[4]) {
                                    case "dt":
                                        lessonIndex = DT_INDEX;
                                        break;
                                    case "ft":
                                        lessonIndex = FT_INDEX;
                                        break;
                                    case "pe":
                                        lessonIndex = PE_INDEX;
                                        break;
                                    case "chem":
                                        lessonIndex = CHEMISTRY_INDEX;
                                        break;
                                    case "ict":
                                        lessonIndex = ICT_INDEX;
                                        break;
                                    default:
                                        return;
                                }
                                switch (lessonIndex) {
                                    case DT_INDEX:
                                    case FT_INDEX:
                                        runnable1 = new Runnable() {
                                            @Override
                                            public void run() {
                                                GAME.setMiniGame(new LessonB(lessonIndex));

                                                final LessonB gameLessonB =
                                                        (LessonB) GAME.getMiniGame();
                                                gameLessonB.refreshHUD();
                                                gameLessonB.setCraftBarWidth();
                                                gameLessonB.exampleQuestion();

                                                GameActivity.getInstance().displayLessonBUI();
                                                final GameActivity gameActivity =
                                                        GameActivity.getInstance();
                                                final ImageView craftBarSlider =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_b_craft_bar_slider);
                                                final ImageView help = gameActivity.
                                                        findViewById(R.id.lesson_b_help_button);
                                                final ImageView back = gameActivity.
                                                        findViewById(R.id.lesson_b_back_button);

                                                help.setVisibility(View.INVISIBLE);
                                                craftBarSlider.setVisibility(View.GONE);
                                                gameActivity.findViewById(R.id.game_surface).
                                                        setAlpha(0.25f);
                                                gameLessonB.setHelp(true);
                                                gameLessonB.setButtons();
                                                ((GameTextView) gameActivity.findViewById(
                                                        R.id.lesson_b_textbox_label_text)).
                                                        setText("Help");
                                                gameLessonB.displayText(
                                                        "> Tap an icon to get an in-depth " +
                                                                "description.");
                                                back.setVisibility(View.VISIBLE);

                                                back.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        GAME.playSFX(SFX_MOVE);
                                                        back.setVisibility(View.GONE);
                                                        gameActivity.findViewById(
                                                                R.id.game_surface).setAlpha(1f);
                                                        gameLessonB.setHelp(false);
                                                        gameLessonB.setButtons();
                                                        gameLessonB.resetBars();
                                                        gameLessonB.displayText(
                                                                gameLessonB.getQuestionText());
                                                        help.setVisibility(View.VISIBLE);
                                                        gameActivity.findViewById(
                                                                R.id.lesson_b).setAlpha(1f);
                                                        gameActivity.findViewById(
                                                                R.id.lesson_b).
                                                                setVisibility(View.GONE);
                                                        gameActivity.findViewById(
                                                                R.id.lesson_b_textbox).
                                                                setVisibility(View.GONE);
                                                        gameActivity.findViewById(
                                                                R.id.lesson_b_craft_bar_slider).
                                                                setVisibility(View.VISIBLE);
                                                        gameActivity.showButtons();
                                                        GAME.setMiniGame(null);
                                                    }
                                                });
                                            }
                                        };
                                        break;
                                    case PE_INDEX:
                                        runnable1 = new Runnable() {
                                            @Override
                                            public void run() {
                                                GAME.getPlayer().resetMoving();
                                                final int speed = GAME.getPlayer().getSpeed();
                                                GAME.setMiniGame(new LessonC());
                                                GameActivity.getInstance().displayLessonCUI();
                                                final LessonC gameLessonC =
                                                        (LessonC) GAME.getMiniGame();
                                                final GameActivity gameActivity =
                                                        GameActivity.getInstance();
                                                gameLessonC.refreshHUD();

                                                final ConstraintLayout lessonCTextbox =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_c_textbox);
                                                final ConstraintLayout textBox =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_c_textbox_box);
                                                final ImageView help =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_c_help_button);
                                                final ImageView back =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_c_back_button);

                                                final ImageView points =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_c_points);
                                                final GameTextView pointsTitle =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_c_points_text_title);
                                                final GameTextView pointsSubtitle =
                                                        gameActivity.findViewById(
                                                                R.id.lesson_c_points_text_subtitle);

                                                points.setVisibility(View.GONE);
                                                pointsTitle.setVisibility(View.GONE);
                                                pointsSubtitle.setVisibility(View.GONE);

                                                help.setVisibility(View.GONE);
                                                gameActivity.findViewById(R.id.game_surface).
                                                        setAlpha(0.25f);
                                                gameLessonC.setHelp(true);
                                                gameLessonC.setButtons();
                                                textBox.setVisibility(View.VISIBLE);
                                                gameLessonC.displayText(
                                                        "> Tap an icon to get an in-depth " +
                                                                "description.");
                                                back.setVisibility(View.VISIBLE);

                                                back.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        GAME.playSFX(SFX_MOVE);
                                                        back.setVisibility(View.GONE);
                                                        gameActivity.findViewById(
                                                                R.id.game_surface).setAlpha(1f);
                                                        gameLessonC.setHelp(false);
                                                        textBox.setVisibility(View.GONE);
                                                        lessonCTextbox.setClickable(false);
                                                        gameLessonC.setButtons();
                                                        gameLessonC.resetBars();
                                                        help.setVisibility(View.VISIBLE);
                                                        gameActivity.findViewById(R.id.lesson_c).
                                                                setAlpha(1f);
                                                        gameActivity.findViewById(R.id.lesson_c).
                                                                setVisibility(View.GONE);

                                                        points.setVisibility(View.VISIBLE);
                                                        pointsTitle.setVisibility(View.VISIBLE);
                                                        pointsSubtitle.setVisibility(View.VISIBLE);

                                                        lessonCTextbox.setVisibility(View.GONE);
                                                        gameActivity.showButtons();
                                                        GAME.setMiniGame(null);
                                                        GAME.getPlayer().setSpeed(speed);
                                                    }
                                                });
                                            }
                                        };
                                        break;
                                    case CHEMISTRY_INDEX:
                                    case ICT_INDEX:
                                        runnable1 = new Runnable() {
                                            @Override
                                            public void run() {
                                                GAME.setMiniGame(new LessonA(lessonIndex));
                                                GameActivity.getInstance().displayLessonAUI();
                                                final LessonA gameLessonA =
                                                        (LessonA) GAME.getMiniGame();
                                                final GameActivity gameActivity =
                                                        GameActivity.getInstance();
                                                gameLessonA.refreshHUD();
                                                final ImageView help = gameActivity.
                                                        findViewById(R.id.lesson_a_help_button);
                                                final ImageView back = gameActivity.
                                                        findViewById(R.id.lesson_a_back_button);

                                                help.setVisibility(View.GONE);
                                                gameActivity.findViewById(R.id.game_surface).
                                                        setAlpha(0.25f);
                                                gameLessonA.setHelp(true);
                                                gameLessonA.setButtons();
                                                ((GameTextView) gameActivity.findViewById(
                                                        R.id.lesson_a_textbox_label_text))
                                                        .setText("Help");
                                                gameLessonA.displayText(
                                                        "> Tap an icon to get an in-depth " +
                                                                "description.");
                                                back.setVisibility(View.VISIBLE);

                                                back.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        GAME.playSFX(SFX_MOVE);
                                                        back.setVisibility(View.GONE);
                                                        gameActivity.findViewById(
                                                                R.id.game_surface).setAlpha(1f);
                                                        gameLessonA.setHelp(false);
                                                        gameLessonA.setButtons();
                                                        gameLessonA.resetBars();
                                                        gameLessonA.displayText(
                                                                gameLessonA.getQuestionText());
                                                        help.setVisibility(View.VISIBLE);
                                                        gameActivity.findViewById(
                                                                R.id.lesson_a).setAlpha(1f);
                                                        gameActivity.findViewById(
                                                                R.id.lesson_a).
                                                                setVisibility(View.GONE);
                                                        gameActivity.findViewById(
                                                                R.id.lesson_a_textbox).
                                                                setVisibility(View.GONE);
                                                        gameActivity.showButtons();
                                                        GAME.setMiniGame(null);
                                                    }
                                                });
                                            }
                                        };
                                        break;
                                }
                                break;
                        }

                        if (runnableData.length > 1) {
                            interactivePoints.get(point).setText(condition,
                                    new TextBoxStructure(split[2], runnableData[1], runnableData[2],
                                            runnable1, null, null));
                        } else {
                            interactivePoints.get(point).setText(condition,
                                    new TextBoxStructure(split[2], runnable1, false,
                                            null));
                        }


                    }
                    if (interactivePoints.get(point).getText(condition) != null) {
                        interactivePoints.get(point).getText(condition).setTag(tag);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialiseNPCs(Context context, int id) {

        InputStream inputStream = context.getResources().openRawResource(id);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");

                int time = -1;
                switch (split[0]) {
                    case "morning":
                        time = TIME_MORNING;
                        break;
                    case "lunch":
                        time = TIME_LUNCH;
                        break;
                    case "after school":
                        time = TIME_AFTER_SCHOOL;
                        break;
                    case "heist2":
                        time = TIME_HEIST_PHASE_2;
                        break;
                    case "chemistry":
                        time = TIME_CHEM;
                        break;
                    case "ict":
                        time = TIME_ICT;
                        break;
                    case "dt":
                        time = TIME_DT;
                        break;
                    case "ft":
                        time = TIME_FT;
                        break;
                }

                if (npcs.get(time) == null) { npcs.put(time, new ArrayList<NPC>()); }


                String[] npcIdData = split[1].split(",");

                String code = npcIdData[0];

                NPCDataStructure data = new NPCDataStructure(NPC.getData(code));
                int npcId = data.getNpcId();

                switch (code) {
                    case "boy":
                        data.setNpcId(BOY_INDEX + Integer.parseInt(npcIdData[1]));
                        break;
                    case "girl":
                        data.setNpcId(GIRL_INDEX + Integer.parseInt(npcIdData[1]));
                        break;
                    case "woman":
                        data.setNpcId(WOMAN_INDEX + Integer.parseInt(npcIdData[1]));
                        break;
                    case "man":
                        data.setNpcId(MAN_INDEX + Integer.parseInt(npcIdData[1]));
                }

                String name;
                name = NPC.getName(npcId);

                String[] npcData = split[2].split(",");
                NPC npc;
                int direction = -1;

                switch (npcData[2]) {
                    case "up":
                        direction = OBJECT_DIRECTION_UP;
                        break;
                    case "down":
                        direction = OBJECT_DIRECTION_DOWN;
                        break;
                    case "left":
                        direction = OBJECT_DIRECTION_LEFT;
                        break;
                    case "right":
                        direction = OBJECT_DIRECTION_RIGHT;
                        break;
                }

                Controller ctrl = null;
                if (split.length > 3) {
                    String[] ctrlData = split[3].split("\\|");

                    switch (ctrlData[0]) {
                        case "random":
                            ctrl = new RandomMovement(Integer.parseInt(ctrlData[1]) == 1);
                            break;
                        case "patrol":
                            SerializablePoint[] points = new SerializablePoint[ctrlData.length - 1];
                            for (int i = 1; i < ctrlData.length; i++) {
                                String[] coordinate = ctrlData[i].split(",");
                                points[i - 1] = new SerializablePoint(
                                        Integer.parseInt(coordinate[0]),
                                        Integer.parseInt(coordinate[1]));
                            }
                            ctrl = new Patrol(points);
                            break;
                    }
                }

                npc = new NPC(context, Integer.parseInt(npcData[0]), Integer.parseInt(npcData[1]),
                        data, name, code, direction, Integer.parseInt(npcData[3]) == 1, ctrl);

                npcs.get(time).add(npc);

            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void initialiseBehaviourPoints(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                final String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");

                Point point;
                try {
                    String[] coordinates = split[0].split(",");
                    point = new Point(Integer.parseInt(coordinates[0]),
                            Integer.parseInt(coordinates[1]));
                    if (behaviourPoints.get(point) == null) { behaviourPoints.put(point,
                            new TileBehaviourStructure());
                    }
                    final String[] runnableData = split[2].split("\\|");
                    Runnable runnable = null;
                    switch (runnableData[0]) {
                        case "change":
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    String[] coordinates = runnableData[1].split(",");
                                    int x = Integer.parseInt(coordinates[0]);
                                    int y = Integer.parseInt(coordinates[1]);
                                    matrix.get(y).set(x, runnableData[2].charAt(0));
                                }
                            };
                            break;
                    }

                    behaviourPoints.get(point).setRunnables(split[1], runnable);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runDoorEffect(int x, int y) {
        Runnable effect = doorPoint.get(new Point(x, y)).getEffect();
        if (effect != null) { effect.run(); }
    }

    private void runBehaviour(int x, int y) {
        TileBehaviourStructure tileBehaviour = behaviourPoints.get(new Point(x, y));
        for (Map.Entry<String, Runnable> entry : tileBehaviour.getRunnables().entrySet()) {
            if (ExpressionAnalyser.analyse(entry.getKey(), null)) {
                entry.getValue().run();
                return;
            }
        }
    }

    public void update() {
        animatedTiles.clear();
        int x = GAME.getCamera().getX() - X_PADDING;
        int y = GAME.getCamera().getY() - Y_PADDING;
        for (int j = 0; j < tilesInView.length; j++) {
            for (int i = 0; i < tilesInView[0].length; i++) {
                Tile tile;
                if (y + j >= 0  && y + j < matrix.size()
                        && x + i >= 0 && x + i < matrix.get(0).size()) {
                    tile = tiles.get(matrix.get(y + j)
                            .get(x + i));
                    if (hasBehaviour(x + i, y + j)) { runBehaviour(x + i, y + j);}
                    if (tile.animation != null) { animatedTiles.put(new Point(i, j), tile); }
                } else { tile = null; }
                tilesInView[j][i] = tile;
                if (tile instanceof InteractiveTile) {
                    ((InteractiveTile) tile).setEvent(getText(x + i, y + j).getTag(), x + i,
                            y + j);
                }
            }
        }
    }

    public void animateTiles() {
        for (Map.Entry<Point, Tile> entry : animatedTiles.entrySet()) {
            entry.getValue().animateTile(this, entry.getKey().x, entry.getKey().y);
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        for (int j = 0; j < tilesInView.length; j++) {
            for (int i = 0; i < tilesInView[0].length; i++) {
                if (tilesInView[j][i] == null) { continue; }
                canvas.drawBitmap(tilesInView[j][i].image,
                        ((i - X_PADDING) * SCALED_TILE_SIZE) - GAME.getCamera().getDiffX(),
                        ((j - Y_PADDING) * SCALED_TILE_SIZE) - GAME.getCamera().getDiffY(),
                        paint);
                if (tilesInView[j][i] instanceof InteractiveTile &&
                        ((InteractiveTile) tilesInView[j][i]).getEmotion() != null &&
                        !GAME.getProgressDataStructure().isCatchInteractiveTile()) {
                    ((InteractiveTile) tilesInView[j][i]).getEmotion().draw(canvas, paint);
                }
            }
        }
    }
}
