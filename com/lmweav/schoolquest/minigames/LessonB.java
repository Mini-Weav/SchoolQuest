package com.lmweav.schoolquest.minigames;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.google.android.flexbox.FlexboxLayout;
import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.GameView;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.items.Item;
import com.lmweav.schoolquest.utilities.GameTextView;
import com.lmweav.schoolquest.utilities.TextBoxStructure;

import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.lmweav.schoolquest.Constants.CHEM_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.CRAFT_D_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK0_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK1_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK2_INDEX;
import static com.lmweav.schoolquest.Constants.DT_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.DT_INDEX;
import static com.lmweav.schoolquest.Constants.DT_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.FOOD_D_INDEX;
import static com.lmweav.schoolquest.Constants.FT_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.FT_INDEX;
import static com.lmweav.schoolquest.Constants.FT_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.GREAT_CONDITION;
import static com.lmweav.schoolquest.Constants.ICT_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.LESSON_MAX_ATTN;
import static com.lmweav.schoolquest.Constants.LESSON_MAX_SKILL;
import static com.lmweav.schoolquest.Constants.PE_INDEX;
import static com.lmweav.schoolquest.Constants.SFX_BUFF;
import static com.lmweav.schoolquest.Constants.SFX_CLICK;
import static com.lmweav.schoolquest.Constants.SFX_DEBUFF;
import static com.lmweav.schoolquest.Constants.SFX_POINT;
import static com.lmweav.schoolquest.Constants.UNWELL_CONDITION;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: LessonB
 * This subclass of minigame is used for dt and food tech lessons.
 *
 * Methods in this class refresh the minigame HUD and define the behaviours that are called by the
 * UI buttons.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public class LessonB extends MiniGame {

    private int time;
    private int skill;
    private int tempSkill;
    private int attn;
    private int numberOfQuestions;
    private int questionsLeft;
    private int questionsCompleted;
    private int timePerAction;

    private int bookIndex;
    private int drinkIndex;

    private double lv0;
    private double lv1;

    private float questionScale;
    private String questionText;

    private boolean help;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public LessonB(int type) {
        time = 60;

        this.id = type;

        if (id != DT_INDEX && id != FT_INDEX) { throw new IllegalArgumentException(); }

        int gradePoint = GAME.getGradeScore(id);
        int gradeIndex = gradePoint > 29 ? 3 : gradePoint / 10;

        ImageView answerButton = GameActivity.getInstance().findViewById(R.id.lesson_b_answer_button);

        if (id == DT_INDEX) {
            if (GAME.hasItem(Item.getItem(DT_BOOK_INDEX))) { bookIndex = DT_BOOK_INDEX; }
            else { bookIndex = DT_SHEET_INDEX; }
            lv0 = 0.3;
            lv1 = 0.7;
            answerButton.setImageBitmap(BitmapFactory.decodeResource(
                    GameActivity.getInstance().getResources(), R.drawable._ui_lesson_b_answer_dt));
        }
        else {
            if (GAME.hasItem(Item.getItem(FT_BOOK_INDEX))) { bookIndex = FT_BOOK_INDEX; }
            else { bookIndex = FT_SHEET_INDEX; }
            lv0 = 0.2;
            lv1 = 0.8;
            answerButton.setImageBitmap(BitmapFactory.decodeResource(
                    GameActivity.getInstance().getResources(), R.drawable._ui_lesson_b_answer_ft));
        }

        if (GAME.hasItem(Item.getItem(DRINK2_INDEX))) { drinkIndex = DRINK2_INDEX; }
        else if (GAME.hasItem(Item.getItem(DRINK1_INDEX))) { drinkIndex = DRINK1_INDEX; }
        else { drinkIndex = DRINK0_INDEX; }

        switch (gradeIndex) {
            case 0:
                numberOfQuestions = 4;
                skill = 1;
                break;
            case 1:
                numberOfQuestions = 6;
                skill = 2;
                break;
            case 2:
                numberOfQuestions = 8;
                skill = 3;
                break;
            case 3:
                numberOfQuestions = 10;
                skill = 4;
                break;
            default:
                throw new IllegalArgumentException();
        }
        questionsLeft = numberOfQuestions;

        int pePoint = GAME.getGradeScore(PE_INDEX);
        int peIndex = pePoint > 29 ? 3 : pePoint / 10;

        attn = peIndex + 1;
        if (GAME.getPlayer().getCondition() == GREAT_CONDITION) { attn++; }
        else if (GAME.getPlayer().getCondition() == UNWELL_CONDITION) { attn--; }

        if (GAME.getPlayer().hasBuff(id)) { timePerAction = 4; }
        else { timePerAction = 5; }

        tempSkill = 0;

        ImageView bookIcon = GameActivity.getInstance().findViewById(R.id.lesson_b_book_icon);
        bookIcon.setImageBitmap(Item.getItem(bookIndex).getIcon());

        generateQuestion();
        setButtons();
        displayText(questionText);
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public void setDrinkIndex(int index) {
        if (index == DRINK0_INDEX || index == DRINK1_INDEX || index == DRINK2_INDEX) {
            drinkIndex = index;
        }
    }

    public String getQuestionText() { return questionText; }

    public void setHelp(boolean help) { this.help = help; }
    public boolean isHelp() { return help; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public void refreshHUD() {
        final GameActivity gameActivity = GameActivity.getInstance();

        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView skillBar = gameActivity.findViewById(R.id.lesson_b_skill_bar_fill);
                final ViewGroup.LayoutParams skillParams = skillBar.getLayoutParams();

                barAnimation(skillParams, skill, LESSON_MAX_SKILL, skillBar);

                final ImageView tempSkillBar =
                        gameActivity.findViewById(R.id.lesson_b_skill_temp_bar_fill);
                final ViewGroup.LayoutParams tempSkillParams = tempSkillBar.getLayoutParams();

                tempBarAnimation(tempSkillParams, tempSkill, skill,
                        tempSkillBar, skillBar);

                ImageView attnBar = gameActivity.findViewById(R.id.lesson_b_attn_bar_fill);
                ViewGroup.LayoutParams attnParams = attnBar.getLayoutParams();

                barAnimation(attnParams, attn, LESSON_MAX_ATTN, attnBar);

                ImageView timeBar = gameActivity.findViewById(R.id.lesson_b_time_bar_fill);
                ViewGroup.LayoutParams timeParams = timeBar.getLayoutParams();

                barAnimation(timeParams, time, 60, timeBar);

                GameTextView labelText = gameActivity.findViewById(R.id.lesson_b_textbox_label_text);
                labelText.setText(
                        String.format(Locale.ENGLISH, "%d", questionsLeft) + " tasks left");

                GameTextView drinkQuantity = gameActivity.findViewById(
                        R.id.lesson_b_drink_quantity_text);
                drinkQuantity.setText("x" + GAME.getItemQuantity(Item.getItem(drinkIndex)));

                ImageView drinkIcon = gameActivity.findViewById(R.id.lesson_b_drink_icon);
                drinkIcon.setImageBitmap(Item.getItem(drinkIndex).getIcon());

                GameTextView bookQuantity = gameActivity.findViewById(
                        R.id.lesson_b_book_quantity_text);
                bookQuantity.setText("x" + GAME.getItemQuantity(Item.getItem(bookIndex)));
            }
        });
    }

    public void resetBars() {
        skill = 0;
        tempSkill = 0;
        attn = 0;
        time = 0;
        ImageView attnBar = GameActivity.getInstance().findViewById(R.id.lesson_b_attn_bar_fill);
        ViewGroup.LayoutParams attnParams = attnBar.getLayoutParams();
        attnParams.width = 1;
        attnBar.setLayoutParams(attnParams);

        ImageView skillBar = GameActivity.getInstance().findViewById(R.id.lesson_b_skill_bar_fill);
        ViewGroup.LayoutParams skillParams = skillBar.getLayoutParams();
        skillParams.width = 1;
        skillBar.setLayoutParams(skillParams);

        ImageView tempSkillBar =
                GameActivity.getInstance().findViewById(R.id.lesson_b_skill_temp_bar_fill);
        ViewGroup.LayoutParams tempSkillParams = tempSkillBar.getLayoutParams();
        tempSkillParams.width = 1;
        tempSkillBar.setLayoutParams(tempSkillParams);

        ImageView timeBar = GameActivity.getInstance().findViewById(R.id.lesson_b_time_bar_fill);
        ViewGroup.LayoutParams timeParams = timeBar.getLayoutParams();
        timeParams.width = 1;
        timeBar.setLayoutParams(timeParams);

        ImageView craftBar1 = GameActivity.getInstance().findViewById(R.id.lesson_b_craft_bar_1point);
        craftBar1.setScaleX(0);

        ImageView craftBar2 = GameActivity.getInstance().findViewById(R.id.lesson_b_craft_bar_2point);
        craftBar2.setScaleX(0);
    }

    public void setButtons() {
        final GameActivity gameActivity = GameActivity.getInstance();

        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView drinkButton = gameActivity.findViewById(R.id.lesson_b_drink_button);
                ImageView drinkIcon = gameActivity.findViewById(R.id.lesson_b_drink_icon);
                GameTextView drinkQuantity = gameActivity.
                        findViewById(R.id.lesson_b_drink_quantity_text);

                ImageView bookButton = gameActivity.findViewById(R.id.lesson_b_book_button);
                ImageView bookIcon = gameActivity.findViewById(R.id.lesson_b_book_icon);
                GameTextView bookQuantity = gameActivity.
                        findViewById(R.id.lesson_b_book_quantity_text);

                ImageView rereadButton = gameActivity.findViewById(R.id.lesson_b_reread_button);
                GameTextView rereadText = gameActivity.findViewById(R.id.lesson_b_reread_text);

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

                if (skill + tempSkill == LESSON_MAX_SKILL && !help) {
                    rereadButton.setEnabled(false);
                    rereadButton.setAlpha(0.5f);
                    rereadText.setBackgroundColor(Color.TRANSPARENT);
                    rereadText.setAlpha(0.5f);
                } else {
                    rereadButton.setEnabled(true);
                    rereadButton.setAlpha(1f);
                    rereadText.setBackgroundColor(
                            gameActivity.getResources().getColor(R.color.colorBlue));
                    rereadText.setAlpha(1f);
                }
            }
        });
    }

    public void setUpSliderAnimation() {
        GameActivity gameActivity = GameActivity.getInstance();

        ImageView slider = gameActivity.findViewById(R.id.lesson_b_craft_bar_slider);
        ImageView craftBar = gameActivity.findViewById(R.id.lesson_b_craft_bar);

        int craftBarWidth = craftBar.getDrawable().getIntrinsicWidth();

        ObjectAnimator sliderAnimation = ObjectAnimator.ofFloat(slider, "translationX",
                -(craftBarWidth / 2) + (craftBarWidth / 27),
                (craftBarWidth / 2) - (craftBarWidth / 27));

        int baseSliderTime = 250;
        sliderAnimation.setDuration(baseSliderTime + (attn * baseSliderTime));
        sliderAnimation.setRepeatCount(-1);
        sliderAnimation.setRepeatMode(ValueAnimator.REVERSE);
        sliderAnimation.setInterpolator(new LinearInterpolator());

        sliderAnimation.start();
    }

    public void setCraftBarWidth() {
        GameActivity gameActivity = GameActivity.getInstance();

        final ImageView craftBar1point = gameActivity.findViewById(R.id.lesson_b_craft_bar_1point);
        setScaleAnimation(craftBar1point, (skill + tempSkill) + questionScale);

        final ImageView craftBar2point = gameActivity.findViewById(R.id.lesson_b_craft_bar_2point);
        setScaleAnimation(craftBar2point, (1.5f * (skill + tempSkill)) + ((3 * questionScale) / 2));
    }

    private void setScaleAnimation(View view, float toScale) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", toScale);
                scaleX.setDuration(500);
                scaleX.start();
    }

    public void exampleQuestion() {
        questionScale = 0;
        setCraftBarWidth();
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
        time -= timePerAction;
        displayFeedbackText(new TextBoxStructure("> You drink a powerful energy drink... " +
                "Your energy has increased!"));
        setUpSliderAnimation();
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
        time -= timePerAction;
        setCraftBarWidth();
        GAME.playSFX(SFX_BUFF);
    }

    public void reread() {
        tempSkill++;
        time -= timePerAction;
        displayFeedbackText(new TextBoxStructure("> You reread the task instruction... Your skill " +
                "has increased until you finish the current task!"));
        setCraftBarWidth();
        GAME.playSFX(SFX_BUFF);
    }

    public void answer() {
        int score;

        GameActivity gameActivity = GameActivity.getInstance();

        ImageView craftBarSlider = gameActivity.findViewById(R.id.lesson_b_craft_bar_slider);
        ImageView craftBar1point = gameActivity.findViewById(R.id.lesson_b_craft_bar_1point);
        ImageView craftBar2point = gameActivity.findViewById(R.id.lesson_b_craft_bar_2point);

        if (isViewOverlapping(craftBarSlider, craftBar2point)) {
            if (!isViewOverlapping(craftBarSlider, craftBar1point)) {
                score = 2;
                displayFeedbackText(new TextBoxStructure("> You completed the task to an " +
                        "excellent standard!", new Runnable() {
                    @Override
                    public void run() {
                        if (questionsLeft > 0) { setCraftBarWidth(); }
                    }
                }, true, null));
            }
            else {
                score = 1;
                displayFeedbackText(new TextBoxStructure("> You completed the task to a good " +
                        "standard!", new Runnable() {
                    @Override
                    public void run() {
                        if (questionsLeft > 0) { setCraftBarWidth(); }
                    }
                }, true, null));
            }
            tempSkill = 0;
            questionsLeft--;
            addScore(score);
            questionsCompleted++;
            if (questionsLeft != 0) { generateQuestion(); }
            GAME.playSFX(SFX_POINT);
        }
        else {
            if (time <= timePerAction) {
                displayFeedbackText(new TextBoxStructure("> You failed to complete the task."));
            }
            else {
                displayFeedbackText(new TextBoxStructure(
                        "> You failed to complete the task. Move on to the next task?",
                        new Runnable() {
                            @Override
                            public void run() {
                                tempSkill = 0;
                                questionsLeft--;
                                if (questionsLeft != 0) {
                                    generateQuestion();
                                    setCraftBarWidth();
                                }
                            }
                        }, false, null));
            }
            GAME.playSFX(SFX_DEBUFF);
        }
        if (attn != 0) {
            attn--;
            setUpSliderAnimation();
        }
        time -= timePerAction;
    }

    private boolean isViewOverlapping(View firstView, View secondView) {
        int[] firstPosition = new int[2];
        int[] secondPosition = new int[2];

        firstView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        firstView.getLocationOnScreen(firstPosition);
        secondView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        secondView.getLocationOnScreen(secondPosition);

        float firstWidth = firstView.getMeasuredWidth() * firstView.getScaleX();
        float secondWidth = secondView.getMeasuredWidth() * secondView.getScaleX();

        return firstPosition[0] < secondPosition[0] + secondWidth
                && firstPosition[0] + firstWidth > secondPosition[0];
    }

    private void generateQuestion() {
        double r = Math.random();

        if (r < lv0) {
            questionScale = 0.5f;
            questionText = "> This task seems quite simple.";
        }
        else if (r < lv1) {
            questionScale = 0;
            questionText = "> This task seems pretty standard.";
        }
        else {
            questionScale = -0.5f;
            questionText = "> This task seems fairly complex.";
        }
    }

    public void displayText(String text) {
        GameTextView lessonBTextBox = GameActivity.getInstance().
                findViewById(R.id.lesson_b_textbox_box_text);
        lessonBTextBox.setText(text);
    }

    private void displayFeedbackText(final TextBoxStructure textBox) {
        displayText(textBox.getText());

        GameActivity gameActivity = GameActivity.getInstance();


        final ConstraintLayout lessonB = gameActivity.findViewById(R.id.lesson_b);
        final GameView gameView = gameActivity.findViewById(R.id.game_surface);

        lessonB.setAlpha(0.5f);
        gameView.setAlpha(0.5f);

        final ConstraintLayout lessonBTextbox = gameActivity.findViewById(R.id.lesson_b_textbox);
        final ImageView lessonBTextboxArrow =
                gameActivity.findViewById(R.id.lesson_b_textbox_box_arrow);

        final ImageView craftBarSlider = gameActivity.findViewById(R.id.lesson_b_craft_bar_slider);
        craftBarSlider.setVisibility(View.GONE);

        lessonBTextbox.setClickable(true);

        if (textBox.getRunnable1() == null) {
            setUpTextBoxArrowAnimation(lessonBTextboxArrow);
            lessonBTextboxArrow.setVisibility(View.VISIBLE);
            lessonBTextbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GAME.playSFX(SFX_CLICK);
                    lessonBTextboxArrow.clearAnimation();
                    lessonBTextboxArrow.setVisibility(View.INVISIBLE);
                    lessonB.setAlpha(1f);
                    gameView.setAlpha(1f);
                    lessonBTextbox.setClickable(false);
                    craftBarSlider.setVisibility(View.VISIBLE);
                    if (questionsCompleted == numberOfQuestions) {
                        questionsCompleted++;
                        String text;
                        String grade;
                        switch (GAME.getGradeScore(id) / 10) {
                            case 0:
                                grade = "a D";
                                break;
                            case 1:
                                grade = "a C";
                                break;
                            case 2:
                                grade = "a B";
                                break;
                            default:
                                grade = "an A";
                                break;
                        }
                        int craftId =
                                GAME.getGradeScore(id) /10 >= 3 ? 3 : GAME.getGradeScore(id) / 10;
                        if (id == DT_INDEX) {
                            text = "> You successfully made " + grade + " Grade Craft!";
                            GAME.addItem(Item.getItem(CRAFT_D_INDEX + craftId));
                            GAME.getProgressDataStructure().setMadeCraft(
                                    GAME.getGradeScore(id) / 10);
                        }
                        else {
                            text = "> You successfully made " + grade + " Grade Snack!";
                            GAME.addItem(Item.getItem(FOOD_D_INDEX + craftId));
                            GAME.getProgressDataStructure().setMadeSnack(
                                    GAME.getGradeScore(id) / 10);
                        }
                        GAME.playJingle(R.raw._jingle_get_item);
                        displayFeedbackText(new TextBoxStructure(text));
                    }
                    else if (questionsLeft == 0 || time == 0) { endLesson(); }
                    else { displayText(questionText); }
                    refreshHUD();
                }
            });
        } else {

            if (textBox.isNoButton()) {
                lessonBTextbox.setClickable(true);
                setUpTextBoxArrowAnimation(lessonBTextboxArrow);
                lessonBTextboxArrow.setVisibility(View.VISIBLE);
                lessonBTextbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GAME.playSFX(SFX_CLICK);
                        textBox.getRunnable1().run();
                        lessonBTextboxArrow.clearAnimation();
                        lessonBTextboxArrow.setVisibility(View.INVISIBLE);
                        lessonB.setAlpha(1f);
                        gameView.setAlpha(1f);
                        lessonBTextbox.setClickable(false);
                        craftBarSlider.setVisibility(View.VISIBLE);
                        if (questionsCompleted == numberOfQuestions) {
                            questionsCompleted++;
                            String text;
                            String grade;
                            switch (GAME.getGradeScore(id) / 10) {
                                case 0:
                                    grade = "a D";
                                    break;
                                case 1:
                                    grade = "a C";
                                    break;
                                case 2:
                                    grade = "a B";
                                    break;
                                default:
                                    grade = "an A";
                                    break;
                            }
                            int craftId = GAME.getGradeScore(id) / 10 >= 3 ?
                                    3 : GAME.getGradeScore(id) /10;
                            if (id == DT_INDEX) {
                                text = "> You successfully made " + grade + " Grade Craft!";
                                GAME.addItem(Item.getItem(CRAFT_D_INDEX + craftId));
                                GAME.getProgressDataStructure().setMadeCraft(craftId);
                            }
                            else {
                                text = "> You successfully cooked " + grade + " Grade Snack!";
                                GAME.addItem(Item.getItem(FOOD_D_INDEX + craftId));
                                GAME.getProgressDataStructure().setMadeSnack(craftId);
                            }
                            GAME.playJingle(R.raw._jingle_get_item);
                            displayFeedbackText(new TextBoxStructure(text));
                        }
                        else if (questionsLeft == 0 || time == 0) { endLesson(); }
                        else { displayText(questionText); }
                        refreshHUD();
                    }
                });
            } else {
                lessonBTextbox.setOnClickListener(null);

                final FlexboxLayout lessonBTextBoxButtons = gameActivity.
                        findViewById(R.id.lesson_b_textbox_box_buttons);

                ConstraintLayout lessonBTextBoxButtonYes = gameActivity.
                        findViewById(R.id.lesson_b_textbox_box_buttons_yes);
                ConstraintLayout lessonBTextBoxButtonNo = gameActivity.
                        findViewById(R.id.lesson_b_textbox_box_buttons_no);

                lessonBTextBoxButtons.setVisibility(View.VISIBLE);

                lessonBTextBoxButtonYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textBox.getRunnable1().run();
                        GAME.playSFX(SFX_CLICK);
                        lessonB.setAlpha(1f);
                        gameView.setAlpha(1f);
                        lessonBTextBoxButtons.setVisibility(View.GONE);
                        lessonBTextbox.setClickable(false);
                        craftBarSlider.setVisibility(View.VISIBLE);
                        if (questionsCompleted == numberOfQuestions) {
                            questionsCompleted++;
                            String text;
                            String grade;
                            switch (GAME.getGradeScore(id) / 10) {
                                case 0:
                                    grade = "a D";
                                    break;
                                case 1:
                                    grade = "a C";
                                    break;
                                case 2:
                                    grade = "a B";
                                    break;
                                default:
                                    grade = "an A";
                                    break;
                            }
                            if (id == DT_INDEX) {
                                text = "> You successfully made " + grade + " Grade Craft!";
                            } else { text = "> You successfully cooked " + grade + " Grade Snack!"; }
                            GAME.playJingle(R.raw._jingle_get_item);
                            displayFeedbackText(new TextBoxStructure(text));
                        }
                        else if (questionsLeft == 0 || time == 0) { endLesson(); }
                        else { displayText(questionText); }
                        refreshHUD();
                    }
                });
                lessonBTextBoxButtonNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (textBox.getRunnable2() != null) { textBox.getRunnable2().run(); }
                        GAME.playSFX(SFX_CLICK);
                        lessonB.setAlpha(1f);
                        gameView.setAlpha(1f);
                        lessonBTextBoxButtons.setVisibility(View.GONE);
                        lessonBTextbox.setClickable(false);
                        craftBarSlider.setVisibility(View.VISIBLE);
                        if (questionsCompleted == numberOfQuestions) {
                            questionsCompleted++;
                            String text;
                            String grade;
                            switch (GAME.getGradeScore(id) / 10) {
                                case 0:
                                    grade = "a D";
                                    break;
                                case 1:
                                    grade = "a C";
                                    break;
                                case 2:
                                    grade = "a B";
                                    break;
                                default:
                                    grade = "an A";
                                    break;
                            }
                            if (id == DT_INDEX) {
                                text = "> You successfully made " + grade + " grade craft!";
                            } else { text = "> You successfully cooked " + grade + " grade snack!"; }
                            GAME.playJingle(R.raw._jingle_get_item);
                            displayFeedbackText(new TextBoxStructure(text));
                        }
                        else if (questionsLeft == 0 || time == 0) { endLesson(); }
                        else { displayText(questionText); }
                        refreshHUD();
                    }
                });
            }
        }

    }

    public void endLesson() {
        int score = (int) (((float) this.score / (float) (numberOfQuestions * 2)) * 100);
        String text = "> The lesson is over! You scored " + score + "% on the task sheet.";
        displayText(text);

        this.score = score / 20;

        final GameActivity gameActivity = GameActivity.getInstance();
        setUpTextBoxArrowAnimation(
                (ImageView) gameActivity.findViewById(R.id.lesson_b_textbox_box_arrow));

        final ConstraintLayout lessonB = gameActivity.findViewById(R.id.lesson_b);
        final GameView gameView = gameActivity.findViewById(R.id.game_surface);
        final ImageView lessonBCraftBarSlider =
                gameActivity.findViewById(R.id.lesson_b_craft_bar_slider);

        lessonB.setAlpha(0.5f);
        gameView.setAlpha(0.5f);
        lessonBCraftBarSlider.setVisibility(View.GONE);

        final ConstraintLayout lessonBTextbox = gameActivity.findViewById(R.id.lesson_b_textbox);
        final ImageView lessonBTextboxArrow =
                gameActivity.findViewById(R.id.lesson_b_textbox_box_arrow);
        lessonBTextboxArrow.setVisibility(View.VISIBLE);

        lessonBTextbox.setClickable(true);
        lessonBTextbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lessonBTextboxArrow.clearAnimation();
                lessonBTextboxArrow.setVisibility(View.INVISIBLE);
                lessonB.setAlpha(1f);
                gameView.setAlpha(1f);
                lessonBCraftBarSlider.setVisibility(View.VISIBLE);
                lessonBTextbox.setClickable(false);
                lessonB.setVisibility(View.GONE);
                lessonBTextbox.setVisibility(View.GONE);
                gameActivity.showButtons();
                resetBars();
                LessonB.super.endLesson();
            }
        });
    }
}
