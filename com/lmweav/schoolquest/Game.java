package com.lmweav.schoolquest;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import androidx.core.content.ContextCompat;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;

import com.lmweav.schoolquest.characters.GameCharacter;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.items.Item;
import com.lmweav.schoolquest.characters.Player;
import com.lmweav.schoolquest.items.ItemComparator;
import com.lmweav.schoolquest.minigames.Exam;
import com.lmweav.schoolquest.minigames.MiniGame;
import com.lmweav.schoolquest.scripting.Script;
import com.lmweav.schoolquest.utilities.BGMFader;
import com.lmweav.schoolquest.utilities.Camera;
import com.lmweav.schoolquest.tiles.TileMap;
import com.lmweav.schoolquest.utilities.TextBoxRunnable;
import com.lmweav.schoolquest.utilities.TextBoxStructure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: Game
 * This class holds data for the current game state and runs the game program. Resources
 * such as bitmaps or strings for the UI can also be stored here.
 *
 * Methods in this class predominantly update and render the current game state. The static methods
 * loads and assigns the resource variables.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-16
 */
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    public boolean visible;

    public boolean rated;

    public static final Game GAME = new Game();

    private static HashMap<String, Integer> mapIds = new HashMap<>();
    private static HashMap<String, Integer> timeIds = new HashMap<>();

    private static Bitmap[] gradeImages = new Bitmap[4];
    private static Bitmap[] friendImages = new Bitmap[5];

    private static Bitmap normalCondition;
    private static Bitmap greatCondition;
    private static Bitmap unwellCondition;

    private static Bitmap uncheckedBox;
    private static Bitmap checkedBox;

    private static Bitmap checkedBoxHeist;

    private static int tileDestinationColor;
    private static int hostileTileColor;
    private static Paint highlightPaint = new Paint();

    private final List<GameCharacter> gameCharacters =
            Collections.synchronizedList(new ArrayList<GameCharacter>());
    private ArrayList<Integer> npcsGivenTo = new ArrayList<>();
    private ArrayList<Integer> npcsSpokenTo = new ArrayList<>();
    private ArrayList<int[]> pointChanges = new ArrayList<>();

    private int[] gradeScores = new int[5];
    private int[] friendScores = new int[5];
    private int[] examScores = new int[5];
    private int[] daysSince = new int[5];

    private TreeMap<Item, Integer> inventory = new TreeMap<>(new ItemComparator());

    private GameCharacter[][] gameCharacterMatrix;

    private GameProgressDataStructure progressDataStructure = new GameProgressDataStructure();

    private int points;
    private int gradePoints;
    private int friendPoints;
    private int heistPoints;
    private int day = 1;
    private int currentLoadingTime = 0;
    private int loadingTime = 6;

    private int tick = 0;

    private int time;
    private int averageGP;
    private int averageFP;
    private int money = 250;
    private int gfIndex = -1;

    private int bgmId;
    private int eventBGM = -1;

    private int mapId = -1;

    private float sumGP;
    private float sumFP;

    private boolean loading;
    private transient boolean playerSpottedByNPC;
    private boolean lunchMoney;
    private boolean NGPlus;

    private transient TileMap tileMap;
    private transient Camera camera;
    private Player player;
    private transient Script script;
    private transient MiniGame miniGame;

    private transient Runnable loadingScreenEndRunnable;

    private transient NPC waitingChar;

    private transient Point destination;

    private transient MediaPlayer bgm;
    private transient MediaPlayer jingle;
    private transient static SoundPool sfx;
    private static int[] sfxIds;

    static {
        mapIds.put("hall_g", MAP_SCHOOL_HALL_G_ID);
        mapIds.put("hall_1", MAP_SCHOOL_HALL_1F_ID);
        mapIds.put("class_dt", MAP_SCHOOL_CLASSROOM_DT_ID);
        mapIds.put("class_ft", MAP_SCHOOL_CLASSROOM_FT_ID);
        mapIds.put("class_1", MAP_SCHOOL_CLASSROOM_1F_ID);
        mapIds.put("canteen", MAP_SCHOOL_CANTEEN_ID);
        mapIds.put("yard", MAP_SCHOOL_YARD_ID);
        mapIds.put("bedroom", MAP_BEDROOM_ID);
        mapIds.put("staffroom", MAP_SCHOOL_STAFFROOM_ID);

        timeIds.put("morning", TIME_MORNING);
        timeIds.put("lunch", TIME_LUNCH);
        timeIds.put("after school", TIME_AFTER_SCHOOL);
        timeIds.put("evening", TIME_EVENING);
        timeIds.put("heist", TIME_HEIST_PHASE_1);
        timeIds.put("heist ", TIME_HEIST_PHASE_2);
    }

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    private Game() { }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    static Bitmap getGradeImage(int index) { return gradeImages[index]; }

    static Bitmap getFriendImage(int index) { return friendImages[index]; }

    public static int getMapId(String key) { return mapIds.get(key); }

    public static int getTimeId(String key) { return timeIds.get(key); }

    public static String getTimeKey(int id) {
        for (Map.Entry<String, Integer> entry : timeIds.entrySet()) {
            if (id == entry.getValue()) { return entry.getKey(); }
        }
        return "NULL";
    }

    public static String getSubjectName(int key) {
        switch (key) {
            case DT_INDEX:
                return "DT";
            case FT_INDEX:
                return "Food Tech";
            case PE_INDEX:
                return "PE";
            case CHEMISTRY_INDEX:
                return "Chemistry";
            case ICT_INDEX:
                return "ICT";
        }
        return null;
    }

    public GameProgressDataStructure getProgressDataStructure() { return progressDataStructure; }

    public int getTime() { return time; }

    public boolean getLoading() { return loading; }

    public void setLoading(boolean loading) { this.loading = loading; }

    public boolean isPlayerSpottedByNPC() { return playerSpottedByNPC; }

    public void setPlayerSpottedByNPC(boolean spotted) { this.playerSpottedByNPC = spotted; }

    boolean isLunchMoney() { return lunchMoney; }

    void resetLunchMoney() { lunchMoney = false; }

    public TileMap getTileMap() { return tileMap; }

    public Camera getCamera() { return camera; }

    public Player getPlayer() { return player; }

    public void setPlayer(Player player) { this.player = player; }

    public Script getScript() { return script; }

    public MiniGame getMiniGame() { return miniGame; }

    public void setMiniGame(MiniGame miniGame) { this.miniGame = miniGame; }

    public GameCharacter getGameCharacterFromMap(int x, int y) {
        return gameCharacterMatrix[y][x];
    }

    public void removeGameCharacterFromMap(GameCharacter gc) {
        gameCharacterMatrix[gc.getY()][gc.getX()] = null;
    }

    public void addGameCharacterToMap(GameCharacter gc) {
        gameCharacterMatrix[gc.getY()][gc.getX()] = gc;
    }

    public boolean givenTo(NPC npc) { return npcsGivenTo.contains(npc.getId()); }

    public void addGivenNPC(NPC npc) { npcsGivenTo.add(npc.getId()); }

    public void removeGivenNPC(NPC npc) { npcsGivenTo.remove(Integer.valueOf(npc.getId())); }

    public boolean spokenTo(NPC npc) { return npcsSpokenTo.contains(npc.getId()); }

    void addSpokenNPC(NPC npc) { npcsSpokenTo.add(npc.getId()); }

    public void addPointChange(int id, int index, int increase) {
        pointChanges.add(new int[] { id, index, increase } );
    }

    ArrayList<int[]> getPointChanges() { return pointChanges; }

    boolean hasPointChange() { return !pointChanges.isEmpty(); }

    void clearPointChange() { pointChanges.clear(); }

    NPC getWaitingChar() { return waitingChar; }

    public void setWaitingChar(NPC character) { waitingChar = character; }

    int getGradePoints() { return gradePoints; }

    public int getGradeScore(int index) { return gradeScores[index]; }

    public void increaseGradeScore(int index, int value) { gradeScores[index] += value; }

    int getFriendPoints() { return friendPoints; }

    public int getFriendScore(int index) { return friendScores[index]; }

    public void increaseFriendScore(int index, int value) { friendScores[index] += value; }

    int getHeistPoints() { return heistPoints; }

    public void increaseExamScore(int index, int value) { examScores[index] += value; }

    public void resetDaysSince(int index) { daysSince[index] = 0; }

    static Bitmap getNormalCondition() { return normalCondition; }

    static Bitmap getGreatCondition() { return greatCondition; }

    static Bitmap getUnwellCondition() { return unwellCondition; }

    static Bitmap getUncheckedBox() { return uncheckedBox; }

    static Bitmap getCheckedBox() { return checkedBox; }

    static Bitmap getCheckedBoxHeist() { return checkedBoxHeist; }

    public int getPoints() { return points; }

    public int getDay() { return day; }

    int getAverageGP() { return averageGP; }

    int getAverageFP() { return averageFP; }

    int getMoney() { return money; }

    public int getGfIndex() { return gfIndex; }

    public void setGfIndex(int gfIndex) {
        if (gfIndex == 1 || gfIndex == 4 || this.gfIndex == -1) { this.gfIndex = gfIndex; }
    }

    public void setEventBGM(int id) { eventBGM = id; }

    public void setDestination(Point destination) { this.destination = destination; }

    int getInventorySize() { return inventory.size(); }
    public Set<Item> getItemSet() { return inventory.keySet(); }
    public boolean hasItem(Item item) { return inventory.containsKey(item); }

    boolean hasLoadingEndRunnable() { return loadingScreenEndRunnable != null; }

    public void setLoadingScreenEndRunnable(Runnable loadingScreenEndRunnable) {
        this.loadingScreenEndRunnable = loadingScreenEndRunnable;
    }
    void runLoadingScreenEndRunnable() { loadingScreenEndRunnable.run(); }

    MediaPlayer getBGM() { return bgm; }

    public void resetEventBGM() { eventBGM = -1; }

    MediaPlayer getJingle() { return jingle; }

    public int getTick() { return tick; }

    boolean isNGPlus() { return NGPlus; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    static void loadGradeImages(Context context) {
        gradeImages[0] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_grade_d);
        gradeImages[1] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_grade_c);
        gradeImages[2] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_grade_b);
        gradeImages[3] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_grade_a);
    }

    static void loadFriendImages(Context context) {
        friendImages[0] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_friend_athlete);
        friendImages[1] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_friend_classmate);
        friendImages[2] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_friend_nerd);
        friendImages[3] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_friend_delinquent);
        friendImages[4] = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_friend_tutee);
    }

    static void loadStatusImages(Context context) {
        checkedBox = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_main_rank_bar_done);
        uncheckedBox = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_main_rank_bar);
        normalCondition = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_main_status_player_condition_normal);
        greatCondition = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_main_status_player_condition_great);
        unwellCondition = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_main_status_player_condition_unwell);
        checkedBoxHeist = BitmapFactory.decodeResource(
                context.getResources(), R.drawable._ui_main_heist_bar_done);

    }

    static void loadSFX(Context context) {
        sfx = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        sfxIds = new int[10];
        sfxIds[SFX_CLICK] = sfx.load(context, R.raw._sfx_click, 0);
        sfxIds[SFX_MOVE] = sfx.load(context, R.raw._sfx_move, 1);
        sfxIds[SFX_POINT] = sfx.load(context, R.raw._sfx_point, 1);
        sfxIds[SFX_MONEY] = sfx.load(context, R.raw._sfx_money, 1);
        sfxIds[SFX_BUFF] = sfx.load(context, R.raw._sfx_buff, 1);
        sfxIds[SFX_DEBUFF] = sfx.load(context, R.raw._sfx_debuff, 1);
        sfxIds[SFX_DOOR] = sfx.load(context, R.raw._sfx_door, 1);
        sfxIds[SFX_STINKBOMB] = sfx.load(context, R.raw._sfx_stinkbomb, 1);
        sfxIds[SFX_ALARM] = sfx.load(context, R.raw._sfx_alarm, 0);
    }

    static void loadPaint(Context context) {
        tileDestinationColor = ContextCompat.getColor(context, R.color.colorBlue);
        hostileTileColor = ContextCompat.getColor(context, R.color.colorRedHostile);
    }

    Point convertToLogical(int x, int y) {
        float xp = (float) x / SCALED_TILE_SIZE;
        float yp = (float) y / SCALED_TILE_SIZE;

        if (xp < 0) { xp -= 1;}
        x = (int)(xp);

        if (yp < 0) { yp -= 1;}
        y = (int)(yp);

        return new Point(x + camera.getX(), y + camera.getY());
    }

    private void addNPCsFromMap() {
        if (script == null || script.isFinished()) {
            if (tileMap.getNPCs(time) != null) {
                for (NPC npc: tileMap.getNPCs(time)) {
                    npc.reset();
                    gameCharacters.add(npc);
                }
            }
        } else if (!script.isFinished()) {
            if (script.getNpcs() != null) {
                for (NPC npc: script.getNpcs()) {
                    npc.reset();
                    gameCharacters.add(npc);
                }
            }
        }
    }

    private void addNPCsFromData(Context context, Game data) {
        if (!data.gameCharacters.isEmpty()) {
            for (GameCharacter character: data.gameCharacters) {
                if (character instanceof NPC) {
                    NPC npc = new NPC(context, (NPC) character);
                    gameCharacters.add(npc);
                }
            }
        }
    }

    public boolean isGameCharacterInMap(int x, int y) {
        return y >= 0 && y < gameCharacterMatrix.length && x >= 0
                && x <= gameCharacterMatrix[0].length && gameCharacterMatrix[y][x] != null;
    }

    public void setLoadingScreen(boolean loading, int... frames) {
        currentLoadingTime = 0;
        Animation loadingTextAnim;

        if (GameActivity.getInstance().findViewById(
                R.id.loading_text).getAnimation() != null) {
            loadingTextAnim = GameActivity.getInstance().findViewById(R.id.loading_text).
                    getAnimation();
        } else if (GameActivity.getInstance().findViewById(
                R.id.loading_day_old).getAnimation() != null) {
            loadingTextAnim = GameActivity.getInstance().findViewById(R.id.loading_day_old).
                    getAnimation();
        } else {
            loadingTextAnim = null;
        }

        int animDuration = 0;
        if (loadingTextAnim != null) {
            animDuration = (int) ((loadingTextAnim.getDuration() + loadingTextAnim.getStartOffset())
                    * TARGET_FPS) / 1000;
            loadingTextAnim.start();
        }
        if (frames.length == 0) { loadingTime = 6 + animDuration; }
        else { loadingTime = frames[0] + animDuration; }

        if (loading) {
            this.loading = true;
            GameActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GameActivity.getInstance().enableLoadingScreen();
                }
            });
        }
        else {
            GameActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GameActivity.getInstance().disableLoadingScreen();
                }
            });
        }
    }

    public void setTime(int time) {
        this.time = time;
        player.setCondition(NORMAL_CONDITION);
        player.setEaten(false);
        player.resetBuffs();
    }

    public void addItem(Item item) {
        switch (item.getId()) {
            case DT_BOOK_INDEX:
                inventory.remove(Item.getItem(DT_SHEET_INDEX));
                break;
            case FT_BOOK_INDEX:
                inventory.remove(Item.getItem(FT_SHEET_INDEX));
                break;
            case PE_BOOK_INDEX:
                inventory.remove(Item.getItem(PE_SHEET_INDEX));
                break;
            case CHEM_BOOK_INDEX:
                inventory.remove(Item.getItem(CHEM_SHEET_INDEX));
                break;
            case ICT_BOOK_INDEX:
                inventory.remove(Item.getItem(ICT_SHEET_INDEX));
                break;
        }
        if (!inventory.containsKey(item)) { inventory.put(item, 1); }
        else { inventory.put(item, inventory.get(item) + 1); }
    }

    public void removeItem(Item item) {
        if (inventory.containsKey(item)) {
            int n = inventory.get(item);
            if (n == 1) { inventory.remove(item); }
            else {inventory.put(item, n - 1); }
        }
    }

    public int getItemQuantity(Item item) {
        try {
            return inventory.get(item);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    boolean isSheetItem(Item item) {
        if (item == null) { return false; }
        return item.getId() >= DT_SHEET_INDEX && item.getId() <= ICT_SHEET_INDEX;
    }

    boolean isBookItem(Item item) {
        if (item == null) { return false; }
        return item.getId() >= DT_BOOK_INDEX && item.getId() <= ICT_BOOK_INDEX;
    }

    boolean hasBook(Item sheet) {
        return inventory.containsKey(Item.getItem(sheet.getId() - 5));
    }

    public int increasePoints(int id, int index, int increase) {
        int pointIncrease = 0;
        switch (id) {
            case GRADE_INCREASE:
                pointIncrease = (int) (2 * (Math.pow((increase * ((gradeScores[index] / 10) + 1)),
                        2) / Math.pow(day, 1 / 3)));
                gradePoints += pointIncrease;
                playSFX(SFX_POINT);
                break;
            case FRIEND_INCREASE:
                pointIncrease = (int) (2 * (Math.pow((increase * ((friendScores[index] / 10) + 1)),
                        2) / Math.pow(day, 1 / 3)));
                friendPoints += pointIncrease;
                playSFX(SFX_POINT);
                break;
            case FRIEND_DECREASE:
                pointIncrease = (int) (-2 * Math.pow((increase * ((friendScores[index] / 10) + 1)),
                        2));
                friendPoints += pointIncrease;
                playSFX(SFX_DEBUFF);
                break;
            case HEIST_BONUS:
                pointIncrease = (int) (points * (0.5 + ((NUMBER_OF_DAYS - day) / NUMBER_OF_DAYS)));
                heistPoints += pointIncrease;
                playSFX(SFX_POINT);
                break;
            case EXAM_INCREASE:
                pointIncrease = (int) (4 * (Math.pow((increase * ((examScores[index] / 10) + 1)),
                        2) / Math.pow(NUMBER_OF_DAYS, 1 / 3)));
                gradePoints += pointIncrease;
                playSFX(SFX_POINT);
                break;
        }
        points += pointIncrease;
        return points;
    }

    int increaseMoney(int increase) {
        money += increase;
        return money;
    }

    public void loadMap(int mapId) {
        tileMap = TileMap.getMap(mapId);
        reloadMap();
    }

    public void loadMap(Pair<Integer, Point> destination) {
        player.setPoint(destination.second.x, destination.second.y);
        tileMap = TileMap.getMap(destination.first);
        reloadMap();
    }

    public void reloadMap() {
        tileMap.removeNPCCollisions();

        setLoadingScreen(true);

        tileMap.update();

        gameCharacters.clear();
        gameCharacters.add(player);
        this.destination = null;

        addNPCsFromMap();
        gameCharacterMatrix = new GameCharacter[tileMap.getRows()][tileMap.getCols()];
        for (GameCharacter gc : gameCharacters) {
            gameCharacterMatrix[gc.getY()][gc.getX()] = gc;
            tileMap.setCollision(gc.getX(), gc.getY(), 2);
        }

        camera.setBoundingBox();

        GameActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameActivity.getInstance().refreshHUD();
            }
        });
        if (eventBGM > -1) {
            if (bgmId != eventBGM) { changeBGM(eventBGM); }
        }
        else if (bgmId != tileMap.getBGM() && script == null) { changeBGM(tileMap.getBGM()); }
    }

    void newGame(Context context, String playerName) {
        player = new Player(context, NEW_GAME_X, NEW_GAME_Y, playerName);
        camera = new Camera(player.getX() - (CAMERA_WIDTH / 2),
                player.getY() - (CAMERA_HEIGHT / 2));

        tileMap = TileMap.getMap(NEW_GAME_MAP_ID);
        tileMap.removeNPCCollisions();
        tileMap.update();

        gameCharacters.clear();
        gameCharacters.add(player);
        this.destination = null;

        addNPCsFromMap();
        gameCharacterMatrix = new GameCharacter[tileMap.getRows()][tileMap.getCols()];
        for (GameCharacter gc : gameCharacters) {
            gameCharacterMatrix[gc.getY()][gc.getX()] = gc;
            tileMap.setCollision(gc.getX(), gc.getY(), 2);
        }

        camera.setBoundingBox();

        bgmId = tileMap.getBGM();
        bgm = MediaPlayer.create(context, bgmId);
        bgm.setLooping(true);
        bgm.start();

        jingle = MediaPlayer.create(context, R.raw._jingle_get_item);
    }

    public void goHome() {
        player.setPoint(HOME_X, HOME_Y);
        player.rotate(OBJECT_DIRECTION_DOWN);
        tileMap = TileMap.getMap(MAP_BEDROOM_ID);

        String before = getTimeKey(time).toUpperCase();
        time = TIME_EVENING;
        String after = getTimeKey(time).toUpperCase();
        GameActivity.getInstance().
                setSlideLoadingTransition(before, after);

        reloadMap();
    }

    public void newDay() {
        player.setPoint(NEW_GAME_X, NEW_GAME_Y);
        player.rotate(OBJECT_DIRECTION_UP);
        tileMap = TileMap.getMap(MAP_SCHOOL_HALL_G_ID);

        npcsGivenTo.clear();
        npcsSpokenTo.clear();

        day++;
        time = TIME_MORNING;
        if (NUMBER_OF_DAYS - day != 0) {
            GameActivity.getInstance().setCalendarLoadingTransition(
                    NUMBER_OF_DAYS - (day - 1), NUMBER_OF_DAYS - day);
        }

        reloadMap();

        for (int i = 0; i < 5; i++) {
            if (i == ATHLETE_INDEX || i == NERD_INDEX || i == TUTEE_INDEX) {
                daysSince[i]++;
                if (friendScores[i] >= 10 && friendScores[i] < 20 && daysSince[i] > 2) {
                    int decrease = daysSince[i] - 1;
                    friendScores[i] -= decrease;
                    pointChanges.add(new int[] {FRIEND_DECREASE, i, decrease} );
                }
            }
        }
        lunchMoney = true;
    }

    public TextBoxStructure startExam(final int id) {
        String subject = getSubjectName(id);
        String number;
        switch (id) {
            case DT_INDEX:
                number = "first";
                break;
            case FT_INDEX:
                number = "second";
                break;
            case PE_INDEX:
                number = "third";
                break;
            case CHEMISTRY_INDEX:
                number = "fourth";
                break;
            case ICT_INDEX:
                number = "fifth";
                break;
            default:
                throw new IllegalArgumentException();
        }

        return new TextBoxStructure(
                "> It's the " + number + " day of exams... Today's exam is " + subject + ".",
                new Runnable() {
                    @Override
                    public void run() {
                        player.setPoint(EXAM_X, EXAM_y);
                        player.rotate(OBJECT_DIRECTION_UP);

                        npcsGivenTo.clear();
                        npcsSpokenTo.clear();

                        day++;

                        GAME.setMiniGame(new Exam(id));
                        GameActivity.getInstance().displayExamUI();
                        time = TIME_MORNING;
                        loadMap(MAP_SCHOOL_EXAM_HALL_ID);
                        ((Exam) GAME.getMiniGame()).refreshHUD();
                    }
                }, true, null
        );
    }

    void study(final int index) {
        int oldGrade = gradeScores[index];
        final int increase = (int) ((Math.random() * 2) + 1);

        increaseGradeScore(index, increase);

        int newGrade = gradeScores[index];

        String message;
        String subject = getSubjectName(index);

        setLoadingScreen(true, -1);

        if (oldGrade / 10 == newGrade / 10 || oldGrade >= 30) {
            message = "Your proficiency has improved!";
        }
        else {
            String grade;
            switch (newGrade / 10) {
                case 1:
                    grade = "a C";
                    if (index == DT_INDEX) { GAME.getProgressDataStructure().setMadeCraft(0); }
                    if (index == FT_INDEX) { GAME.getProgressDataStructure().setMadeSnack(0); }
                    break;
                case 2:
                    grade = "a B";
                    if (index == DT_INDEX) { GAME.getProgressDataStructure().setMadeCraft(1); }
                    if (index == FT_INDEX) { GAME.getProgressDataStructure().setMadeSnack(1); }
                    break;
                default:
                    grade = "an A";
                    if (index == DT_INDEX) { GAME.getProgressDataStructure().setMadeCraft(2); }
                    if (index == FT_INDEX) { GAME.getProgressDataStructure().setMadeSnack(2); }
                    break;
            }
            message = "Your grade has increased! You now have " + grade + " in " + subject + "!";
            playJingle(R.raw._jingle_rank_up);
        }
        if (day < 30) {
            GameActivity.getInstance().displayTextBox(
                    new TextBoxStructure(
                            "> You spent the evening studying " + subject + "... " + message,
                            new Runnable() {
                                @Override
                                public void run() {
                                    newDay();
                                    pointChanges.add(new int[] {GRADE_INCREASE, index, increase} );
                                }
                            }, true, null));
        }
        else {
            Runnable runnable = new TextBoxRunnable(startExam((day + 1) % NUMBER_OF_DAYS)) {
                @Override
                public void run() {
                    GAME.pauseMusic();
                    pointChanges.add(new int[] {GRADE_INCREASE, index, increase} );
                    GameActivity.getInstance().displayTextBox(textBox);
                }
            };
            GameActivity.getInstance().displayTextBox(
                    new TextBoxStructure(
                            "> You spent the evening studying " + subject + "... " + message,
                            runnable, true, null));
        }
    }

    public void game() {
        double rand = Math.random();
        String message;
        if (rand < GAME_BUFF_CHANCE) {
            GAME.playSFX(SFX_BUFF);
            player.setCondition(GREAT_CONDITION);
            message = "> You spent the evening playing games... You feel great now!";
        } else {
            GAME.playSFX(SFX_DEBUFF);
            player.setCondition(UNWELL_CONDITION);
            message = "> You spent the evening playing games... However, you played too much! " +
                    "You don't feel very well now...";
        }

        setLoadingScreen(true, -1);

        if (day < 30) {
            GameActivity.getInstance().displayTextBox(
                    new TextBoxStructure(message,
                            new Runnable() {
                                @Override
                                public void run() {
                                    newDay();
                                }
                            }, true, null));
        } else {
            Runnable runnable = new TextBoxRunnable(startExam((day + 1) % NUMBER_OF_DAYS)) {
                @Override
                public void run() {
                    GAME.pauseMusic();
                    GameActivity.getInstance().displayTextBox(textBox);
                }
            };
            GameActivity.getInstance().displayTextBox(
                    new TextBoxStructure(message, runnable, true, null));
        }
    }

    public void startHeist() {
        playSFX(SFX_STINKBOMB);
        playSFX(SFX_ALARM);

        progressDataStructure.setStartedHeist();
        progressDataStructure.setTimeBeforeHeist(time);

        pauseMusic();
        removeItem(Item.getItem(KEY3_INDEX));
        setLoadingScreen(true, -1);

        GameActivity.getInstance().displayTextBox(new TextBoxStructure("> You put the stink " +
                "bomb in the air vent... The fire bell is ringing! All the students and teachers " +
                "have gone outside!", new Runnable() {
                    @Override
                    public void run() {
                        final String before = getTimeKey(time).toUpperCase();
                        setTime(TIME_HEIST_PHASE_1);
                        final String after = getTimeKey(time).toUpperCase();
                        GameActivity.getInstance().
                                setSlideLoadingTransition(before, after);
                        eventBGM = R.raw._music_theme;
                        reloadMap();
                        GameActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GameActivity.getInstance().refreshHUD();
                            }
                        });
                    }
                }, true, null));
    }

    public void hack() {
        removeItem(Item.getItem(KEY4_INDEX));
        addItem(Item.getItem(KEY6_INDEX));

        playJingle(R.raw._jingle_get_item);

        setLoadingScreen(true, -1);
        progressDataStructure.setHackedPC();

        progressDataStructure.setCatchInteractiveTile();
        progressDataStructure.setCatchInteractiveTileText("> There's no time to look around, " +
                "you need to get out of here!");
        progressDataStructure.setCatchNPCInteraction();
        progressDataStructure.setCatchNPCInteractionText("> What are you doing!? Don't get the" +
                " teachers' attention!");

        GameActivity.getInstance().displayTextBox(new TextBoxStructure("> Yes!! " +
                "You found the exam questions! But the teachers are back... You need to get out" +
                " of here!", new Runnable() {
            @Override
            public void run() {
                setTime(TIME_HEIST_PHASE_2);
                reloadMap();
                GameActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GameActivity.getInstance().refreshHUD();
                    }
                });
            }
        }, true, null));
    }

    public void winHeist() {
        progressDataStructure.resetCatchInteractiveTile();
        progressDataStructure.resetCatchNPCInteraction();

        pauseMusic();
        setLoadingScreen(true, -1);
        playJingle(R.raw._jingle_heist_win);

        GameActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameActivity.getInstance().displayTextBox(new TextBoxStructure(
                        "> The heist was a success! " +
                        "You will leak the questions to your friends!", new Runnable() {
                    @Override
                    public void run() {
                        final String before = getTimeKey(TIME_HEIST_PHASE_1).toUpperCase();
                        setTime(progressDataStructure.getTimeBeforeHeist());
                        progressDataStructure.setWonHeist();
                        pointChanges.add(new int[] { HEIST_BONUS, 0, 0 });
                        final String after = getTimeKey(time).toUpperCase();
                        GameActivity.getInstance().
                                setSlideLoadingTransition(before, after);
                        eventBGM = -1;
                        reloadMap();
                        GameActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GameActivity.getInstance().refreshHUD();
                            }
                        });
                    }
                }, true, null));
            }
        });
    }

    public void loseHeist() {
        playerSpottedByNPC = false;
        player.setPoint(9, 2);
        player.rotate(OBJECT_DIRECTION_DOWN);
        tileMap = TileMap.getMap(MAP_SCHOOL_HALL_1F_ID);

        progressDataStructure.resetCatchInteractiveTile();
        progressDataStructure.resetCatchNPCInteraction();

        pauseMusic();
        setLoadingScreen(true, -1);
        playJingle(R.raw._jingle_heist_lose);
    }

    public void loadCutscene(final Script script) {
        this.script = script;

        GameActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (script.isSkippable()) { GameActivity.getInstance().enableSkipButton(); }
                else { GameActivity.getInstance().disableSkipButton(); }
                GameActivity.getInstance().scriptHUD(true);
            }
        });

        int mapId = script.getMapId();
        if (GAME.getTileMap().getId() == mapId) { GAME.loadMap(mapId); }
        else { GAME.setLoadingScreen(true); }

        int playerX = script.getPlayerX();
        int playerY = script.getPlayerY();
        int playerDirection = script.getPlayerDirection();

        player.changeTile(script.getPlayerTileSet());

        player.setPoint(playerX, playerY);
        player.rotate(playerDirection);

        script.setLoaded(true);
        script.setOldSpeed(player.getSpeed());
        script.copyCommands();

        if (bgmId != script.getBGM()) { changeBGM(script.getBGM()); }
    }

    public void changeBGM(final int newBGM, final int... pos) {
        BGMFader.stop(bgm, 400, new Runnable() {
            @Override
            public void run() {
                bgm.reset();

                bgmId = newBGM;
                AssetFileDescriptor afd = GameActivity.getInstance().getResources().
                        openRawResourceFd(bgmId);
                if (afd == null) return;

                try {
                    bgm.setDataSource(
                            afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                    bgm.prepare();
                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                }

                bgm.setLooping(true);
                if (pos.length > 0) { bgm.seekTo(pos[0]); }
            }
        });
    }

    public void playJingle(int jingleId) {
        jingle.reset();

        bgm.setVolume(0, 0);

        AssetFileDescriptor afd = GameActivity.getInstance().getResources().
                openRawResourceFd(jingleId);
        if (afd == null) return;

        try {
            jingle.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            jingle.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        jingle.start();

        jingle.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                bgm.setVolume(1, 1);
            }
        });
    }

    public void playSFX(int id) {
        sfx.play(sfxIds[id], 1, 1, 1, 0, 1.0f);
    }

    public void pauseMusic() { bgm.pause(); }

    void update() {
        tick++;
        if (tick == TARGET_FPS) { tick = 0; }

        tileMap.resetHostileMatrix();

        synchronized (gameCharacters) {
            for (GameCharacter gc : gameCharacters) {
                if (gc instanceof NPC) {
                    if (gc.getEmotion() != null && !gc.getEmotion().isAuto()
                            && GameActivity.getInstance().findViewById(R.id.textbox).getVisibility()
                            == View.VISIBLE) {
                        gc.setEmotion(-1);
                    }
                    else if (((NPC) gc).hasEvent()) {
                        if (gc.getEmotion() == null) { ((NPC) gc).setEvent(); }
                    }
                    else if (gc.getEmotion() != null && !gc.getEmotion().isAuto()) {
                        gc.setEmotion(-1);
                    }
                }
                if (gc.getEmotion() != null) { gc.getEmotion().update(); }
            }
        }

        tileMap.animateTiles();

        if (!GameActivity.getInstance().isGamePause()) {

            if (script == null) { for (GameCharacter gc : gameCharacters) { gc.update(); } }
            else if (script.isLoaded() && (!loading || script.isStarted())) {
                script.execute();
                if (script.isFinished()) {
                    script.setFinished(false);
                    script.setSkip(false);
                    script.setStarted(false);
                    script.setLoaded(false);
                    if (script.getEndRunnable() != null) {
                        GameActivity.getInstance().runOnUiThread(script.getEndRunnable());
                        script.setEndRunnable(null);
                    }
                    script = null;
                }
            }
            camera.update();
            if (tileMap.isDoorPoint(player.getX(), player.getY())) {
                if (!tileMap.isDoorLocked(player.getX(), player.getY())) {
                    playSFX(SFX_DOOR);
                    Pair<Integer, Point> doorDestination =
                            tileMap.getDoorDestination(player.getX(), player.getY());
                    tileMap.runDoorEffect(player.getX(), player.getY());
                    loadMap(doorDestination);
                }
            }

            if (loading &&
                    GameActivity.getInstance().
                            findViewById(R.id.loading_screen).getAnimation() == null) {
                if (loadingTime <= 0) { return; }
                if (currentLoadingTime > loadingTime) {
                    setLoadingScreen(false);
                    currentLoadingTime = 0;
                } else { currentLoadingTime++; }
            }
        }
    }

    void updatePointAverages() {
        sumGP = 0;
        sumFP = 0;
        for (int i = 0; i < 5; i++) {
            sumGP += gradeScores[i] > 29 ? 30 : gradeScores[i];
            sumFP += friendScores[i] > 19 ? 20 : friendScores[i];
        }
        averageGP = sumGP >= MAX_GP ? 3: (int) ((sumGP / MAX_GP) * 3);
        averageFP = sumFP >= MAX_FP ? 3: (int) ((sumFP / MAX_FP) * 3);
    }


    void load(Context context, final GameActivity gameActivity) {
        Game data;

        try {
            FileInputStream saveData = context.openFileInput(gameActivity.getDataFile());
            ObjectInputStream in = new ObjectInputStream(saveData);
            data = (Game) in.readObject();
            in.close();
            saveData.close();
        } catch (Exception e) {
            newGame(gameActivity, gameActivity.getPlayerName());
            return;
        }

        npcsGivenTo = new ArrayList<>(data.npcsGivenTo);
        npcsSpokenTo = new ArrayList<>(data.npcsSpokenTo);
        pointChanges = new ArrayList<>(data.pointChanges);

        gradeScores = new int[5];
        System.arraycopy(
                data.gradeScores, 0, gradeScores, 0, data.gradeScores.length);
        friendScores = new int[5];
        System.arraycopy(
                data.friendScores, 0, friendScores, 0, data.friendScores.length);
        examScores = new int[5];
        System.arraycopy(
                data.examScores, 0, examScores, 0, data.examScores.length);
        daysSince = new int[5];
        System.arraycopy(
                data.daysSince, 0, daysSince, 0, data.daysSince.length);

        inventory = new TreeMap<>(new ItemComparator());
        for (Item item : data.inventory.keySet()) {
            int amount = data.inventory.get(item);
            item = Item.getItem(item.getId());
            inventory.put(item, amount);
        }

        progressDataStructure = data.progressDataStructure;

        points = data.points;
        gradePoints = data.gradePoints;
        friendPoints = data.friendPoints;
        heistPoints = data.heistPoints;
        day = data.day;

        time = data.time;
        averageGP = data.averageGP;
        averageFP = data.averageFP;
        money = data.money;
        gfIndex = data.gfIndex;

        bgmId = data.bgmId;
        eventBGM = data.eventBGM;

        mapId = data.mapId;

        sumGP = data.sumGP;
        sumFP = data.sumFP;

        player = new Player(gameActivity, data.getPlayer());

        tileMap = TileMap.getMap(mapId);
        camera = new Camera(player.getX() - (CAMERA_WIDTH / 2),
                player.getY() - (CAMERA_HEIGHT / 2));

        tileMap.removeNPCCollisions();
        tileMap.update();

        gameCharacters.clear();
        gameCharacters.add(player);
        this.destination = null;

        addNPCsFromData(gameActivity, data);
        gameCharacterMatrix = new GameCharacter[tileMap.getRows()][tileMap.getCols()];
        for (GameCharacter gc : gameCharacters) {
            gameCharacterMatrix[gc.getY()][gc.getX()] = gc;
            tileMap.setCollision(gc.getX(), gc.getY(), 2);
        }

        camera.setBoundingBox();

        bgmId = tileMap.getBGM();
        if (eventBGM > -1) { bgm = MediaPlayer.create(gameActivity, eventBGM); }
        else { bgm = MediaPlayer.create(gameActivity, bgmId); }
        bgm.setLooping(true);
        bgm.start();

        jingle = MediaPlayer.create(gameActivity, R.raw._jingle_get_item);
    }

    public void rateThis(final GameActivity gameActivity) {
        if (!rated) {
            gameActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gameActivity.displayTextBox(
                            new TextBoxStructure("Thanks for playing School Quest! " +
                                    "Please consider rating the game, it will take less than a " +
                                    "minute and will help the developer.",
                                    "Rate", "No thanks",
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            gameActivity.startActivity(
                                                    new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("market://details?id=com.lmweav.schoolquest")));
                                        }
                                    }, null, null)
                    );
                }
            });
            rated = true;
        }
        else { gameActivity.findViewById(R.id.textbox).setVisibility(View.GONE); }
    }

    public void save() {
        Context context = GameActivity.getInstance().getApplicationContext();

        mapId = tileMap.getId();
        try {
            FileOutputStream saveData = context.openFileOutput(
                    GameActivity.getInstance().getDataFile(), Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(saveData);
            out.writeObject(this);
            out.close();
            saveData.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setNGPlus() {
        NGPlus = true;

        friendPoints = 0;
        gradePoints = 0;
        for (int i = 0; i < friendScores.length; i++) { friendScores[i] = 0; }
        for (int i = 0; i < examScores.length; i++) { examScores[i] = 0; }
        for (int i = 0; i < gradeScores.length; i++) {
            gradeScores[i] = (gradeScores[i] / 10) * 10;
        }
        points = 0;
        for(Iterator<Map.Entry<Item, Integer>> it = inventory.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Item, Integer> entry = it.next();
            Item item = entry.getKey();
            if ((item.getId() >= KEY0_INDEX && item.getId() <= KEY5_INDEX)
                    || item.getId() == KEY6_INDEX) {
                it.remove();
            }
        }

        money += 250;
        day = 1;
        time = TIME_MORNING;
        gfIndex = -1;
        loadMap(NEW_GAME_MAP_ID);
        player.setPoint(NEW_GAME_X, NEW_GAME_Y);
        eventBGM = -1;
        loading = false;
    }


    void draw(Canvas canvas, Paint paint) {
        tileMap.draw(canvas, paint);

        if (destination != null) {
            int x = ((destination.x - camera.getX()) * SCALED_TILE_SIZE) - camera.getDiffX();
            int y = ((destination.y - camera.getY()) * SCALED_TILE_SIZE) - camera.getDiffY();

            highlightPaint.setColor(tileDestinationColor);

            canvas.drawRect(x, y, x + SCALED_TILE_SIZE, y + SCALED_TILE_SIZE,
                    highlightPaint);
        }

        for (GameCharacter gc : gameCharacters) {
            if (camera.getBoundingBox().contains(gc.getX(), gc.getY())) {
                gc.draw(canvas, paint);
            }
        }

        highlightPaint.setColor(hostileTileColor);

        for (int j = 0; j < tileMap.getHostileMatrixRows(); j++) {
            for (int i = 0; i < tileMap.getHostileMatrixCols(); i++) {
                if (tileMap.isHostile(i, j)) {
                    int x = ((i - camera.getX()) * SCALED_TILE_SIZE) - camera.getDiffX();
                    int y = ((j - camera.getY()) * SCALED_TILE_SIZE) - camera.getDiffY();
                    canvas.drawRect(x, y, x + SCALED_TILE_SIZE, y + SCALED_TILE_SIZE,
                            highlightPaint);
                }
            }
        }
    }
}
