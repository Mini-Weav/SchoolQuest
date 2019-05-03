package com.lmweav.schoolquest.minigames;

import android.graphics.Color;
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

import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.lmweav.schoolquest.Constants.*;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: LessonA
 * This subclass of minigame is used for chemistry and ict lessons.
 *
 * Methods in this class refresh the minigame HUD and define the behaviours that are called by the
 * UI buttons.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class LessonA extends MiniGame {

    private int time;
    private int skill;
    private int tempSkill;
    private int attn;
    private int numberOfQuestions;
    private int questionsLeft;
    private int timePerAction;

    private int bookIndex;
    private int drinkIndex;

    private double lv0;
    private double lv1;

    private int questionLvl;
    private String questionText;

    private boolean help;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public LessonA(int type) {
        time = 60;

        this.id = type;

        if (id != CHEMISTRY_INDEX && id != ICT_INDEX) { throw new IllegalArgumentException(); }

        int gradePoint = GAME.getGradeScore(id);
        int gradeIndex = gradePoint > 29 ? 3 : gradePoint / 10;

        if (id == CHEMISTRY_INDEX) {
            if (GAME.hasItem(Item.getItem(CHEM_BOOK_INDEX))) { bookIndex = CHEM_BOOK_INDEX; }
            else { bookIndex = CHEM_SHEET_INDEX; }
            lv0 = 0.3;
            lv1 = 0.7;
        }
        else {
            if (GAME.hasItem(Item.getItem(ICT_BOOK_INDEX))) { bookIndex = ICT_BOOK_INDEX; }
            else { bookIndex = ICT_SHEET_INDEX; }
            lv0 = 0.2;
            lv1 = 0.8;
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

        ImageView bookIcon = GameActivity.getInstance().findViewById(R.id.lesson_a_book_icon);
        bookIcon.setImageBitmap(Item.getItem(bookIndex).getIcon());

        setButtons();
        refreshHUD();
        generateQuestion();
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
                final ImageView skillBar = gameActivity.findViewById(R.id.lesson_a_skill_bar_fill);
                final ViewGroup.LayoutParams skillParams = skillBar.getLayoutParams();

                barAnimation(skillParams, skill, LESSON_MAX_SKILL, skillBar);

                final ImageView tempSkillBar =
                        gameActivity.findViewById(R.id.lesson_a_skill_temp_bar_fill);
                final ViewGroup.LayoutParams tempSkillParams = tempSkillBar.getLayoutParams();

                tempBarAnimation(tempSkillParams, tempSkill, skill,
                        tempSkillBar, skillBar);

                ImageView attnBar = gameActivity.findViewById(R.id.lesson_a_attn_bar_fill);
                ViewGroup.LayoutParams attnParams = attnBar.getLayoutParams();

                barAnimation(attnParams, attn, LESSON_MAX_ATTN, attnBar);

                ImageView timeBar = gameActivity.findViewById(R.id.lesson_a_time_bar_fill);
                ViewGroup.LayoutParams timeParams = timeBar.getLayoutParams();

                barAnimation(timeParams, time, 60, timeBar);

                GameTextView labelText = gameActivity.findViewById(R.id.lesson_a_textbox_label_text);
                labelText.setText(
                        String.format(Locale.ENGLISH, "%d", questionsLeft) + " tasks left");

                GameTextView drinkQuantity = gameActivity.findViewById(
                        R.id.lesson_a_drink_quantity_text);
                drinkQuantity.setText("x" + GAME.getItemQuantity(Item.getItem(drinkIndex)));

                ImageView drinkIcon = gameActivity.findViewById(R.id.lesson_a_drink_icon);
                drinkIcon.setImageBitmap(Item.getItem(drinkIndex).getIcon());

                GameTextView bookQuantity = gameActivity.findViewById(
                        R.id.lesson_a_book_quantity_text);
                bookQuantity.setText("x" + GAME.getItemQuantity(Item.getItem(bookIndex)));
            }
        });
    }

    public void resetBars() {
        skill = 0;
        tempSkill = 0;
        attn = 0;
        time = 0;
        ImageView attnBar = GameActivity.getInstance().findViewById(R.id.lesson_a_attn_bar_fill);
        ViewGroup.LayoutParams attnParams = attnBar.getLayoutParams();
        attnParams.width = 1;
        attnBar.setLayoutParams(attnParams);

        ImageView skillBar = GameActivity.getInstance().findViewById(R.id.lesson_a_skill_bar_fill);
        ViewGroup.LayoutParams skillParams = skillBar.getLayoutParams();
        skillParams.width = 1;
        skillBar.setLayoutParams(skillParams);

        ImageView tempSkillBar =
                GameActivity.getInstance().findViewById(R.id.lesson_a_skill_temp_bar_fill);
        ViewGroup.LayoutParams tempSkillParams = tempSkillBar.getLayoutParams();
        tempSkillParams.width = 1;
        tempSkillBar.setLayoutParams(tempSkillParams);

        ImageView timeBar = GameActivity.getInstance().findViewById(R.id.lesson_a_time_bar_fill);
        ViewGroup.LayoutParams timeParams = timeBar.getLayoutParams();
        timeParams.width = 1;
        timeBar.setLayoutParams(timeParams);
    }

    public void setButtons() {
        final GameActivity gameActivity = GameActivity.getInstance();

        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView drinkButton = gameActivity.findViewById(R.id.lesson_a_drink_button);
                ImageView drinkIcon = gameActivity.findViewById(R.id.lesson_a_drink_icon);
                GameTextView drinkQuantity = gameActivity.
                        findViewById(R.id.lesson_a_drink_quantity_text);

                ImageView bookButton = gameActivity.findViewById(R.id.lesson_a_book_button);
                ImageView bookIcon = gameActivity.findViewById(R.id.lesson_a_book_icon);
                GameTextView bookQuantity = gameActivity.
                        findViewById(R.id.lesson_a_book_quantity_text);

                ImageView rereadButton = gameActivity.findViewById(R.id.lesson_a_reread_button);
                GameTextView rereadText = gameActivity.findViewById(R.id.lesson_a_reread_text);

                ImageView answer1Button = gameActivity.findViewById(R.id.lesson_a_answer1_button);
                GameTextView answer1Text = gameActivity.findViewById(R.id.lesson_a_answer1_text);

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

                if (attn == 0 || time < timePerAction * 2 && !help) {
                    answer1Button.setEnabled(false);
                    answer1Button.setAlpha(0.5f);
                    answer1Text.setBackgroundColor(Color.TRANSPARENT);
                    answer1Text.setAlpha(0.5f);
                } else {
                    answer1Button.setEnabled(true);
                    answer1Button.setAlpha(1f);
                    answer1Text.setBackgroundColor(
                            gameActivity.getResources().getColor(R.color.colorBlue));
                    answer1Text.setAlpha(1f);
                }
            }
        });
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
        GAME.playSFX(SFX_BUFF);
    }

    public void reread() {
        tempSkill++;
        time -= timePerAction;
        displayFeedbackText(new TextBoxStructure("> You reread the task instruction... Your skill " +
                "has increased until you finish the current task!"));
        GAME.playSFX(SFX_BUFF);
    }

    public void answer0() {
        int score;
        if (skill + tempSkill <= questionLvl) {
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
                                generateQuestion();
                            }
                        }, false, null));
            }
            GAME.playSFX(SFX_DEBUFF);
        }
        else {
            if (skill + tempSkill <= questionLvl * 2) {
                score = 1;
                displayFeedbackText(new TextBoxStructure("> You completed the task to a good " +
                        "standard!"));
            }
            else {
                score = 2;
                displayFeedbackText(new TextBoxStructure("> You completed the task to an " +
                        "excellent standard!"));
            }
            tempSkill = 0;
            questionsLeft--;
            addScore(score);
            generateQuestion();
            GAME.playSFX(SFX_POINT);
        }
        time -= timePerAction;
    }

    public void answer1() {
        attn--;
        int score;
        if (skill + tempSkill >= questionLvl - 1) {
            if (skill + tempSkill > questionLvl) {
                score = 2;
                displayFeedbackText(new TextBoxStructure("> You completed the task to an " +
                        "excellent standard!"));
            }
            else {
                score = 1;
                displayFeedbackText(new TextBoxStructure("> You completed the task to a good " +
                        "standard!"));
            }
            tempSkill = 0;
            questionsLeft--;
            addScore(score);
            generateQuestion();
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
                                generateQuestion();
                            }
                        }, false, null));
            }
            GAME.playSFX(SFX_DEBUFF);
            GAME.playSFX(SFX_DEBUFF);
        }
        time -= timePerAction * 2;
    }

    private void generateQuestion() {
        double r = Math.random();

        if (r < lv0) {
            questionLvl = 1;
            questionText = "> This task seems quite simple.";
        }
        else if (r < lv1) {
            questionLvl = 2;
            questionText = "> This task seems pretty standard.";
        }
        else {
            questionLvl = 3;
            questionText = "> This task seems fairly complex.";
        }
    }

    public void displayText(String text) {
        GameTextView lessonATextBox = GameActivity.getInstance().
                findViewById(R.id.lesson_a_textbox_box_text);
        lessonATextBox.setText(text);
    }

    private void displayFeedbackText(final TextBoxStructure textBox) {
        displayText(textBox.getText());

        GameActivity gameActivity = GameActivity.getInstance();


        final ConstraintLayout lessonA = gameActivity.findViewById(R.id.lesson_a);
        final GameView gameView = gameActivity.findViewById(R.id.game_surface);

        lessonA.setAlpha(0.5f);
        gameView.setAlpha(0.5f);

        final ConstraintLayout lessonATextbox = gameActivity.findViewById(R.id.lesson_a_textbox);
        final ImageView lessonATextboxArrow =
                gameActivity.findViewById(R.id.lesson_a_textbox_box_arrow);

        lessonATextbox.setClickable(true);

        if (textBox.getRunnable1() == null) {
            setUpTextBoxArrowAnimation(lessonATextboxArrow);
            lessonATextboxArrow.setVisibility(View.VISIBLE);
            lessonATextbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GAME.playSFX(SFX_CLICK);
                    lessonATextboxArrow.clearAnimation();
                    lessonATextboxArrow.setVisibility(View.INVISIBLE);
                    lessonA.setAlpha(1f);
                    gameView.setAlpha(1f);
                    lessonATextbox.setClickable(false);
                    if (questionsLeft == 0 || time == 0) { endLesson(); }
                    else { displayText(questionText); }
                    refreshHUD();
                }
            });
        } else {

            lessonATextbox.setOnClickListener(null);

            final FlexboxLayout lessonATextBoxButtons = gameActivity.
                    findViewById(R.id.lesson_a_textbox_box_buttons);

            ConstraintLayout lessonATextBoxButtonYes = gameActivity.
                    findViewById(R.id.lesson_a_textbox_box_buttons_yes);
            ConstraintLayout lessonATextBoxButtonNo = gameActivity.
                    findViewById(R.id.lesson_a_textbox_box_buttons_no);

            lessonATextBoxButtons.setVisibility(View.VISIBLE);

            lessonATextBoxButtonYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textBox.getRunnable1().run();
                    GAME.playSFX(SFX_CLICK);
                    lessonA.setAlpha(1f);
                    gameView.setAlpha(1f);
                    lessonATextBoxButtons.setVisibility(View.GONE);
                    lessonATextbox.setClickable(false);
                    if (questionsLeft == 0 || time == 0) { endLesson(); }
                    else { displayText(questionText); }
                    refreshHUD();
                }
            });
            lessonATextBoxButtonNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textBox.getRunnable2() != null) { textBox.getRunnable2().run(); }
                    GAME.playSFX(SFX_CLICK);
                    lessonA.setAlpha(1f);
                    gameView.setAlpha(1f);
                    lessonATextBoxButtons.setVisibility(View.GONE);
                    lessonATextbox.setClickable(false);
                    if (questionsLeft == 0 || time == 0) { endLesson(); }
                    else { displayText(questionText); }
                    refreshHUD();
                }
            });
        }

    }

    public void endLesson() {
        int score = (int) ( ((float) this.score / (float) (numberOfQuestions * 2)) * 100);
        String text = "> The lesson is over! You scored " + score + "% on the task sheet.";
        displayText(text);

        this.score = score / 20;

        final GameActivity gameActivity = GameActivity.getInstance();
        setUpTextBoxArrowAnimation(
                (ImageView) gameActivity.findViewById(R.id.lesson_a_textbox_box_arrow));

        final ConstraintLayout lessonA = gameActivity.findViewById(R.id.lesson_a);
        final GameView gameView = gameActivity.findViewById(R.id.game_surface);

        lessonA.setAlpha(0.5f);
        gameView.setAlpha(0.5f);

        final ConstraintLayout lessonATextbox = gameActivity.findViewById(R.id.lesson_a_textbox);
        final ImageView lessonATextboxArrow =
                gameActivity.findViewById(R.id.lesson_a_textbox_box_arrow);
        lessonATextboxArrow.setVisibility(View.VISIBLE);

        lessonATextbox.setClickable(true);
        lessonATextbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lessonATextboxArrow.clearAnimation();
                lessonATextboxArrow.setVisibility(View.INVISIBLE);
                lessonA.setAlpha(1f);
                gameView.setAlpha(1f);
                lessonATextbox.setClickable(false);
                lessonA.setVisibility(View.GONE);
                lessonATextbox.setVisibility(View.GONE);
                gameActivity.showButtons();
                resetBars();
                LessonA.super.endLesson();
            }
        });
    }
}
