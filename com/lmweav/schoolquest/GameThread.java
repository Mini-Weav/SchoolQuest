package com.lmweav.schoolquest;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import static com.lmweav.schoolquest.Constants.TARGET_FPS;

/*
 * School Quest: GameThread
 * This class is the thread that runs the game logic and rendering of the game view (separate
 * from UI), as well as controlling the frame rate.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class GameThread extends Thread {

    private final SurfaceHolder surfaceHolder;
    private GameView gameView;

    private boolean running;


    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();

        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    void setRunning(boolean running) { this.running = running; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long targetTime = 1000 / TARGET_FPS;

        while (running) {
            startTime = System.nanoTime();
            Canvas canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    gameView.update();
                    gameView.postInvalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis;

            try {
                sleep(waitTime);
            } catch (Exception e) {
                //Ignore
            }
        }
    }
}
