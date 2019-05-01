package com.lmweav.schoolquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.lmweav.schoolquest.characters.GameCharacter;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.minigames.LessonC;
import com.lmweav.schoolquest.minigames.MiniGame;
import com.lmweav.schoolquest.utilities.TextBoxStructure;
import com.lmweav.schoolquest.utilities.pathfinding.AStarPathFinder;
import com.lmweav.schoolquest.utilities.pathfinding.Path;

import static com.lmweav.schoolquest.Game.*;

import static com.lmweav.schoolquest.Constants.*;

/*
 * School Quest: GameView
 * This class is the custom view that holds the game (tilemap and objects, not UI).
 *
 * Methods in this class handle the surface by implementing the SurfaceHolder.Callback interface.
 * A thread is started on creation of the view, which calls the game's update and draw methods.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-14
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final Matrix translationMatrix = new Matrix();

    private GameThread thread;

    private Paint paint;


    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        thread = new GameThread(getHolder(), this);

        setWillNotDraw(false);

        setFocusable(true);

        if (((GameActivity) context).isNewGame()) {
            GAME.newGame(context, ((GameActivity) context).getPlayerName());
        }
        else { GAME.load(context.getApplicationContext(), (GameActivity) context); }

        paint = new Paint();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);

        thread = new GameThread(getHolder(), this);

        setWillNotDraw(false);

        setFocusable(true);

        if (((GameActivity) context).isNewGame()) {
            GAME.newGame(context, ((GameActivity) context).getPlayerName());
        }
        else { GAME.load(context.getApplicationContext(), (GameActivity) context); }

        paint = new Paint();
    }


    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public void update() {
        GAME.update();
        MiniGame miniGame = GAME.getMiniGame();

        if (GAME.getPlayer().isMoving() || GameActivity.getInstance().isGamePause()
                || GAME.isPlayerSpottedByNPC()) {
            if (miniGame == null) {
                GameActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GameActivity.getInstance().disableButtons();
                    }
                });
            } else {
                if (miniGame instanceof LessonC && !((LessonC) miniGame).isHelp()) {
                    GameActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GameActivity.getInstance().disableLessonCButtons();
                        }
                    });
                }
            }

        } else {
            if (GAME.getMiniGame() == null) {
                GameActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GameActivity.getInstance().enableButtons();
                    }
                });
            } else {
                GameActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GameActivity.getInstance().enableLessonCButtons();
                    }
                });
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            translationMatrix.setTranslate(HORIZONTAL_OFFSET, VERTICAL_OFFSET);
            canvas.concat(translationMatrix);
            GAME.draw(canvas, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (GAME.getScript() != null || GAME.getLoading() || GAME.isPlayerSpottedByNPC()) {
            return false;
        }

        float rawX = event.getRawX();
        float rawY = event.getRawY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            int endX = (int) rawX;
            int endY = (int) rawY;

            if (GAME.getWaitingChar() != null) { GAME.getWaitingChar().setWillWait(false); }
            GAME.setWaitingChar(null);


            if (GAME.getPlayer().getPath() == null) {
                Point logicalPoint = GAME.convertToLogical((endX - HORIZONTAL_OFFSET),
                        endY - VERTICAL_OFFSET);
                AStarPathFinder pathFinder =
                        new AStarPathFinder(GAME.getTileMap(), 30);
                Path path = pathFinder.findPath(GAME.getPlayer().getX(), GAME.getPlayer().getY(),
                        logicalPoint.x, logicalPoint.y);

                if (path != null) {
                    if (GAME.isGameCharacterInMap(logicalPoint.x, logicalPoint.y)) {
                        GameCharacter object =
                                GAME.getGameCharacterFromMap(logicalPoint.x, logicalPoint.y);
                        if (object instanceof NPC) {
                            if (GAME.getProgressDataStructure().isCatchNPCInteraction()) {
                                GameActivity.getInstance().displayTextBox(new TextBoxStructure(
                                        GAME.getProgressDataStructure().getCatchNPCInteractionText()
                                ));
                                return false;
                            }
                            GAME.setWaitingChar((NPC) object);
                            ((NPC) object).setWillWait(true);
                        }
                    }

                    GAME.playSFX(SFX_MOVE);
                    GAME.setDestination(logicalPoint);
                    GAME.getPlayer().setGoal(logicalPoint.x, logicalPoint.y);
                    GAME.getPlayer().setPath(path);
                }
            }
            else {
                GAME.getPlayer().cancelMovement();
                Point logicalPoint = GAME.convertToLogical((endX - HORIZONTAL_OFFSET),
                        endY - VERTICAL_OFFSET);
                GAME.getPlayer().setNewGoal(logicalPoint);
            }
        }

        return false;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = thread.getState() == Thread.State.TERMINATED ?
                new GameThread(getHolder(), this) : thread;
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }
}
