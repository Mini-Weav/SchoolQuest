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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.lmweav.schoolquest.Constants.CHEMISTRY_INDEX;
import static com.lmweav.schoolquest.Constants.CHEM_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.CHEM_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK0_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK1_INDEX;
import static com.lmweav.schoolquest.Constants.DRINK2_INDEX;
import static com.lmweav.schoolquest.Constants.DT_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.DT_INDEX;
import static com.lmweav.schoolquest.Constants.DT_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.EXAM_INCREASE;
import static com.lmweav.schoolquest.Constants.FT_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.FT_INDEX;
import static com.lmweav.schoolquest.Constants.FT_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.GREAT_CONDITION;
import static com.lmweav.schoolquest.Constants.ICT_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.ICT_INDEX;
import static com.lmweav.schoolquest.Constants.ICT_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.LESSON_MAX_ATTN;
import static com.lmweav.schoolquest.Constants.LESSON_MAX_SKILL;
import static com.lmweav.schoolquest.Constants.PE_BOOK_INDEX;
import static com.lmweav.schoolquest.Constants.PE_INDEX;
import static com.lmweav.schoolquest.Constants.PE_SHEET_INDEX;
import static com.lmweav.schoolquest.Constants.SFX_BUFF;
import static com.lmweav.schoolquest.Constants.SFX_CLICK;
import static com.lmweav.schoolquest.Constants.SFX_DEBUFF;
import static com.lmweav.schoolquest.Constants.SFX_POINT;
import static com.lmweav.schoolquest.Constants.UNWELL_CONDITION;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: Exam
 * This subclass of minigame is used for end game exams.
 *
 * Methods in this class refresh the minigame HUD and define the behaviours that are called by the
 * UI buttons.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class Exam extends MiniGame {

    private int time;
    private int skill;
    private int tempSkill;
    private int attn;
    private int numberOfQuestions;
    private int questionsLeft;
    private int timePerAction;

    private int bookIndex;
    private int drinkIndex;

    private int questionLvl;
    private String questionText;

    private boolean help;

    private static final Integer[] QUESTION_LVLS =
            new Integer[] { 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 };
    private ArrayList<Integer> questions;

    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public Exam(int type) {
        time = 90;

        this.id = type;

        int gradePoint = GAME.getGradeScore(id);
        int gradeIndex = gradePoint > 29 ? 3 : gradePoint / 10;

        switch (id) {
            case DT_INDEX:
                if (GAME.hasItem(Item.getItem(DT_BOOK_INDEX))) { bookIndex = DT_BOOK_INDEX; }
                else { bookIndex = DT_SHEET_INDEX; }
                break;
            case FT_INDEX:
                if (GAME.hasItem(Item.getItem(FT_BOOK_INDEX))) { bookIndex = FT_BOOK_INDEX; }
                else { bookIndex = FT_SHEET_INDEX; }
                break;
            case PE_INDEX:
                if (GAME.hasItem(Item.getItem(PE_BOOK_INDEX))) { bookIndex = PE_BOOK_INDEX; }
                else { bookIndex = PE_SHEET_INDEX; }
                break;
            case CHEMISTRY_INDEX:
                if (GAME.hasItem(Item.getItem(CHEM_BOOK_INDEX))) { bookIndex = CHEM_BOOK_INDEX; }
                else { bookIndex = CHEM_SHEET_INDEX; }
                break;
            case ICT_INDEX:
                if (GAME.hasItem(Item.getItem(ICT_BOOK_INDEX))) { bookIndex = ICT_BOOK_INDEX; }
                else { bookIndex = ICT_SHEET_INDEX; }
                break;
            default:
                throw new IllegalArgumentException();
        }

        if (GAME.hasItem(Item.getItem(DRINK2_INDEX))) { drinkIndex = DRINK2_INDEX; }
        else if (GAME.hasItem(Item.getItem(DRINK1_INDEX))) { drinkIndex = DRINK1_INDEX; }
        else { drinkIndex = DRINK0_INDEX; }

        switch (gradeIndex) {
            case 0:
                skill = 1;
                break;
            case 1:
                skill = 2;
                break;
            case 2:
                skill = 3;
                break;
            case 3:
                skill = 4;
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (GAME.getProgressDataStructure().hasWonHeist()) { skill++; }

        numberOfQuestions = 12;
        questionsLeft = numberOfQuestions;

        int pePoint = GAME.getGradeScore(PE_INDEX);
        int peIndex = pePoint > 29 ? 3 : pePoint / 10;

        attn = peIndex + 1;
        if (GAME.getPlayer().getCondition() == GREAT_CONDITION) { attn++; }
        else if (GAME.getPlayer().getCondition() == UNWELL_CONDITION) { attn--; }


        timePerAction = 5;
        tempSkill = 0;

        questions = new ArrayList<>();
        questions.addAll(Arrays.asList(QUESTION_LVLS));

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

                barAnimation(timeParams, time, 90, timeBar);

                GameTextView labelText = gameActivity.findViewById(R.id.lesson_a_textbox_label_text);
                labelText.setText(
                        String.format(Locale.ENGLISH,"%d", questionsLeft) + " questions left");

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


                drinkButton.setAlpha(0.5f);
                drinkButton.setEnabled(true);
                drinkIcon.setAlpha(0.5f);
                drinkQuantity.setBackgroundColor(Color.TRANSPARENT);
                drinkQuantity.setTextColor(gameActivity.getResources().
                        getColor(R.color.colorRedFont));
                drinkQuantity.setAlpha(0.5f);

                bookButton.setAlpha(0.5f);
                bookButton.setEnabled(true);
                bookIcon.setAlpha(0.5f);
                bookQuantity.setBackgroundColor(Color.TRANSPARENT);
                bookQuantity.setTextColor(gameActivity.getResources().
                        getColor(R.color.colorRedHostile));
                bookQuantity.setAlpha(0.5f);

                if (skill + tempSkill == LESSON_MAX_SKILL && !help) {
                    rereadButton.setEnabled(false);
                    rereadButton.setAlpha(0.5f);
                    rereadText.setText("Read\nQu");
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
        displayFeedbackText(new TextBoxStructure("> You can't drink energy drinks in exams!"));
        GAME.playSFX(SFX_DEBUFF);
    }

    public void book() {
        displayFeedbackText(new TextBoxStructure("> You can't read books in exams!"));
        GAME.playSFX(SFX_DEBUFF);
    }

    public void reread() {
        tempSkill++;
        time -= timePerAction;
        displayFeedbackText(new TextBoxStructure("> You reread the question... Your skill " +
                "has increased until you finish the current question!"));
        GAME.playSFX(SFX_BUFF);
    }

    public void answer0() {
        int score;
        if (skill + tempSkill < questionLvl) {
            if (time <= timePerAction) {
                displayFeedbackText(new TextBoxStructure("> You failed to answer the question."));
            }
            else {
                displayFeedbackText(new TextBoxStructure(
                        "> You failed to answer the question. Move on to the next question?",
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
            if (skill + tempSkill < questionLvl * 2) {
                score = 1;
                displayFeedbackText(new TextBoxStructure("> You feel pretty good about your answer..."));
            }
            else {
                score = 2;
                displayFeedbackText(new TextBoxStructure("> You feel very good about your answer!"));
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
            if (skill + tempSkill >= questionLvl) {
                score = 2;
                displayFeedbackText(new TextBoxStructure("> You feel very good about your answer!"));
            }
            else {
                score = 1;
                displayFeedbackText(new TextBoxStructure("> You feel pretty good about your answer..."));
            }
            tempSkill = 0;
            questionsLeft--;
            addScore(score);
            generateQuestion();
            GAME.playSFX(SFX_POINT);
        }
        else {
            if (time <= timePerAction) {
                displayFeedbackText(new TextBoxStructure("> You failed to answer the question."));
            }
            else {
                displayFeedbackText(new TextBoxStructure(
                        "> You failed to answer the question. Move on to the next question?",
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
        if (questions.size() == 0) { return; }
        int r = (int) (Math.random() * questions.size());

        questionLvl = questions.remove(r);

        switch (questionLvl) {
            case 1:
                questionText = "> This question is fairly easy.";
                break;
            case 2:
                questionText = "> This question is standard.";
                break;
            case 3:
                questionText = "> This question is quite challenging.";
                break;
            case 4:
                questionText = "> This question is very difficult!";
                break;
            default:
                throw new IllegalStateException();

        }
    }

    public void displayText(String text) {
        GameTextView lessonATextBox = GameActivity.getInstance().
                findViewById(R.id.lesson_a_textbox_box_text);
        lessonATextBox.setText(text);
    }

    public void displayFeedbackText(final TextBoxStructure textBox) {
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
        String feedback;
        if (score >= 90) { feedback = "really well!!"; }
        else if (score >= 70) { feedback = "pretty well!"; }
        else if (score >= 40) { feedback = "ok."; }
        else { feedback = "horribly..."; }
        String text = "The exam is over! You feel it went " + feedback;
        displayText(text);

        this.score = score / 10;

        final int finalScore = this.score;

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
                GAME.setMiniGame(null);
                GAME.increaseExamScore(id, finalScore);
                if (id == ICT_INDEX) {
                    GAME.increasePoints(EXAM_INCREASE, id, finalScore);
                    GAME.setEventBGM(R.raw._music_results);
                    GAME.reloadMap();
                    GameActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GameActivity.getInstance().displayEndScreen();
                        }
                    });
                }
                else {
                    GAME.addPointChange(EXAM_INCREASE, id, finalScore);
                    GAME.goHome();
                    GameActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GameActivity.getInstance().refreshHUD();
                        }
                    });
                }
            }
        });
    }
}