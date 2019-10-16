package com.lmweav.schoolquest.minigames;

import android.graphics.Color;
import android.graphics.Point;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.flexbox.FlexboxLayout;
import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.GameView;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.items.Item;
import com.lmweav.schoolquest.utilities.GameTextView;
import com.lmweav.schoolquest.utilities.TextBoxStructure;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.lmweav.schoolquest.Constants.CHEM_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK0_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK1_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK2_INDEX;
import static com.lmweav.schoolquest.Constants.GREAT_CONDITION;
import static com.lmweav.schoolquest.Constants.ICT_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.LESSON_MAX_ATTN;
import static com.lmweav.schoolquest.Constants.LESSON_MAX_SKILL;
import static com.lmweav.schoolquest.Constants.MAP_SCHOOL_YARD_ID;
import static com.lmweav.schoolquest.Constants.OBJECT_DIRECTION_DOWN;
import static com.lmweav.schoolquest.Constants.PE_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.PE_END_X;
import static com.lmweav.schoolquest.Constants.PE_END_Y;
import static com.lmweav.schoolquest.Constants.PE_INDEX;
import static com.lmweav.schoolquest.Constants.PE_MAP_LEFT_MARGIN;
import static com.lmweav.schoolquest.Constants.PE_MAP_TOP_MARGIN;
import static com.lmweav.schoolquest.Constants.PE_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.SCREEN_DENSITY;
import static com.lmweav.schoolquest.Constants.SFX_BUFF;
import static com.lmweav.schoolquest.Constants.SFX_CLICK;
import static com.lmweav.schoolquest.Constants.SFX_DEBUFF;
import static com.lmweav.schoolquest.Constants.SFX_POINT;
import static com.lmweav.schoolquest.Constants.UNWELL_CONDITION;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: LessonC
 * This subclass of minigame is used for pe lessons.
 *
 * Methods in this class refresh the minigame HUD and define the behaviours that are called by the
 * UI buttons. There are also methods that are called as the player moves around and interacts
 * with the map.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class LessonC extends MiniGame {

    private int time;
    private int skill;
    private int tempSkill;
    private int attn;
    private int numberOfQuestions;
    private int questionsLeft;
    private int runTime;
    private int restTime;
    private int mapTime;

    private final int PLAYER_SPEED;


    private int bookIndex;
    private int drinkIndex;

    private float timeDecrease = 0;
    private float currentRunTime;
    private int startTime;

    private boolean help;
    private boolean running;

    private int oldIndex = -1;
    private SparseArray<ArrayList<Point>> points = new SparseArray<>();
    private Point currentPoint;

    private final static Point[] RED_ZONE_POINTS = new Point[] {
            new Point(22, 34), new Point(24, 35), new Point(25, 32),
            new Point(30, 34), new Point(32, 35), new Point(36, 35)
    };

    private final static Point[] BLUE_ZONE_POINTS = new Point[] {
            new Point(50, 32), new Point(42,36), new Point(46,34),
            new Point(47, 34), new Point(47, 35), new Point(48, 35)
    };

    private final static Point[] YELLOW_ZONE_POINTS = new Point[] {
            new Point(42, 20), new Point(36, 21), new Point(37, 21),
            new Point(37, 23), new Point(40, 25), new Point(41, 29)
    };

    private final static Point[] GREEN_ZONE_POINTS = new Point[] {
            new Point(24, 19), new Point(30, 19), new Point(26, 21),
            new Point(27, 24), new Point(30, 29), new Point(22, 33)
    };

    private final static Point[] PURPLE_ZONE_POINTS = new Point[] {
            new Point(48, 12), new Point(47, 15), new Point(45, 18),
            new Point(44, 20), new Point(45, 30), new Point(46, 30)
    };

    private final static Point[] PINK_ZONE_POINTS = new Point[] {
            new Point(23, 16), new Point(25, 13), new Point(27, 15),
            new Point(28, 12), new Point(30, 13), new Point(32, 16),
            new Point(33, 12), new Point(35, 14), new Point(37, 11),
            new Point(37, 15), new Point(39, 13), new Point(39, 16)
    };

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public LessonC() {
        time = 60;
        startTime = 60;

        id = PE_INDEX;

        PLAYER_SPEED = GAME.getPlayer().getSpeed();

        int gradePoint = GAME.getGradeScore(id);
        int gradeIndex = gradePoint > 29 ? 3 : gradePoint / 10;

        if (GAME.hasItem(Item.getItem(PE_BOOK_INDEX))) { bookIndex = PE_BOOK_INDEX; }
        else { bookIndex = PE_SHEET_INDEX; }

        if (GAME.hasItem(Item.getItem(DRINK2_INDEX))) { drinkIndex = DRINK2_INDEX; }
        else if (GAME.hasItem(Item.getItem(DRINK1_INDEX))) { drinkIndex = DRINK1_INDEX; }
        else { drinkIndex = DRINK0_INDEX; }

        switch (gradeIndex) {
            case 0:
                numberOfQuestions = 4;
                skill = 1;
                mapTime = 6;
                break;
            case 1:
                numberOfQuestions = 6;
                skill = 2;
                mapTime = 5;
                break;
            case 2:
                numberOfQuestions = 8;
                skill = 3;
                mapTime = 4;
                break;
            case 3:
                numberOfQuestions = 10;
                skill = 4;
                mapTime = 3;
                break;
            default:
                throw new IllegalArgumentException();
        }
        questionsLeft = numberOfQuestions;

        attn = gradeIndex + 1;

        if (GAME.getPlayer().getCondition() == GREAT_CONDITION) { attn++; }
        else if (GAME.getPlayer().getCondition() == UNWELL_CONDITION) { attn--; }

        if (GAME.getPlayer().hasBuff(id)) {
            runTime = 10;
            restTime = 5;
        }
        else {
            runTime = 5;
            restTime = 10;
        }

        tempSkill = 0;

        GAME.getPlayer().setSpeed(2);

        ImageView bookIcon = GameActivity.getInstance().findViewById(R.id.lesson_c_book_icon);
        bookIcon.setImageBitmap(Item.getItem(bookIndex).getIcon());

        setButtons();
        refreshHUD();
        setControlPoints(gradeIndex);
        generatePoint();
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public int getTime() { return time; }

    public void setDrinkIndex(int index) {
        if (index == DRINK0_INDEX || index == DRINK1_INDEX || index == DRINK2_INDEX) {
            drinkIndex = index;
        }
    }

    public void setHelp(boolean help) { this.help = help; }
    public boolean isHelp() { return help; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public void refreshHUD() {
        final GameActivity gameActivity = GameActivity.getInstance();

        if (time < 0) { time = 0; }

        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView skillBar = gameActivity.findViewById(R.id.lesson_c_skill_bar_fill);
                final ViewGroup.LayoutParams skillParams = skillBar.getLayoutParams();

                barAnimation(skillParams, skill, LESSON_MAX_SKILL, skillBar);

                final ImageView tempSkillBar =
                        gameActivity.findViewById(R.id.lesson_c_skill_temp_bar_fill);
                final ViewGroup.LayoutParams tempSkillParams = tempSkillBar.getLayoutParams();

                tempBarAnimation(tempSkillParams, tempSkill, skill,
                        tempSkillBar, skillBar);

                ImageView attnBar = gameActivity.findViewById(R.id.lesson_c_attn_bar_fill);
                ViewGroup.LayoutParams attnParams = attnBar.getLayoutParams();

                barAnimation(attnParams, attn, LESSON_MAX_ATTN, attnBar);

                ImageView timeBar = gameActivity.findViewById(R.id.lesson_c_time_bar_fill);
                ViewGroup.LayoutParams timeParams = timeBar.getLayoutParams();

                barAnimation(timeParams, time, 60, timeBar);

                GameTextView drinkQuantity = gameActivity.findViewById(
                        R.id.lesson_c_drink_quantity_text);
                drinkQuantity.setText("x" + GAME.getItemQuantity(Item.getItem(drinkIndex)));

                ImageView drinkIcon = gameActivity.findViewById(R.id.lesson_c_drink_icon);
                drinkIcon.setImageBitmap(Item.getItem(drinkIndex).getIcon());

                GameTextView bookQuantity = gameActivity.findViewById(
                        R.id.lesson_c_book_quantity_text);
                bookQuantity.setText("x" + GAME.getItemQuantity(Item.getItem(bookIndex)));

                GameTextView tasksLeft = gameActivity.findViewById(R.id.lesson_c_points_text_title);
                tasksLeft.setText("" + questionsLeft);
            }
        });
    }

    public void resetBars() {
        skill = 0;
        tempSkill = 0;
        attn = 0;
        time = 0;
        ImageView attnBar = GameActivity.getInstance().findViewById(R.id.lesson_c_attn_bar_fill);
        ViewGroup.LayoutParams attnParams = attnBar.getLayoutParams();
        attnParams.width = 1;
        attnBar.setLayoutParams(attnParams);

        ImageView skillBar = GameActivity.getInstance().findViewById(R.id.lesson_c_skill_bar_fill);
        ViewGroup.LayoutParams skillParams = skillBar.getLayoutParams();
        skillParams.width = 1;
        skillBar.setLayoutParams(skillParams);

        ImageView tempSkillBar =
                GameActivity.getInstance().findViewById(R.id.lesson_c_skill_temp_bar_fill);
        ViewGroup.LayoutParams tempSkillParams = tempSkillBar.getLayoutParams();
        tempSkillParams.width = 1;
        tempSkillBar.setLayoutParams(tempSkillParams);

        ImageView timeBar = GameActivity.getInstance().findViewById(R.id.lesson_c_time_bar_fill);
        ViewGroup.LayoutParams timeParams = timeBar.getLayoutParams();
        timeParams.width = 1;
        timeBar.setLayoutParams(timeParams);
    }

    public void setButtons() {
        final GameActivity gameActivity = GameActivity.getInstance();

        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView drinkButton = gameActivity.findViewById(R.id.lesson_c_drink_button);
                ImageView drinkIcon = gameActivity.findViewById(R.id.lesson_c_drink_icon);
                GameTextView drinkQuantity = gameActivity.
                        findViewById(R.id.lesson_c_drink_quantity_text);

                ImageView bookButton = gameActivity.findViewById(R.id.lesson_c_book_button);
                ImageView bookIcon = gameActivity.findViewById(R.id.lesson_c_book_icon);
                GameTextView bookQuantity = gameActivity.
                        findViewById(R.id.lesson_c_book_quantity_text);

                ImageView mapButton = gameActivity.findViewById(R.id.lesson_c_map_button);
                GameTextView mapText = gameActivity.findViewById(R.id.lesson_c_map_text);

                ImageView runButton = gameActivity.findViewById(R.id.lesson_c_run_button);
                GameTextView runText = gameActivity.findViewById(R.id.lesson_c_run_text);

                ImageView waitButton = gameActivity.findViewById(R.id.lesson_c_wait_button);
                GameTextView waitText = gameActivity.findViewById(R.id.lesson_c_wait_text);

                if (GAME.getItemQuantity(Item.getItem(drinkIndex)) == 0 && !help) {
                    drinkButton.setEnabled(false);
                    drinkButton.setAlpha(0.5f);
                    drinkIcon.setAlpha(0.5f);
                    drinkQuantity.setBackgroundColor(Color.TRANSPARENT);
                    drinkQuantity.setTextColor(gameActivity.getResources().
                            getColor(R.color.colorRedHostile));
                    drinkQuantity.setAlpha(0.5f);
                } else {
                    drinkButton.setEnabled(true);
                    drinkButton.setAlpha(1f);
                    drinkIcon.setAlpha(1f);
                    drinkQuantity.setBackgroundColor(
                            gameActivity.getResources().getColor(R.color.colorBlue));
                    drinkQuantity.setTextColor(gameActivity.getResources().
                            getColor(R.color.colorWhiteFont));
                    drinkQuantity.setAlpha(1f);
                }

                if ((GAME.getItemQuantity(Item.getItem(bookIndex)) == 0 ||
                        skill + tempSkill == LESSON_MAX_SKILL) && !help) {
                    bookButton.setEnabled(false);
                    bookButton.setAlpha(0.5f);
                    bookIcon.setAlpha(0.5f);
                    bookQuantity.setBackgroundColor(Color.TRANSPARENT);
                    if (GAME.getItemQuantity(Item.getItem(bookIndex)) == 0) {
                        bookQuantity.setTextColor(gameActivity.getResources().
                                getColor(R.color.colorRedHostile));
                    }
                    bookQuantity.setAlpha(0.5f);
                } else {
                    bookButton.setEnabled(true);
                    bookButton.setAlpha(1f);
                    bookIcon.setAlpha(1f);
                    bookQuantity.setBackgroundColor(
                            gameActivity.getResources().getColor(R.color.colorBlue));
                    bookQuantity.setTextColor(gameActivity.getResources().
                            getColor(R.color.colorWhiteFont));
                    bookQuantity.setAlpha(1f);
                }

                if (time < mapTime && !help) {
                    mapButton.setEnabled(false);
                    mapButton.setAlpha(0.5f);
                    mapText.setBackgroundColor(Color.TRANSPARENT);
                    mapText.setAlpha(0.5f);
                } else {
                    mapButton.setEnabled(true);
                    mapButton.setAlpha(1f);
                    mapText.setBackgroundColor(
                            gameActivity.getResources().getColor(R.color.colorBlue));
                    mapText.setAlpha(1f);
                }

                if (attn == 0 || running && !help) {
                    runButton.setEnabled(false);
                    runButton.setAlpha(0.5f);
                    runText.setBackgroundColor(Color.TRANSPARENT);
                    runText.setAlpha(0.5f);
                } else {
                    runButton.setEnabled(true);
                    runButton.setAlpha(1f);
                    runText.setBackgroundColor(
                            gameActivity.getResources().getColor(R.color.colorBlue));
                    runText.setAlpha(1f);
                }

                if (time < restTime || running && !help) {
                    waitButton.setEnabled(false);
                    waitButton.setAlpha(0.5f);
                    waitText.setBackgroundColor(Color.TRANSPARENT);
                    waitText.setAlpha(0.5f);
                } else {
                    waitButton.setEnabled(true);
                    waitButton.setAlpha(1f);
                    waitText.setBackgroundColor(
                            gameActivity.getResources().getColor(R.color.colorBlue));
                    waitText.setAlpha(1f);
                }
            }
        });
    }

    private void setControlPoints(int gradeIndex) {
        switch (gradeIndex) {
            case 0:
                points.put(0, new ArrayList<>(Arrays.asList(RED_ZONE_POINTS)));
                points.put(1, new ArrayList<>(Arrays.asList(BLUE_ZONE_POINTS)));
                break;
            case 1:
                points.put(0, new ArrayList<>(Arrays.asList(RED_ZONE_POINTS)));
                points.put(1, new ArrayList<>(Arrays.asList(YELLOW_ZONE_POINTS)));
                points.put(2, new ArrayList<>(Arrays.asList(GREEN_ZONE_POINTS)));
                break;
            case 2:
                points.put(0, new ArrayList<>(Arrays.asList(RED_ZONE_POINTS)));
                points.put(1, new ArrayList<>(Arrays.asList(BLUE_ZONE_POINTS)));
                points.put(2, new ArrayList<>(Arrays.asList(YELLOW_ZONE_POINTS)));
                points.put(3, new ArrayList<>(Arrays.asList(GREEN_ZONE_POINTS)));
                points.put(4, new ArrayList<>(Arrays.asList(PURPLE_ZONE_POINTS)));
                break;
            case 3:
                points.put(0, new ArrayList<>(Arrays.asList(RED_ZONE_POINTS)));
                points.put(1, new ArrayList<>(Arrays.asList(BLUE_ZONE_POINTS)));
                points.put(2, new ArrayList<>(Arrays.asList(YELLOW_ZONE_POINTS)));
                points.put(3, new ArrayList<>(Arrays.asList(GREEN_ZONE_POINTS)));
                points.put(4, new ArrayList<>(Arrays.asList(PURPLE_ZONE_POINTS)));
                points.put(5, new ArrayList<>(Arrays.asList(PINK_ZONE_POINTS)));
                break;
        }
    }

    public void drink() {
        int increase = 0;
        switch (drinkIndex) {
            case DRINK0_INDEX:
                increase = 1;
                GAME.removeItem(Item.getItem(DRINK0_INDEX));
                break;
            case DRINK1_INDEX:
                increase = 2;
                GAME.removeItem(Item.getItem(DRINK1_INDEX));
                break;
            case DRINK2_INDEX:
                increase = 3;
                GAME.removeItem(Item.getItem(DRINK2_INDEX));
                break;
        }
        attn += increase;
        int DRINK_TIME = 2;
        time -= DRINK_TIME;
        displayFeedbackText(new TextBoxStructure("> You drink a powerful energy drink... " +
                "Your energy has increased!"));
        GAME.playSFX(SFX_BUFF);
    }

    public void book() {
        skill++;
        if (bookIndex == CHEM_SHEET_INDEX || bookIndex == ICT_SHEET_INDEX) {
            GAME.removeItem(Item.getItem(bookIndex));
            displayFeedbackText(new TextBoxStructure("> You read the fact sheet... Your skill has " +
                    "increased for the remainder of the lesson!"));
        }
        else {
            displayFeedbackText(new TextBoxStructure("> You glance over the text book... Your " +
                    "skill has increased for the remainder of the lesson!"));
        }
        int BOOK_TIME = 5;
        time -= BOOK_TIME;
        mapTime--;
        GAME.playSFX(SFX_BUFF);
    }

    public void map() {
        time -= mapTime;
        ConstraintLayout mapMenu = GameActivity.getInstance().findViewById(R.id.lesson_c_map_menu);
        ConstraintLayout gameLayout = GameActivity.getInstance().findViewById(R.id.game_layout);
        GAME.playSFX(SFX_CLICK);
        gameLayout.setAlpha(0.5f);
        mapMenu.setVisibility(View.VISIBLE);
    }

    public void running() {
        running = true;
        currentRunTime = 0;
        attn--;
        GAME.getPlayer().setSpeed(4);
        displayFeedbackText(
                new TextBoxStructure("> Your movement speed has increased for a little while."));
        GAME.playSFX(SFX_BUFF);
    }

    public void rest() {
        attn++;
        displayFeedbackText(
                new TextBoxStructure("> You rest for a few minutes... Your energy has increased!"));
        time -= restTime;
        GAME.playSFX(SFX_BUFF);
    }

    private void generatePoint() {
        int r = (int) (Math.random() * points.size());

        while (r == oldIndex) { r = (int) (Math.random() * points.size()); }

        int r0 = (int) (Math.random() * points.get(r).size());

        oldIndex = r;
        currentPoint = points.get(r).remove(r0);

        ImageView mapPoint = GameActivity.getInstance().findViewById(R.id.lesson_c_map_menu_point);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) mapPoint.getLayoutParams();
        layoutParams.topMargin =
                Math.round((currentPoint.y - PE_MAP_TOP_MARGIN) * 8 * SCREEN_DENSITY);
        layoutParams.leftMargin =
                Math.round((currentPoint.x - PE_MAP_LEFT_MARGIN) * 8 * SCREEN_DENSITY);
        mapPoint.setLayoutParams(layoutParams);
    }

    public void step() {

        if (running) {
            timeDecrease += 0.1;
            currentRunTime += 0.2;
        }
        else { timeDecrease += 0.5; }

        if (timeDecrease >= 1) {
            timeDecrease = timeDecrease % 1;
            time--;
            if (currentRunTime >= runTime) {
                running = false;
                GAME.getPlayer().setSpeed(2);
                currentRunTime = 0;
                GAME.playSFX(SFX_DEBUFF);
                GAME.getPlayer().cancelMovement();
            }
            refreshHUD();
            setButtons();
            if (time <= 0) {
                GAME.getPlayer().resetPath();
                GAME.setDestination(null);
                GameActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLesson();
                    }
                });
            }
        }

    }

    public boolean isCurrentPoint(int x, int y) {
        return currentPoint.equals(x, y);
    }

    public void foundPoint() {
        int pointTime = startTime - time;
        startTime = time;
        questionsLeft--;
        GAME.playSFX(SFX_POINT);
        refreshHUD();
        score++;
        String text;
        if (pointTime <= 60 / numberOfQuestions) {
            score++;
            text = "> You found the current control point! You got extra credit for finding " +
                    "the point quickly!";
        } else {
            text = "> You found the current control point! You add the next control point to your " +
                    "map.";
        }
        generatePoint();
        displayFeedbackText(new TextBoxStructure(
                text,
                new Runnable() {
                    @Override
                    public void run() {
                        if (questionsLeft != 0) {
                            ConstraintLayout mapMenu =
                                    GameActivity.getInstance().findViewById(R.id.lesson_c_map_menu);
                            ConstraintLayout gameLayout =
                                    GameActivity.getInstance().findViewById(R.id.game_layout);
                            gameLayout.setAlpha(0.5f);
                            mapMenu.setVisibility(View.VISIBLE);
                        }
                    }
                }, true, null));
    }

    public void displayText(String text) {
        GameTextView lessonCTextBox = GameActivity.getInstance().
                findViewById(R.id.lesson_c_textbox_box_text);
        lessonCTextBox.setText(text);
    }

    private void displayFeedbackText(final TextBoxStructure textBox) {
        displayText(textBox.getText());

        GameActivity gameActivity = GameActivity.getInstance();


        final ConstraintLayout lessonC = gameActivity.findViewById(R.id.lesson_c);
        final GameView gameView = gameActivity.findViewById(R.id.game_surface);

        lessonC.setAlpha(0.5f);
        gameView.setAlpha(0.5f);

        final ConstraintLayout lessonCTextbox = gameActivity.findViewById(R.id.lesson_c_textbox);
        final ImageView lessonCTextboxArrow =
                gameActivity.findViewById(R.id.lesson_c_textbox_box_arrow);

        final ConstraintLayout lessonCTextBoxBox =
                gameActivity.findViewById(R.id.lesson_c_textbox_box);
        lessonCTextBoxBox.setVisibility(View.VISIBLE);

        lessonCTextbox.setOnClickListener(null);

        if (textBox.getRunnable1() == null) {
            setUpTextBoxArrowAnimation(lessonCTextboxArrow);
            lessonCTextboxArrow.setVisibility(View.VISIBLE);
            lessonCTextbox.setClickable(true);
            lessonCTextbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GAME.playSFX(SFX_CLICK);
                    lessonCTextboxArrow.clearAnimation();
                    lessonCTextBoxBox.setVisibility(View.GONE);
                    lessonCTextboxArrow.setVisibility(View.INVISIBLE);
                    lessonC.setAlpha(1f);
                    gameView.setAlpha(1f);
                    lessonCTextbox.setClickable(false);
                    if (questionsLeft == 0 || time == 0) { endLesson(); }
                    refreshHUD();
                }
            });
        } else {
            if (textBox.isNoButton()) {
                setUpTextBoxArrowAnimation(lessonCTextboxArrow);
                lessonCTextboxArrow.setVisibility(View.VISIBLE);
                lessonCTextbox.setClickable(true);
                lessonCTextbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textBox.getRunnable1().run();
                        GAME.playSFX(SFX_CLICK);
                        lessonCTextboxArrow.clearAnimation();
                        lessonCTextBoxBox.setVisibility(View.GONE);
                        lessonCTextboxArrow.setVisibility(View.INVISIBLE);
                        lessonC.setAlpha(1f);
                        gameView.setAlpha(1f);
                        lessonCTextbox.setClickable(false);
                        if (questionsLeft == 0 || time == 0) { endLesson(); }
                        refreshHUD();
                    }
                });
            } else {
                final FlexboxLayout lessonCTextBoxButtons = gameActivity.
                        findViewById(R.id.lesson_a_textbox_box_buttons);

                ConstraintLayout lessonCTextBoxButtonYes = gameActivity.
                        findViewById(R.id.lesson_a_textbox_box_buttons_yes);
                ConstraintLayout lessonCTextBoxButtonNo = gameActivity.
                        findViewById(R.id.lesson_a_textbox_box_buttons_no);

                lessonCTextBoxButtons.setVisibility(View.VISIBLE);

                lessonCTextBoxButtonYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textBox.getRunnable1().run();
                        GAME.playSFX(SFX_CLICK);
                        lessonC.setAlpha(1f);
                        gameView.setAlpha(1f);
                        lessonCTextBoxBox.setVisibility(View.GONE);
                        lessonCTextBoxButtons.setVisibility(View.GONE);
                        lessonCTextbox.setClickable(false);
                        if (questionsLeft == 0 || time == 0) { endLesson(); }
                        refreshHUD();
                    }
                });
                lessonCTextBoxButtonNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (textBox.getRunnable2() != null) { textBox.getRunnable2().run(); }
                        GAME.playSFX(SFX_CLICK);
                        lessonC.setAlpha(1f);
                        gameView.setAlpha(1f);
                        lessonCTextBoxBox.setVisibility(View.GONE);
                        lessonCTextBoxButtons.setVisibility(View.GONE);
                        lessonCTextbox.setClickable(false);
                        if (questionsLeft == 0 || time == 0) { endLesson(); }
                        refreshHUD();
                    }
                });
            }
        }

    }

    public void endLesson() {
        int score = (int) ( ((float) this.score / (float) (numberOfQuestions * 2)) * 100);
        String text = "> The lesson is over! You scored " + score + "% on the course.";
        final ConstraintLayout textBox =
                GameActivity.getInstance().findViewById(R.id.lesson_c_textbox_box);
        textBox.setVisibility(View.VISIBLE);
        displayText(text);

        this.score = score / 20;

        final GameActivity gameActivity = GameActivity.getInstance();
        setUpTextBoxArrowAnimation(
                (ImageView) gameActivity.findViewById(R.id.lesson_c_textbox_box_arrow));

        final ConstraintLayout lessonC = gameActivity.findViewById(R.id.lesson_c);
        final GameView gameView = gameActivity.findViewById(R.id.game_surface);

        lessonC.setAlpha(0.5f);
        gameView.setAlpha(0.5f);

        final ConstraintLayout lessonCTextbox = gameActivity.findViewById(R.id.lesson_c_textbox);
        final ImageView lessonCTextboxArrow =
                gameActivity.findViewById(R.id.lesson_c_textbox_box_arrow);
        lessonC.setVisibility(View.VISIBLE);
        lessonCTextboxArrow.setVisibility(View.VISIBLE);

        lessonCTextbox.setClickable(true);
        lessonCTextbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textBox.setVisibility(View.GONE);
                lessonCTextboxArrow.clearAnimation();
                lessonCTextboxArrow.setVisibility(View.INVISIBLE);
                lessonC.setAlpha(1f);
                gameView.setAlpha(1f);
                lessonCTextbox.setClickable(false);
                lessonC.setVisibility(View.GONE);
                lessonCTextbox.setVisibility(View.GONE);
                gameActivity.showButtons();
                GAME.getPlayer().setPoint(PE_END_X, PE_END_Y);
                GAME.getPlayer().changeTile("default");
                GAME.getPlayer().rotate(OBJECT_DIRECTION_DOWN);
                GAME.loadMap(MAP_SCHOOL_YARD_ID);
                GAME.getPlayer().setSpeed(PLAYER_SPEED);
                resetBars();
                LessonC.super.endLesson();
            }
        });
    }
}
