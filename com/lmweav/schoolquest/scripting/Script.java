package com.lmweav.schoolquest.scripting;

import android.content.Context;
import android.util.SparseArray;

import com.lmweav.schoolquest.Game;
import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.characters.GameCharacter;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.characters.NPCDataStructure;
import com.lmweav.schoolquest.tiles.TileMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;

import static com.lmweav.schoolquest.Game.GAME;
import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: Script
 *
 * This class runs an in-game script by executing a list of game characters with a stack of
 * commands.
 *
 * Methods in this class parse the external script file and execute the created script object.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class Script {
    private static SparseArray<Script> scripts = new SparseArray<>();

    private int playerX;
    private int playerY;
    private int playerDirection;
    private int oldSpeed;

    private int bgmId;

    private boolean loaded;
    private boolean started;
    private boolean finished;
    private boolean skip;
    private boolean skippable;

    private String playerTileSet;

    private HashMap<GameCharacter, LinkedList<Command>> commands = new HashMap<>();
    private HashMap<GameCharacter, LinkedList<Command>> commandsCopy = new HashMap<>();

    private HashMap<String, GameCharacter> actors = new HashMap<>();
    private ArrayList<NPC> npcs = new ArrayList<>();

    private TileMap scriptMap;

    private int[] endInfo = new int[] {-1, -1, -1, -1, -1, -1};

    private Runnable endRunnable = null;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    private Script(Context context, int id, int bgm, boolean skippable) {
        InputStream inputStream = context.getResources().openRawResource(id);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        boolean scriptStart = false;
        boolean scriptEnd = false;
        commands.put(null, new LinkedList<Command>());
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("\\\\\\\\")) {
                    scriptStart = false;
                    scriptEnd = true;
                }
                if (line.contains("////")) {
                    scriptStart = true;
                    scriptEnd = false;
                }
                else {
                    String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                            split("]\\[");
                    if (scriptStart) { readScriptLine(split); }
                    else if (scriptEnd) { readScriptEnd(split); }
                    else { readScriptMeta(context, split); }
                }
            }
            for (GameCharacter gc: actors.values()) {
                if (gc instanceof NPC) { npcs.add((NPC) gc); }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        bgmId = bgm;

        this.skippable = skippable;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public static Script getScript(int index) { return scripts.get(index); }

    public int getMapId() { return scriptMap.getId(); }

    public int getPlayerX() { return playerX; }

    public int getPlayerY() { return playerY; }

    public int getPlayerDirection() { return playerDirection; }

    public void setOldSpeed(int oldSpeed) { this.oldSpeed = oldSpeed; }

    public boolean isLoaded() { return loaded; }
    public void setLoaded(boolean loaded) { this.loaded = loaded; }

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public void setSkip(boolean skip) { this.skip = skip; }

    public boolean isSkippable() { return skippable; }

    public String getPlayerTileSet() { return playerTileSet; }

    public ArrayList<NPC> getNpcs() { return npcs; }

    public Runnable getEndRunnable() { return endRunnable; }
    public void setEndRunnable(Runnable runnable) { this.endRunnable = runnable; }

    public int getBGM() { return bgmId; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public static void loadScripts(Context context) {
        scripts.put(TRACK_CLUB_CUTSCENE, new Script(context, R.raw._cutscene_track_club,
                R.raw._music_activity, true));
        scripts.put(CHEMISTRY_CUTSCENE, new Script(context, R.raw._cutscene_chemistry,
                R.raw._music_activity, true));
        scripts.put(CHEMISTRY_HEIST_CUTSCENE, new Script(context, R.raw._cutscene_chemistry_heist,
                R.raw._music_activity, false));
        scripts.put(DT_HEIST_CUTSCENE, new Script(context, R.raw._cutscene_dt_heist,
                R.raw._music_activity, false));
        scripts.put(TUTORING_CUTSCENE, new Script(context, R.raw._cutscene_tutoring,
                R.raw._music_activity, true));
        scripts.put(TUTORING_HEIST_CUTSCENE, new Script(context, R.raw._cutscene_tutoring_heist,
                R.raw._music_activity, false));
    }

    boolean isCommandFinished(@NonNull GameCharacter actor, int index) {
        LinkedList<Command> commands = commandsCopy.get(actor);
        return commands.isEmpty() || commands.peek().lineIndex > index;
    }

    private void readScriptLine(String[] line) {

        GameCharacter actor = null;
        if (!line[0].equals("ui")) { actor = actors.get(line[0]); }

        String[] commandData = line[1].split("\\|");

        int speed;
        int steps;

        int lineIndex;
        if (commands.get(actor).isEmpty()) { lineIndex = 0; }
        else { lineIndex = commands.get(actor).getLast().lineIndex + 1; }

        Command command;

        switch (commandData[0]) {
            case "path":
                String[] startData = commandData[2].split(",");
                int oldDestX = Integer.parseInt(startData[0]);
                int oldDestY = Integer.parseInt(startData[1]);

                for (int i = 3; i < commandData.length; i++) {
                    String[] pointData = commandData[i].split(",");
                    int destinationX = Integer.parseInt(pointData[0]);
                    int destinationY = Integer.parseInt(pointData[1]);
                    speed = Integer.parseInt(commandData[1]);

                    command = new PathCommand(scriptMap,
                            actor, oldDestX, oldDestY, destinationX, destinationY, speed);
                    command.lineIndex = lineIndex;
                    commands.get(actor).add(command);

                    oldDestX = destinationX;
                    oldDestY = destinationY;

                }
                break;

            case "wait":
                GameCharacter target = actors.get(commandData[1]);
                int index = Integer.parseInt(commandData[2]);

                command = new WaitCommand(this, target, index);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;

            case "up":
                speed = Integer.parseInt(commandData[1]);
                steps = Integer.parseInt(commandData[2]);

                assert actor != null;
                command = new DirectionCommand(scriptMap, actor, OBJECT_DIRECTION_UP, speed, steps);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;
            case "down":
                speed = Integer.parseInt(commandData[1]);
                steps = Integer.parseInt(commandData[2]);

                assert actor != null;
                command = new DirectionCommand(scriptMap, actor, OBJECT_DIRECTION_DOWN, speed, steps);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;
            case "left":
                speed = Integer.parseInt(commandData[1]);
                steps = Integer.parseInt(commandData[2]);

                assert actor != null;
                command = new DirectionCommand(scriptMap, actor, OBJECT_DIRECTION_LEFT, speed, steps);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;
            case "right":
                speed = Integer.parseInt(commandData[1]);
                steps = Integer.parseInt(commandData[2]);

                assert actor != null;
                command = new DirectionCommand(scriptMap, actor, OBJECT_DIRECTION_RIGHT, speed, steps);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;

            case "emotion":
                command = new EmotionCommand(actor, commandData[1], this);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;

            case "text":
                String text = line[2];


                if (line.length > 3) {
                    Runnable runnable = null;
                    switch (line[3]) {
                        case "skip":
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    for (GameCharacter actor: commandsCopy.keySet()) {
                                        if (actor != null) { commandsCopy.remove(actor); }
                                    }
                                }
                            };
                            break;
                    }
                    command = new TextCommand(actor, text, runnable);
                } else { command = new TextCommand(actor, text); }

                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;

            case "loading":
                boolean loading = Boolean.parseBoolean(commandData[1]);
                command = new LoadingCommand(loading);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;

            case "jingle":
                command = new JingleCommand(commandData[1]);
                command.lineIndex = lineIndex;
                commands.get(actor).add(command);
                break;

        }
    }

    private void readScriptEnd(String[] line) {
        String[] endData = line[0].split("\\|");
        switch (endData[0]) {
            case "next":
                endInfo[0] = Integer.parseInt(endData[1]);
                break;
            case "time":
                endInfo[1] = Game.getTimeId(endData[1]);
                break;
            case "map":
                endInfo[2] = Game.getMapId(endData[1]);
                break;
            case "player":
                String[] charInfo = endData[1].split(",");
                endInfo[3] = Integer.parseInt(charInfo[0]);
                endInfo[4] = Integer.parseInt(charInfo[1]);
                switch (charInfo[2]) {
                    case "up":
                        endInfo[5] = OBJECT_DIRECTION_UP;
                        break;
                    case "down":
                        endInfo[5] = OBJECT_DIRECTION_DOWN;
                        break;
                    case "left":
                        endInfo[5] = OBJECT_DIRECTION_LEFT;
                        break;
                    case "right":
                        endInfo[5] = OBJECT_DIRECTION_RIGHT;
                        break;
                }
                break;
        }
    }

    private void readScriptMeta(Context context, String[] line) {
        String[] info = line[0].split("\\|");
        switch (info[0]) {
            case "map":
                int mapId = Game.getMapId(info[1]);

                scriptMap = TileMap.getMap(mapId);
                break;
            case "actor":
                GameCharacter actor;
                String[] charInfo = line[1].split(",");

                int x = Integer.parseInt(charInfo[0]);
                int y = Integer.parseInt(charInfo[1]);

                int direction = 0;
                switch (charInfo[2]) {
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

                switch (info[1]) {
                    case "player":
                        actor = GAME.getPlayer();
                        playerX = x;
                        playerY = y;
                        playerDirection = direction;
                        if (info.length > 2) { playerTileSet = info[2]; }
                        else { playerTileSet = "default"; }
                        break;
                    default:
                        NPCDataStructure data = NPC.getData(info[1]);
                        actor = new NPC(context, x, y, data,
                                NPC.getName(data.getNpcId()), "", direction, false,
                                null);
                        if (info.length > 2) { actor.changeTile(info[2]); }
                        break;
                }
                actors.put(info[1], actor);
                commands.put(actor, new LinkedList<Command>());
        }
    }

    public void copyCommands() {
        commandsCopy = new HashMap<>();
        for (Map.Entry<GameCharacter, LinkedList<Command>> entry : commands.entrySet()) {
            commandsCopy.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        }
    }

    boolean uiFinished() {
        return commandsCopy.get(null).isEmpty();
    }

    public void execute() {

        started = true;

        Set<GameCharacter> actors = commands.keySet();
        ArrayList<LinkedList> comm = new ArrayList<LinkedList>(commandsCopy.values());

        if (skip) {
            for (GameCharacter actor : actors) {
                LinkedList<Command> commands = commandsCopy.get(actor);
                if (commands == null || commands.isEmpty()) {
                    continue;
                }
                commands.peek().reset();
            }
            comm.clear();
        } else {
            for (GameCharacter actor: actors) {
                if (commandsCopy.get(actor).isEmpty()) {
                    continue;
                }
                Command current = commandsCopy.get(actor).peek();
                assert current != null;
                if (current.finished) {
                    current.reset();
                    commandsCopy.get(actor).pop();
                    if (commandsCopy.get(actor).isEmpty()) { continue; }
                    else { current = commandsCopy.get(actor).peek(); }
                }
                assert current != null;
                current.execute();

                if (current.finished) {
                    current.reset();
                    commandsCopy.get(actor).pop();
                    if (!commandsCopy.get(actor).isEmpty()) {
                        current = commandsCopy.get(actor).peek();
                        assert current != null;
                        current.execute();
                    }
                }
            }
        }

        if (allEmpty(comm)) {
            GameActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GameActivity.getInstance().scriptHUD(false);
                    GameActivity.getInstance().runningShoes(oldSpeed == 4);
                }
            });
            finished = true;
            GAME.getPlayer().changeTile("default");
            if (endInfo[3] > -1) {
                GAME.getPlayer().setPoint(endInfo[3], endInfo[4]);
                GAME.getPlayer().rotate(endInfo[5]);
            }
            if (endInfo[1] > -1) {
                int oldTime = GAME.getTime();
                final String before = Game.getTimeKey(oldTime).toUpperCase();

                GAME.setTime(endInfo[1]);
                int newTime = GAME.getTime();
                final String after = Game.getTimeKey(newTime).toUpperCase();

                if (oldTime != newTime) {
                    GameActivity.getInstance().setSlideLoadingTransition(before, after);
                }
            }
            if (endInfo[2] > -1) {
                GAME.loadMap(endInfo[2]);
            }
            else { GAME.reloadMap(); }
            GAME.changeBGM(GAME.getTileMap().getBGM());
        }
    }


    private boolean allEmpty(ArrayList<LinkedList> lists) {
        for (LinkedList list: lists) {
            if (!list.isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
