package com.lmweav.schoolquest.minigames;

import android.animation.ValueAnimator;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.lmweav.schoolquest.Game;
import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.utilities.TextBoxStructure;

import androidx.core.content.ContextCompat;

import static com.lmweav.schoolquest.Constants.DT_INDEX;
import static com.lmweav.schoolquest.Constants.FT_INDEX;
import static com.lmweav.schoolquest.Constants.GRADE_INCREASE;
import static com.lmweav.schoolquest.Constants.LESSON_MAX_SKILL;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: MiniGame
 * This abstract class is used to define mini games. As mini games are controlled by UI buttons,
 * there is no need to declare an update method.
 *
 * Methods in this class define common mini game UI animations and finish the mini game to return
 * to the main game view.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public abstract class MiniGame {

    int id;
    int score;

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    void addScore(int score) { this.score += score; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    void barAnimation(final ViewGroup.LayoutParams params, int currentValue,
                             final int maxValue, final ImageView bar) {
        int newWidth = currentValue == 0 ? 1 :
                (int) (bar.getDrawable().getIntrinsicWidth() *
                        ((float) currentValue / (float) maxValue));
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(params.width, newWidth);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.width = (int) animation.getAnimatedValue();
                int current = (int) (((float)((int) animation.getAnimatedValue()) /
                        (float) bar.getDrawable().getIntrinsicWidth()) * maxValue);
                if (current * 5 <= maxValue) {
                    bar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorRed));
                } else if (current * 2 <= maxValue) {
                    bar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorYellow));
                } else {
                    bar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorGreen));
                }
                bar.setLayoutParams(params);
                if (GAME.getMiniGame() == null) {
                    params.width = 1;
                    bar.setLayoutParams(params);
                    valueAnimator.cancel();
                }
            }
        });
        valueAnimator.start();
        bar.setLayoutParams(params);
    }

    void tempBarAnimation(final ViewGroup.LayoutParams params, int currentValue,
                          final int permValue, final ImageView bar,
                          final ImageView permBar) {
        int newWidth = currentValue == 0 ? 1 :
                (int) (bar.getDrawable().getIntrinsicWidth() *
                        ((float) currentValue / (float) LESSON_MAX_SKILL));
        ValueAnimator valueAnimator = ValueAnimator.ofInt(params.width, newWidth);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.width = (int) animation.getAnimatedValue();
                int current = (int) (((float)((int) animation.getAnimatedValue()) /
                        (float) bar.getDrawable().getIntrinsicWidth()) * LESSON_MAX_SKILL);
                if ((permValue + current) * 5 <= LESSON_MAX_SKILL) {
                    bar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorRed));
                    permBar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorRed));
                } else if ((permValue + current) * 2 <= LESSON_MAX_SKILL) {
                    bar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorYellow));
                    permBar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorYellow));
                } else {
                    bar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorGreen));
                    permBar.setColorFilter(ContextCompat.getColor(GameActivity.getInstance(),
                            R.color.colorGreen));
                }
                bar.setLayoutParams(params);
            }
        });
        valueAnimator.start();
        bar.setLayoutParams(params);
    }

    void setUpTextBoxArrowAnimation(ImageView textboxArrow) {
        TranslateAnimation textBoxArrowAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.01f);
        textBoxArrowAnimation.setDuration(500);
        textBoxArrowAnimation.setRepeatCount(-1);
        textBoxArrowAnimation.setRepeatMode(Animation.RESTART);
        textBoxArrowAnimation.setInterpolator(new LinearInterpolator());
        textBoxArrowAnimation.setFillAfter(true);

        textboxArrow.setAnimation(textBoxArrowAnimation);
    }

    public void endLesson() {
        int oldPoint = GAME.getGradeScore(id);
        if (score != 0) {
            GAME.addPointChange(GRADE_INCREASE, id, score);
            GAME.increaseGradeScore(id, score);
        }
        int newPoint = GAME.getGradeScore(id);

        if (oldPoint / 10 != newPoint / 10 && oldPoint < 30) {
            final String lesson = Game.getSubjectName(id);

            final String grade;
            switch (newPoint / 10) {
                case 1:
                    grade = "a C";
                    if (id == DT_INDEX) { GAME.getProgressDataStructure().setMadeCraft(0); }
                    if (id == FT_INDEX) { GAME.getProgressDataStructure().setMadeSnack(0); }
                    break;
                case 2:
                    grade = "a B";
                    if (id == DT_INDEX) { GAME.getProgressDataStructure().setMadeCraft(1); }
                    if (id == FT_INDEX) { GAME.getProgressDataStructure().setMadeSnack(1); }
                    break;
                default:
                    grade = "an A";
                    if (id == DT_INDEX) { GAME.getProgressDataStructure().setMadeCraft(2); }
                    if (id == FT_INDEX) { GAME.getProgressDataStructure().setMadeSnack(2); }
                    break;
            }
            GAME.setLoadingScreen(true);
            GAME.playJingle(R.raw._jingle_rank_up);
            GameActivity.getInstance().displayTextBox(new TextBoxStructure(
                    "> Your grade has increased! You now have " + grade + " in " + lesson + "!",
                    new Runnable() {
                        @Override
                        public void run() {
                            final String before = Game.getTimeKey(GAME.getTime()).toUpperCase();
                            GAME.setTime(GAME.getTime() + 1);
                            GAME.setMiniGame(null);
                            final String after = Game.getTimeKey(GAME.getTime()).toUpperCase();
                            GameActivity.getInstance().setSlideLoadingTransition(before, after);
                            GAME.reloadMap();
                            GameActivity.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GameActivity.getInstance().refreshHUD();
                                }
                            });
                        }
                    }, true, null));
        } else {
            final String before = Game.getTimeKey(GAME.getTime()).toUpperCase();
            GAME.setTime(GAME.getTime() + 1);
            GAME.setMiniGame(null);
            final String after = Game.getTimeKey(GAME.getTime()).toUpperCase();
            GameActivity.getInstance().setSlideLoadingTransition(before, after);
            GAME.reloadMap();
            GameActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GameActivity.getInstance().refreshHUD();
                }
            });
        }
    }

}
