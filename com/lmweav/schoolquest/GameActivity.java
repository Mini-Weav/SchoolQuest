package com.lmweav.schoolquest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.google.android.flexbox.FlexboxLayout;
import com.lmweav.schoolquest.characters.Emotion;
import com.lmweav.schoolquest.characters.NPC;
import com.lmweav.schoolquest.items.Item;
import com.lmweav.schoolquest.minigames.Exam;
import com.lmweav.schoolquest.minigames.LessonA;
import com.lmweav.schoolquest.minigames.LessonB;
import com.lmweav.schoolquest.minigames.LessonC;
import com.lmweav.schoolquest.minigames.MiniGame;
import com.lmweav.schoolquest.scripting.Script;
import com.lmweav.schoolquest.utilities.BGMFader;
import com.lmweav.schoolquest.utilities.GameTextView;
import com.lmweav.schoolquest.items.ItemImageView;
import com.lmweav.schoolquest.items.ItemRunnable;
import com.lmweav.schoolquest.utilities.TextBoxRunnable;
import com.lmweav.schoolquest.utilities.TextBoxStructure;
import com.lmweav.schoolquest.tiles.TileMap;

import java.util.ArrayList;
import java.util.Locale;

import static com.lmweav.schoolquest.Constants.*;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: GameActivity
 * This class is the Android activity that runs the game.
 *
 * Methods in this class primarily set up, display and alter UI elements based on the game object's
 * current state.
 *
 * @author Luke Weaver
 * @version 1.0.8
 * @since 2019-05-02
 */
public class GameActivity extends Activity {

    private static GameActivity instance = null;

    private boolean newGame;
    private String dataFile = "schoolQuest1.dat";
    private String playerName = "Player";

    private GameTextView currentAnim;
    private ObjectAnimator mapTextAnimator1;
    private ObjectAnimator mapTextAnimator2;
    private ObjectAnimator mapPointAnimator1;
    private ObjectAnimator mapPointAnimator2;
    private Runnable defaultTextBoxRunnable;
    private ItemRunnable inventoryRunnable = new ItemRunnable();
    private ItemRunnable buyRunnable = new ItemRunnable();
    private ItemRunnable sellRunnable = new ItemRunnable();
    private ItemRunnable craftRunnable = new ItemRunnable();

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public static GameActivity getInstance() { return instance; }

    public boolean isNewGame() { return newGame; }

    public String getDataFile() { return dataFile; }

    public String getPlayerName() { return playerName; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newGame = getIntent().getBooleanExtra("newGame", false);
        if (getIntent().getStringExtra("data") != null) { dataFile = getIntent().
                getStringExtra("data"); }
        if (getIntent().getStringExtra("name") != null) { playerName = getIntent().
                getStringExtra("name"); }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        NPC.readNPCNames(this);
        Item.readItems(this);
        NPC.initialiseNPCData();
        NPC.readNPCItems(this);
        TileMap.loadMaps(this);
        Emotion.loadEmotions(this);
        Game.loadGradeImages(this);
        Game.loadFriendImages(this);
        Game.loadStatusImages(this);
        Game.loadSFX(this);
        Game.loadPaint(this);

        setContentView(R.layout.activity_game);
        Script.loadScripts(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        instance = this;

        setUpButtonsAndRunnables();
        refreshHUD();
        disableCancelButton();
        showButtons();
        if (!newGame) { GAME.rateThis(this); }
    }


    public boolean isGamePause() {
        ConstraintLayout statusMenu = findViewById(R.id.status_menu);
        ConstraintLayout mapMenu = findViewById(R.id.map_menu);
        ConstraintLayout inventory = findViewById(R.id.inventory_menu);
        ConstraintLayout textBox = findViewById(R.id.textbox);
        ConstraintLayout studyMenu = findViewById(R.id.study_menu);
        ConstraintLayout buyMenu = findViewById(R.id.buy_menu);

        boolean lessonCHelp = false;
        MiniGame miniGame = GAME.getMiniGame();
        if (miniGame != null) {
            if (miniGame instanceof LessonC) {
                lessonCHelp = ((LessonC) miniGame).isHelp();
            }
        }

        return statusMenu.getVisibility() == View.VISIBLE || mapMenu.getVisibility() == View.VISIBLE
                || inventory.getVisibility() == View.VISIBLE
                || textBox.getVisibility() == View.VISIBLE
                || studyMenu.getVisibility() == View.VISIBLE
                || buyMenu.getVisibility() == View.VISIBLE
                || lessonCHelp;
    }

    private void setUpStatusInfo() {
        GameTextView statusName = findViewById(R.id.status_menu_player_name);
        statusName.setText(GAME.getPlayer().getName());

        ImageView[] gradeRanks = new ImageView[] {
                findViewById(R.id.status_menu_player_grades_rank1),
                findViewById(R.id.status_menu_player_grades_rank2),
                findViewById(R.id.status_menu_player_grades_rank3),
                findViewById(R.id.status_menu_player_grades_rank4) };
        ImageView[] friendRanks = new ImageView[] {
                findViewById(R.id.status_menu_player_friends_rank1),
                findViewById(R.id.status_menu_player_friends_rank2),
                findViewById(R.id.status_menu_player_friends_rank3),
                findViewById(R.id.status_menu_player_friends_rank4) };
        ImageView[] buffs = new ImageView[] {
                findViewById(R.id.status_menu_player_buffs_buff1),
                findViewById(R.id.status_menu_player_buffs_buff2) };

        GAME.updatePointAverages();

        for (int i = 0; i < 4; i++) {
            if (i <= GAME.getAverageGP()) { gradeRanks[i].setImageBitmap(Game.getCheckedBox()); }
            else { gradeRanks[i].setImageBitmap(Game.getUncheckedBox()); }

            if (i <= GAME.getAverageFP()) { friendRanks[i].setImageBitmap(Game.getCheckedBox()); }
            else { friendRanks[i].setImageBitmap(Game.getUncheckedBox()); }
        }

        setUpBuffImage(buffs[0], 0);
        setUpBuffImage(buffs[1], 1);

        ImageView conditionIcon = findViewById(R.id.status_menu_player_condition_icon);
        GameTextView conditionText = findViewById(R.id.status_menu_player_condition_text);

        switch (GAME.getPlayer().getCondition()) {
            case NORMAL_CONDITION:
                conditionText.setText("NORMAL");
                conditionIcon.setImageBitmap(Game.getNormalCondition());
                break;
            case GREAT_CONDITION:
                conditionText.setText("GREAT");
                conditionIcon.setImageBitmap(Game.getGreatCondition());
                break;
            case UNWELL_CONDITION:
                conditionText.setText("POORLY");
                conditionIcon.setImageBitmap(Game.getUnwellCondition());
                break;
            default:
                conditionText.setText("NORMAL");
                conditionIcon.setImageBitmap(Game.getNormalCondition());
                break;
        }
    }

    private void setUpFriendMenu() {
        ImageView athleteButton = findViewById(R.id.status_menu_friends_athlete_button);
        GameTextView athleteTitle = findViewById(R.id.status_menu_friends_athlete_title);
        GameTextView athleteSubTitle = findViewById(R.id.status_menu_friends_athlete_subtitle);

        ImageView classmateButton = findViewById(R.id.status_menu_friends_classmate_button);
        GameTextView classmateTitle = findViewById(R.id.status_menu_friends_classmate_title);
        GameTextView classmateSubTitle = findViewById(R.id.status_menu_friends_classmate_subtitle);

        ImageView nerdButton = findViewById(R.id.status_menu_friends_nerd_button);
        GameTextView nerdTitle = findViewById(R.id.status_menu_friends_nerd_title);
        GameTextView nerdSubTitle = findViewById(R.id.status_menu_friends_nerd_subtitle);

        ImageView delinquentButton = findViewById(R.id.status_menu_friends_delinquent_button);
        GameTextView delinquentTitle = findViewById(R.id.status_menu_friends_delinquent_title);
        GameTextView delinquentSubTitle = findViewById(R.id.status_menu_friends_delinquent_subtitle);

        ImageView tuteeButton = findViewById(R.id.status_menu_friends_tutee_button);
        GameTextView tuteeTitle = findViewById(R.id.status_menu_friends_tutee_title);
        GameTextView tuteeSubTitle = findViewById(R.id.status_menu_friends_tutee_subtitle);

        if (GAME.getFriendScore(ATHLETE_INDEX) == 0) {
            athleteButton.setAlpha(0.5f);
            athleteButton.setEnabled(false);
            athleteTitle.setVisibility(View.GONE);
            athleteSubTitle.setVisibility(View.GONE);
        } else {
            athleteButton.setAlpha(1f);
            athleteButton.setEnabled(true);
            athleteTitle.setVisibility(View.VISIBLE);
            athleteSubTitle.setVisibility(View.VISIBLE);
        }

        if (GAME.getFriendScore(CLASSMATE_INDEX) == 0) {
            classmateButton.setAlpha(0.5f);
            classmateButton.setEnabled(false);
            classmateTitle.setVisibility(View.GONE);
            classmateSubTitle.setVisibility(View.GONE);
        } else {
            classmateButton.setAlpha(1f);
            classmateButton.setEnabled(true);
            classmateTitle.setVisibility(View.VISIBLE);
            classmateSubTitle.setVisibility(View.VISIBLE);
        }

        if (GAME.getFriendScore(NERD_INDEX) == 0) {
            nerdButton.setAlpha(0.5f);
            nerdButton.setEnabled(false);
            nerdTitle.setVisibility(View.GONE);
            nerdSubTitle.setVisibility(View.GONE);
        } else {
            nerdButton.setAlpha(1f);
            nerdButton.setEnabled(true);
            nerdTitle.setVisibility(View.VISIBLE);
            nerdSubTitle.setVisibility(View.VISIBLE);
        }

        if (GAME.getFriendScore(DELINQUENT_INDEX) == 0) {
            delinquentButton.setAlpha(0.5f);
            delinquentButton.setEnabled(false);
            delinquentTitle.setVisibility(View.GONE);
            delinquentSubTitle.setVisibility(View.GONE);
        } else {
            delinquentButton.setAlpha(1f);
            delinquentButton.setEnabled(true);
            delinquentTitle.setVisibility(View.VISIBLE);
            delinquentSubTitle.setVisibility(View.VISIBLE);
        }

        if (GAME.getFriendScore(TUTEE_INDEX) == 0) {
            tuteeButton.setAlpha(0.5f);
            tuteeButton.setEnabled(false);
            tuteeTitle.setVisibility(View.GONE);
            tuteeSubTitle.setVisibility(View.GONE);
        } else {
            tuteeButton.setAlpha(1f);
            tuteeButton.setEnabled(true);
            tuteeTitle.setVisibility(View.VISIBLE);
            tuteeSubTitle.setVisibility(View.VISIBLE);
        }
    }

    private void setUpHeistMenu() {
        findViewById(R.id.status_menu_heist).setVisibility(View.VISIBLE);

        ImageView heistBar1 = findViewById(R.id.status_menu_heist_bar_1);
        ImageView heistBar2 = findViewById(R.id.status_menu_heist_bar_2);
        ImageView heistBar3 = findViewById(R.id.status_menu_heist_bar_3);

        ImageView heistBar4 = findViewById(R.id.status_menu_heist_bar_4);
        GameTextView heistText4 = findViewById(R.id.status_menu_heist_bar_text4);
        ImageView heistBar5 = findViewById(R.id.status_menu_heist_bar_5);
        GameTextView heistText5 = findViewById(R.id.status_menu_heist_bar_text5);
        ImageView heistBar6 = findViewById(R.id.status_menu_heist_bar_6);
        GameTextView heistText6 = findViewById(R.id.status_menu_heist_bar_text6);
        ImageView heistBar7 = findViewById(R.id.status_menu_heist_bar_7);
        GameTextView heistText7 = findViewById(R.id.status_menu_heist_bar_text7);

        if (GAME.hasItem(Item.getItem(KEY3_INDEX))) {
            heistBar1.setImageBitmap(Game.getCheckedBoxHeist());
            heistBar4.setVisibility(View.VISIBLE);
            heistText4.setVisibility(View.VISIBLE);
        }
        if (GAME.hasItem(Item.getItem(KEY0_INDEX))) {
            heistBar2.setImageBitmap(Game.getCheckedBoxHeist());
            heistBar5.setVisibility(View.VISIBLE);
            heistText5.setVisibility(View.VISIBLE);
        }
        if (GAME.hasItem(Item.getItem(KEY4_INDEX)) || GAME.hasItem(Item.getItem(KEY6_INDEX))) {
            heistBar3.setImageBitmap(Game.getCheckedBoxHeist());
            heistBar6.setVisibility(View.VISIBLE);
            heistText6.setVisibility(View.VISIBLE);
        }

        if (GAME.getProgressDataStructure().hasStartedHeist()) {
            heistBar4.setImageBitmap(Game.getCheckedBoxHeist());
        }
        if (GAME.getProgressDataStructure().hasEnteredStaffRoom()) {
            heistBar5.setImageBitmap(Game.getCheckedBoxHeist());
        }
        if (GAME.getProgressDataStructure().hasHackedPC()) {
            heistBar6.setImageBitmap(Game.getCheckedBoxHeist());
            heistBar7.setVisibility(View.VISIBLE);
            heistText7.setVisibility(View.VISIBLE);
        }
        if (GAME.getProgressDataStructure().hasWonHeist()) {
            heistBar7.setImageBitmap(Game.getCheckedBoxHeist());
        }
    }

    private void setUpBuffImage(ImageView view, int index) {
        if (index > 2) { return; }
        switch (GAME.getPlayer().getBuff(index)) {
            case DT_INDEX:
                view.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable._ui_main_status_player_dt_buff));
                view.setVisibility(View.VISIBLE);
                break;
            case FT_INDEX:
                view.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable._ui_main_status_player_ft_buff));
                view.setVisibility(View.VISIBLE);
                break;
            case PE_INDEX:
                view.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable._ui_main_status_player_pe_buff));
                view.setVisibility(View.VISIBLE);
                break;
            case CHEMISTRY_INDEX:
                view.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable._ui_main_status_player_chemistry_buff));
                view.setVisibility(View.VISIBLE);
                break;
            case ICT_INDEX:
                view.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable._ui_main_status_player_ict_buff));
                view.setVisibility(View.VISIBLE);
                break;
            default:
                view.setVisibility(View.GONE);
                break;
        }
    }

    private void setUpGradeInfo(int id) {
        String name;
        String text;
        int gradePoint = GAME.getGradeScore(id);
        int gradeIndex = gradePoint > 29 ? 3 : gradePoint / 10;
        float xpPercent = gradePoint > 29 ? 1 : ((float)(gradePoint % 10)) / 10;

        Bitmap gradeImage = Game.getGradeImage(gradeIndex);
        GameTextView subject = findViewById(R.id.status_menu_grades_info_title);
        ImageView gradeImageView = findViewById(R.id.status_menu_grades_info_grade);
        GameTextView description = findViewById(R.id.status_menu_grades_info_description);
        ImageView xpBar = findViewById(R.id.status_menu_grades_info_xp_bar);
        ViewGroup.LayoutParams params = xpBar.getLayoutParams();

        int skill;
        String attn;
        String timePerAction;
        String runTime;
        String restTime;
        int numberOfQuestions;
        int mapTime;

        int pePoint = GAME.getGradeScore(PE_INDEX);
        int peIndex = pePoint > 29 ? 3 : pePoint / 10;

        attn = "" + (peIndex+ 1);
        if (GAME.getPlayer().getCondition() == GREAT_CONDITION) { attn = attn + "(+1)"; }
        else if (GAME.getPlayer().getCondition() == UNWELL_CONDITION) { attn = attn + "(-1)"; }

        if (GAME.getPlayer().hasBuff(id)) {
            timePerAction = "5(-1)";
            runTime = "5(+5)";
            restTime = "10(-5)";
        }
        else {
            timePerAction = "5";
            runTime = "5";
            restTime = "10";
        }

        switch (id) {
            case DT_INDEX:
                name = "DT";
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
                text = "Craft an item in DT.\n\n" +
                        "Skill: " + skill + "\n\nEnergy: " + attn +
                        "\n\nTasks: " + numberOfQuestions + "\n\nTime per action: " +
                        timePerAction;
                break;
            case FT_INDEX:
                name = "Food Tech";
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
                text = "Cook a snack in Food Tech.\n\n" +
                        "Skill: " + skill + "\n\nEnergy: " + attn +
                        "\n\nTasks: " + numberOfQuestions + "\n\nTime per action: " +
                        timePerAction;
                break;
            case PE_INDEX:
                name = "PE";
                switch (gradeIndex) {
                    case 0:
                        numberOfQuestions = 4;
                        mapTime = 6;
                        break;
                    case 1:
                        numberOfQuestions = 6;
                        mapTime = 5;
                        break;
                    case 2:
                        numberOfQuestions = 8;
                        mapTime = 4;
                        break;
                    case 3:
                        numberOfQuestions = 10;
                        mapTime = 3;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                text = "Find hidden points in PE.\n\n" +
                        "Energy: " + attn +
                        "\n\nPoints: " + numberOfQuestions + "\n\nMap time: " +
                        mapTime  + "\n\nRun time: " + runTime + "\n\nRest time: " + restTime;
                break;
            case CHEMISTRY_INDEX:
                name = "Chemistry";
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
                text = "Complete a task sheet in Chemistry.\n\n" +
                        "Skill: " + skill + "\n\nEnergy: " + attn +
                        "\n\nTasks: " + numberOfQuestions + "\n\nTime per action: " +
                        timePerAction;
                break;
            case ICT_INDEX:
                name = "ICT";
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
                text = "Complete a task sheet in ICT.\n\n" +
                        "Skill: " + skill + "\n\nEnergy: " + attn +
                        "\n\nTasks: " + numberOfQuestions + "\n\nTime per action: " +
                        timePerAction;
                break;
            default:
                name = "null";
                text = "null";
                break;
        }
        subject.setText(name);
        gradeImageView.setImageBitmap(gradeImage);
        description.setText(text);
        params.width = xpPercent == 0 ?
                1 : (int)(xpBar.getDrawable().getIntrinsicWidth() * xpPercent);
        xpBar.setLayoutParams(params);
    }

    private void setUpFriendInfo(int id) {
        String nameText;
        String descriptionText;
        String rankText;
        int friendPoint = GAME.getFriendScore(id);
        int friendIndex = friendPoint == 0 ? 0 : (friendPoint > 19 ? 3 : (friendPoint / 10) + 1);
        float xpPercent = friendPoint > 19 ? 1 : ((float)(friendPoint % 10)) / 10;

        Bitmap friendImage = Game.getFriendImage(id);
        GameTextView name = findViewById(R.id.status_menu_friends_info_title);
        ImageView friendImageView = findViewById(R.id.status_menu_friends_info_picture);
        GameTextView description = findViewById(R.id.status_menu_friends_info_description);
        GameTextView rank = findViewById(R.id.status_menu_friends_info_rank);
        ImageView xpBar = findViewById(R.id.status_menu_friends_info_xp_bar);
        ViewGroup.LayoutParams params = xpBar.getLayoutParams();

        switch (id) {
            case ATHLETE_INDEX:
                nameText = "Sam";
                descriptionText = "Captain of the school athletics team. Though not academically " +
                        "inclined, Sam is incredibly gifted at sports." +
                        "\n\nLikes: Exercise, hard-workers";
                break;
            case CLASSMATE_INDEX:
                nameText = "Amy";
                descriptionText = "Most popular girl in school. Amy is friendly to students and " +
                        "teachers alike and has a major sweet tooth." +
                        "\n\nLikes: Sweets";
                break;
            case NERD_INDEX:
                nameText = "Alex";
                descriptionText = "Typical geek. Socially awkward, Alex is often found alone in " +
                        "the Chemistry lab after school." +
                        "\n\nLikes: Experiments, fellow scientists";
                break;
            case DELINQUENT_INDEX:
                nameText = "Nathan";
                descriptionText = "Rebel without a cause. Often found near DT, Nathan seems to " +
                        "have an unlimited supply of energy drinks." +
                        "\n\nLikes: Money, crafts";
                break;
            case TUTEE_INDEX:
                nameText = "Gemma";
                descriptionText = "Nice but book-dumb. Though she studies every day, Gemma still " +
                        "struggles in most subjects." +
                        "\n\nDislikes: Exams";
                break;
            default:
                nameText = "null";
                descriptionText = "null";
                break;
        }

        if (GAME.getGfIndex() == id) { rankText = "Girlfriend"; }
        else {
            switch (friendIndex) {
                case 0:
                    rankText = "Don't Know";
                    break;
                case 1:
                    rankText = "Acquaintance";
                    break;
                case 2:
                    rankText = "Friend";
                    break;
                case 3:
                    rankText = "Good Friend";
                    break;
                default:
                    rankText = "Don't Know";
                    break;
            }
        }
        name.setText(nameText);
        rank.setText(rankText);
        friendImageView.setImageBitmap(friendImage);
        description.setText(descriptionText);
        params.width = xpPercent == 0 ?
                1 : (int)(xpBar.getDrawable().getIntrinsicWidth() * xpPercent);
        xpBar.setLayoutParams(params);
    }

    private void setUpItemInfo(final Item item, int id) {
        final ConstraintLayout gameLayout = findViewById(R.id.game_layout);

        final ConstraintLayout inventoryMenu = findViewById(R.id.inventory_menu);
        final ConstraintLayout inventoryInfo = findViewById(R.id.inventory_menu_info);

        GameTextView inventoryTitle = findViewById(R.id.inventory_menu_info_title);
        ImageView inventoryIcon = findViewById(R.id.inventory_menu_info_icon);
        final GameTextView inventoryQuantity = findViewById(R.id.inventory_menu_info_quantity);
        final ConstraintLayout inventoryButton = findViewById(R.id.inventory_menu_info_button);
        GameTextView buttonText = findViewById(R.id.inventory_menu_info_button_text);
        GameTextView inventoryDescription = findViewById(R.id.inventory_menu_info_description);

        final ConstraintLayout buyMenu = findViewById(R.id.buy_menu);

        ConstraintLayout priceInfo = findViewById(R.id.inventory_menu_info_prices);
        final GameTextView money = findViewById(R.id.inventory_menu_prices_money);
        final GameTextView cost = findViewById(R.id.inventory_menu_prices_cost);
        GameTextView operator = findViewById(R.id.inventory_menu_info_prices_operator);

        inventoryTitle.setText(item.getName());
        inventoryIcon.setImageBitmap(item.getIcon());

        int n = 0;
        if (GAME.hasItem(item)) { n = GAME.getItemQuantity(item); }
        inventoryQuantity.setText("x" + String.format(Locale.ENGLISH, "%d", n));

        switch (id) {
            case INVENTORY_USE:
                buttonText.setText("Eat");

                final Runnable effect = item.getEffect();
                if (effect == null) { inventoryButton.setVisibility(View.GONE); }
                else {
                    inventoryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GAME.playSFX(SFX_CLICK);
                            gameLayout.setAlpha(1f);
                            inventoryInfo.setVisibility(View.GONE);
                            inventoryMenu.setVisibility(View.GONE);
                            effect.run();
                        }
                    });
                    inventoryButton.setVisibility(View.VISIBLE);
                }
                break;

            case INVENTORY_GIVE:
                buttonText.setText("Give");

                inventoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GAME.playSFX(SFX_CLICK);
                        gameLayout.setAlpha(1f);
                        inventoryInfo.setVisibility(View.GONE);
                        inventoryMenu.setVisibility(View.GONE);
                        inventoryRunnable.getReceiver().give(item);
                    }
                });
                inventoryButton.setVisibility(View.VISIBLE);
                break;

            case INVENTORY_BUY:
                priceInfo.setVisibility(View.VISIBLE);

                money.setText(String.format(Locale.ENGLISH, "%.2f",
                        ((float) GAME.getMoney()) / 100));
                cost.setText(String.format(Locale.ENGLISH, "%.2f",
                        ((float) item.getBuyPrice()) / 100));
                operator.setText("-");

                buttonText.setText(String.format("Buy for £%s",
                        String.format(Locale.ENGLISH, "%.2f",
                                ((float) item.getBuyPrice()) / 100)));

                if (GAME.getMoney() < item.getBuyPrice() ||
                        (GAME.hasItem(item) && GAME.isBookItem(item))) {
                    inventoryButton.setAlpha(0.5f);
                    inventoryButton.setEnabled(false);
                }

                inventoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GAME.playSFX(SFX_CLICK);
                        GAME.playSFX(SFX_MONEY);
                        GAME.getPlayer().setResponseIndex(1);
                        GAME.addItem(item);
                        addMoney(-item.getBuyPrice(), false);
                        money.setText(String.format(Locale.ENGLISH, "%.2f",
                                ((float) GAME.getMoney()) / 100));
                        inventoryQuantity.setText("x" + String.format(Locale.ENGLISH, "%d",
                                GAME.getItemQuantity(item)));
                        if (GAME.getMoney() < item.getBuyPrice()) {
                            inventoryButton.setAlpha(0.5f);
                            inventoryButton.setEnabled(false);
                        }
                        refreshBuyMenu();
                        refreshHUD();
                    }
                });
                inventoryButton.setVisibility(View.VISIBLE);
                break;
            case INVENTORY_SELL:
                priceInfo.setVisibility(View.VISIBLE);

                money.setText(String.format(Locale.ENGLISH, "%.2f",
                        ((float) GAME.getMoney()) / 100));
                cost.setText(String.format(Locale.ENGLISH, "%.2f",
                        ((float) item.getSellPrice()) / 100));
                operator.setText("+");

                buttonText.setText("Sell for £" +
                        (String.format(Locale.ENGLISH, "%.2f",
                                ((float) item.getSellPrice()) / 100)));

                if (GAME.getItemQuantity(item) == 0) {
                    inventoryButton.setAlpha(0.5f);
                    inventoryButton.setEnabled(false);
                }

                inventoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GAME.playSFX(SFX_CLICK);
                        GAME.playSFX(SFX_MONEY);
                        GAME.getPlayer().setResponseIndex(1);
                        GAME.removeItem(item);
                        addMoney(item.getSellPrice(), false);
                        money.setText(String.format(Locale.ENGLISH, "%.2f",
                                ((float) GAME.getMoney()) / 100));
                        inventoryQuantity.setText("x" + String.format(Locale.ENGLISH, "%d",
                                GAME.getItemQuantity(item)));
                        if (GAME.getItemQuantity(item) == 0) {
                            inventoryButton.setAlpha(0.5f);
                            inventoryButton.setEnabled(false);
                        }
                        refreshSellMenu();
                        refreshHUD();
                    }
                });
                inventoryButton.setVisibility(View.VISIBLE);
                break;

            case INVENTORY_EQUIP:
                buttonText.setText("Equip");

                inventoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GAME.playSFX(SFX_CLICK);
                        gameLayout.setAlpha(1f);
                        findViewById(R.id.lesson_a).setAlpha(1f);
                        findViewById(R.id.lesson_a_textbox).setAlpha(1f);
                        findViewById(R.id.lesson_b).setAlpha(1f);
                        findViewById(R.id.lesson_b_textbox).setAlpha(1f);
                        inventoryInfo.setVisibility(View.GONE);
                        inventoryMenu.setVisibility(View.GONE);
                        MiniGame miniGame = GAME.getMiniGame();
                        if (miniGame != null) {
                            if (miniGame instanceof LessonA) {
                                ((LessonA) miniGame).setDrinkIndex(item.getId());
                                ((LessonA) miniGame).setButtons();
                                ((LessonA) miniGame).refreshHUD();
                            } else if (miniGame instanceof  LessonB) {
                                findViewById(R.id.lesson_b_craft_bar_slider).
                                        setVisibility(View.VISIBLE);
                                ((LessonB) miniGame).setDrinkIndex(item.getId());
                                ((LessonB) miniGame).setButtons();
                                ((LessonB) miniGame).refreshHUD();
                            } else if (miniGame instanceof LessonC) {
                                ((LessonC) miniGame).setDrinkIndex(item.getId());
                                ((LessonC) miniGame).setButtons();
                                ((LessonC) miniGame).refreshHUD();
                            }
                        }
                    }
                });
                inventoryButton.setVisibility(View.VISIBLE);
                break;

            case INVENTORY_CRAFT:
                buttonText.setText("Make");

                inventoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.getId() == KEY5_INDEX) {
                            GAME.removeItem(Item.getItem(KEY1_INDEX));
                        }
                        GAME.playSFX(SFX_CLICK);
                        gameLayout.setAlpha(1f);
                        inventoryInfo.setVisibility(View.GONE);
                        buyMenu.setVisibility(View.GONE);
                        GAME.addItem(item);
                        GAME.playJingle(R.raw._jingle_get_item);
                        displayTextBox(new TextBoxStructure(
                                "> You made a " + item.getName() + "!",
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        GAME.goHome();
                                    }
                                }, true, null));
                    }
                });
                inventoryButton.setVisibility(View.VISIBLE);
                break;
        }

        inventoryDescription.setText(item.getDescription());
    }

    private void setMapTextAnimationView(int id) {
        if (currentAnim != null) {
            mapTextAnimator1.cancel();
            mapTextAnimator2.cancel();
            currentAnim.clearAnimation();
            currentAnim.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteFont));
        }
        switch (id) {
            case MAP_SCHOOL_HALL_G_ID:
                currentAnim = findViewById(R.id.map_menu_ground_hall_text);
                break;
            case MAP_SCHOOL_HALL_1F_ID:
                currentAnim = findViewById(R.id.map_menu_1floor_hall_text);
                break;
            case MAP_SCHOOL_CLASSROOM_DT_ID:
                currentAnim = findViewById(R.id.map_menu_ground_dt_text);
                break;
            case MAP_SCHOOL_CLASSROOM_FT_ID:
                currentAnim = findViewById(R.id.map_menu_ground_ft_text);
                break;
            case MAP_SCHOOL_CLASSROOM_1F_ID:
                currentAnim = findViewById(R.id.map_menu_1floor_classroom_text);
                break;
            case MAP_SCHOOL_CANTEEN_ID:
                currentAnim = findViewById(R.id.map_menu_ground_canteen_text);
                break;
            case MAP_SCHOOL_YARD_ID:
                currentAnim = findViewById(R.id.map_menu_ground_yard_text);
                break;
            case MAP_BEDROOM_ID:
                break;
            case MAP_SCHOOL_STAFFROOM_ID:
                currentAnim = findViewById(R.id.map_menu_1floor_staffroom_text);
                break;
            case MAP_SCHOOL_SHOP_ID:
                currentAnim = findViewById(R.id.map_menu_ground_shop_text);
                break;
        }
        setUpMapTextAnimation();
    }
    /*--------------------------------------------------------------------------------------------*/

    private void setUpMapTextAnimation() {
        mapTextAnimator1 = ObjectAnimator.ofInt(currentAnim, "textColor",
                ContextCompat.getColor(this, R.color.colorWhiteFont),
                ContextCompat.getColor(this, R.color.colorRedFont));
        mapTextAnimator1.setEvaluator(new ArgbEvaluator());
        mapTextAnimator1.setDuration(0);
        mapTextAnimator1.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapTextAnimator2.start();
                    }
                }, 500);
            }
        });

        mapTextAnimator2 = ObjectAnimator.ofInt(currentAnim, "textColor",
                ContextCompat.getColor(this, R.color.colorRedFont),
                ContextCompat.getColor(this, R.color.colorWhiteFont));
        mapTextAnimator2.setEvaluator(new ArgbEvaluator());
        mapTextAnimator2.setDuration(0);
        mapTextAnimator2.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapTextAnimator1.start();
                    }
                }, 500);
            }
        });
        mapTextAnimator1.start();
    }

    private void setUpTextBoxArrowAnimation() {
        ImageView textBoxArrow = findViewById(R.id.textbox_box_arrow);
        TranslateAnimation textBoxArrowAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.01f);
        textBoxArrowAnimation.setDuration(500);
        textBoxArrowAnimation.setRepeatCount(-1);
        textBoxArrowAnimation.setRepeatMode(Animation.RESTART);
        textBoxArrowAnimation.setInterpolator(new LinearInterpolator());
        textBoxArrowAnimation.setFillAfter(true);

        textBoxArrow.setAnimation(textBoxArrowAnimation);
    }

    private void setUpButtonsAndRunnables() {

        final ConstraintLayout gameLayout = findViewById(R.id.game_layout);

        final ImageView statusButton = findViewById(R.id.status_button);
        final ImageView mapButton = findViewById(R.id.map_button);
        final ImageView inventoryButton = findViewById(R.id.inventory_button);
        final ImageView cancelButton = findViewById(R.id.cancel_button);
        final ImageView runButton = findViewById(R.id.run_button);
        final ImageView heistButton = findViewById(R.id.heist_button);

        final ConstraintLayout statusMenu = findViewById(R.id.status_menu);
        final ConstraintLayout statusMenuPlayer = findViewById(R.id.status_menu_player);
        final ConstraintLayout statusMenuFriends = findViewById(R.id.status_menu_friends);
        final ConstraintLayout statusMenuGrades = findViewById(R.id.status_menu_grades);
        final ConstraintLayout statusMenuHeist = findViewById(R.id.status_menu_heist);
        final ImageView playerTab = findViewById(R.id.status_menu_player_tab);
        final ImageView friendsTab = findViewById(R.id.status_menu_friends_tab);
        final ImageView gradesTab = findViewById(R.id.status_menu_grades_tab);
        final ImageView heistTab = findViewById(R.id.status_menu_heist_tab);

        final ImageView athleteButton = findViewById(R.id.status_menu_friends_athlete_button);
        final GameTextView athleteTitle = findViewById(R.id.status_menu_friends_athlete_title);
        final GameTextView athleteSubtitle = findViewById(
                R.id.status_menu_friends_athlete_subtitle);

        final ImageView classmateButton = findViewById(R.id.status_menu_friends_classmate_button);
        final GameTextView classmateTitle = findViewById(R.id.status_menu_friends_classmate_title);
        final GameTextView classmateSubtitle = findViewById(
                R.id.status_menu_friends_classmate_subtitle);

        final ImageView nerdButton = findViewById(R.id.status_menu_friends_nerd_button);
        final GameTextView nerdTitle = findViewById(R.id.status_menu_friends_nerd_title);
        final GameTextView nerdSubtitle = findViewById(R.id.status_menu_friends_nerd_subtitle);

        final ImageView delinquentButton = findViewById(R.id.status_menu_friends_delinquent_button);
        final GameTextView delinquentTitle = findViewById(R.id.status_menu_friends_delinquent_title);
        final GameTextView delinquentSubtitle = findViewById(
                R.id.status_menu_friends_delinquent_subtitle);

        final ImageView tuteeButton = findViewById(R.id.status_menu_friends_tutee_button);
        final GameTextView tuteeTitle = findViewById(R.id.status_menu_friends_tutee_title);
        final GameTextView tuteeSubtitle = findViewById(R.id.status_menu_friends_tutee_subtitle);

        final ConstraintLayout friendInfo = findViewById(R.id.status_menu_friends_info);

        final ImageView dtButton = findViewById(R.id.status_menu_grades_dt_button);
        final GameTextView dtTitle = findViewById(R.id.status_menu_grades_dt_title);
        final GameTextView dtSubtitle = findViewById(R.id.status_menu_grades_dt_subtitle);

        final ImageView ftButton = findViewById(R.id.status_menu_grades_ft_button);
        final GameTextView ftTitle = findViewById(R.id.status_menu_grades_ft_title);
        final GameTextView ftSubtitle = findViewById(R.id.status_menu_grades_ft_subtitle);

        final ImageView peButton = findViewById(R.id.status_menu_grades_pe_button);
        final GameTextView peTitle = findViewById(R.id.status_menu_grades_pe_title);
        final GameTextView peSubtitle = findViewById(R.id.status_menu_grades_pe_subtitle);

        final ImageView chemistryButton = findViewById(R.id.status_menu_grades_chemistry_button);
        final GameTextView chemistryTitle = findViewById(R.id.status_menu_grades_chemistry_title);
        final GameTextView chemistrySubtitle = findViewById(
                R.id.status_menu_grades_chemistry_subtitle);

        final ImageView ictButton = findViewById(R.id.status_menu_grades_ict_button);
        final GameTextView ictTitle = findViewById(R.id.status_menu_grades_ict_title);
        final GameTextView ictSubtitle = findViewById(R.id.status_menu_grades_ict_subtitle);

        final ConstraintLayout gradeInfo = findViewById(R.id.status_menu_grades_info);

        final ConstraintLayout mapMenu = findViewById(R.id.map_menu);
        final ConstraintLayout mapMenuGround = findViewById(R.id.map_menu_ground);
        final ConstraintLayout mapMenuFirst = findViewById(R.id.map_menu_1floor);

        final ImageView groundTab = findViewById(R.id.map_menu_ground_tab);
        final ImageView firstFloorTab = findViewById(R.id.map_menu_1floor_tab);

        final ConstraintLayout textBox = findViewById(R.id.textbox);
        final GameTextView textBoxText = findViewById(R.id.textbox_box_text);
        final FlexboxLayout textBoxButtons = findViewById(R.id.textbox_box_buttons);

        final ConstraintLayout inventoryMenu = findViewById(R.id.inventory_menu);
        final ConstraintLayout inventoryInfo = findViewById(R.id.inventory_menu_info);
        final ConstraintLayout priceInfo = findViewById(R.id.inventory_menu_info_prices);
        final ConstraintLayout inventoryInfoButton = findViewById(R.id.inventory_menu_info_button);

        final ImageView skipButton = findViewById(R.id.skip_button);

        final ConstraintLayout studyMenu = findViewById(R.id.study_menu);

        final ImageView dtStudyButton = findViewById(R.id.study_menu_dt_button);
        final ImageView ftStudyButton = findViewById(R.id.study_menu_ft_button);
        final ImageView peStudyButton = findViewById(R.id.study_menu_pe_button);
        final ImageView chemistryStudyButton = findViewById(R.id.study_menu_chemistry_button);
        final ImageView ictStudyButton = findViewById(R.id.study_menu_ict_button);

        final ConstraintLayout lessonA = findViewById(R.id.lesson_a);
        final ConstraintLayout lessonATextBox = findViewById(R.id.lesson_a_textbox);

        final ConstraintLayout lessonB = findViewById(R.id.lesson_b);
        final ConstraintLayout lessonBTextBox = findViewById(R.id.lesson_b_textbox);
        final ImageView lessonBCraftBarSlider = findViewById(R.id.lesson_b_craft_bar_slider);

        final ConstraintLayout lessonCMapMenu = findViewById(R.id.lesson_c_map_menu);

        final ConstraintLayout shopMenu = findViewById(R.id.shop_menu);
        final ConstraintLayout shopBuyMenu = findViewById(R.id.shop_menu_buy);
        final ConstraintLayout shopSellMenu = findViewById(R.id.shop_menu_sell);
        final ImageView shopBuyTab = findViewById(R.id.shop_menu_buy_tab);
        final ImageView shopSellTab = findViewById(R.id.shop_menu_sell_tab);

        final ImageView quitButton = findViewById(R.id.quit_button);

        setUpTextBoxArrowAnimation();

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                setUpStatusInfo();
                setUpFriendMenu();
                setUpHeistMenu();
                gameLayout.setAlpha(0.5f);
                statusMenu.setVisibility(View.VISIBLE); }
        });

        statusMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                statusMenuPlayer.bringToFront();
                gameLayout.setAlpha(1f);
                statusMenu.setVisibility(View.GONE);
            }
        });

        playerTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                statusMenuPlayer.bringToFront();
            }
        });

        friendsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                statusMenuFriends.bringToFront();
            }
        });

        friendInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                friendInfo.setVisibility(View.GONE);
            }
        });

        View.OnClickListener athleteMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpFriendInfo(ATHLETE_INDEX);
                friendInfo.setVisibility(View.VISIBLE);
            }
        };
        athleteButton.setOnClickListener(athleteMenu);
        athleteTitle.setOnClickListener(athleteMenu);
        athleteSubtitle.setOnClickListener(athleteMenu);

        View.OnClickListener classmateMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpFriendInfo(CLASSMATE_INDEX);
                friendInfo.setVisibility(View.VISIBLE);
            }
        };
        classmateButton.setOnClickListener(classmateMenu);
        classmateTitle.setOnClickListener(classmateMenu);
        classmateSubtitle.setOnClickListener(classmateMenu);

        View.OnClickListener nerdMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpFriendInfo(NERD_INDEX);
                friendInfo.setVisibility(View.VISIBLE);
            }
        };
        nerdButton.setOnClickListener(nerdMenu);
        nerdTitle.setOnClickListener(nerdMenu);
        nerdSubtitle.setOnClickListener(nerdMenu);

        View.OnClickListener delinquentMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpFriendInfo(DELINQUENT_INDEX);
                friendInfo.setVisibility(View.VISIBLE);
            }
        };
        delinquentButton.setOnClickListener(delinquentMenu);
        delinquentTitle.setOnClickListener(delinquentMenu);
        delinquentSubtitle.setOnClickListener(delinquentMenu);

        View.OnClickListener tuteeMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpFriendInfo(TUTEE_INDEX);
                friendInfo.setVisibility(View.VISIBLE);
            }
        };
        tuteeButton.setOnClickListener(tuteeMenu);
        tuteeTitle.setOnClickListener(tuteeMenu);
        tuteeSubtitle.setOnClickListener(tuteeMenu);

        gradesTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                statusMenuGrades.bringToFront();
            }
        });

        gradeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                gradeInfo.setVisibility(View.GONE);
            }
        });

        View.OnClickListener dtMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpGradeInfo(DT_INDEX);
                gradeInfo.setVisibility(View.VISIBLE);
            }
        };
        dtButton.setOnClickListener(dtMenu);
        dtTitle.setOnClickListener(dtMenu);
        dtSubtitle.setOnClickListener(dtMenu);

        View.OnClickListener ftMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpGradeInfo(FT_INDEX);
                gradeInfo.setVisibility(View.VISIBLE);
            }
        };
        ftButton.setOnClickListener(ftMenu);
        ftTitle.setOnClickListener(ftMenu);
        ftSubtitle.setOnClickListener(ftMenu);

        View.OnClickListener peMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpGradeInfo(PE_INDEX);
                gradeInfo.setVisibility(View.VISIBLE);
            }
        };
        peButton.setOnClickListener(peMenu);
        peTitle.setOnClickListener(peMenu);
        peSubtitle.setOnClickListener(peMenu);

        View.OnClickListener chemistryMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpGradeInfo(CHEMISTRY_INDEX);
                gradeInfo.setVisibility(View.VISIBLE);
            }
        };
        chemistryButton.setOnClickListener(chemistryMenu);
        chemistryTitle.setOnClickListener(chemistryMenu);
        chemistrySubtitle.setOnClickListener(chemistryMenu);

        View.OnClickListener ictMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                setUpGradeInfo(ICT_INDEX);
                gradeInfo.setVisibility(View.VISIBLE);
            }
        };
        ictButton.setOnClickListener(ictMenu);
        ictTitle.setOnClickListener(ictMenu);
        ictSubtitle.setOnClickListener(ictMenu);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                switch (GAME.getTileMap().getMiniMapId()) {
                    case 0:
                        mapMenuGround.bringToFront();
                        break;
                    case 1:
                        mapMenuFirst.bringToFront();
                        break;
                }
                setMapTextAnimationView(GAME.getTileMap().getId());
                gameLayout.setAlpha(0.5f);
                mapMenu.setVisibility(View.VISIBLE);
            }
        });

        mapMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                mapMenuGround.bringToFront();
                gameLayout.setAlpha(1f);
                mapMenu.setVisibility(View.GONE);
            }
        });

        groundTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                mapMenuGround.bringToFront();
            }
        });

        firstFloorTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                mapMenuFirst.bringToFront();
            }
        });

        defaultTextBoxRunnable = new Runnable() {
            @Override
            public void run() {
                if (textBoxText.canScrollVertically(1)) {
                    GAME.playSFX(SFX_CLICK);
                    textBoxText.scrollBy(0, textBoxText.getLineHeight() * 6);
                } else if (textBoxButtons.getVisibility() == View.GONE) {
                    GAME.playSFX(SFX_CLICK);
                    textBox.setVisibility(View.GONE);
                    GAME.getPlayer().resetMoving();
                }
            }
        };

        textBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultTextBoxRunnable.run();
            }
        });

        inventoryRunnable.setRunnable(new Runnable() {
            @Override
            public void run() {
                GAME.playSFX(SFX_CLICK);
                setUpItemInfo(inventoryRunnable.getItem(), inventoryRunnable.getType());
                inventoryInfo.setVisibility(View.VISIBLE);

            }
        });

        buyRunnable.setRunnable(new Runnable() {
            @Override
            public void run() {
                GAME.playSFX(SFX_CLICK);
                setUpItemInfo(buyRunnable.getItem(), INVENTORY_BUY);
                inventoryInfo.setVisibility(View.VISIBLE);
            }
        });

        sellRunnable.setRunnable(new Runnable() {
            @Override
            public void run() {
                GAME.playSFX(SFX_CLICK);
                setUpItemInfo(sellRunnable.getItem(), INVENTORY_SELL);
                inventoryInfo.setVisibility(View.VISIBLE);
            }
        });

        craftRunnable.setRunnable(new Runnable() {
            @Override
            public void run() {
                GAME.playSFX(SFX_CLICK);
                setUpItemInfo(craftRunnable.getItem(), INVENTORY_CRAFT);
                inventoryInfo.setVisibility(View.VISIBLE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                GAME.getPlayer().cancelMovement();
            }
        });

        inventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                displayInventory(INVENTORY_USE);
            }
        });

        inventoryMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                gameLayout.setAlpha(1f);
                lessonA.setAlpha(1f);
                lessonATextBox.setAlpha(1f);
                lessonB.setAlpha(1f);
                lessonBTextBox.setAlpha(1f);
                lessonBCraftBarSlider.setVisibility(View.VISIBLE);
                inventoryMenu.setVisibility(View.GONE);
            }
        });

        inventoryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                inventoryInfo.setVisibility(View.GONE);
                inventoryInfoButton.setAlpha(1f);
                inventoryInfoButton.setEnabled(true);
                priceInfo.setVisibility(View.GONE);

            }
        });

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                runningShoes(GAME.getPlayer().getSpeed() == 2);
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                GAME.getScript().setSkip(true);
            }
        });

        studyMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                gameLayout.setAlpha(1f);
                studyMenu.setVisibility(View.GONE);
            }
        });

        dtStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                studyMenu.setVisibility(View.GONE);
                gameLayout.setAlpha(1f);
                GAME.study(DT_INDEX);
            }
        });
        ftStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                studyMenu.setVisibility(View.GONE);
                gameLayout.setAlpha(1f);
                GAME.study(FT_INDEX);
            }
        });
        peStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                studyMenu.setVisibility(View.GONE);
                gameLayout.setAlpha(1f);
                GAME.study(PE_INDEX);
            }
        });
        chemistryStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                studyMenu.setVisibility(View.GONE);
                gameLayout.setAlpha(1f);
                GAME.study(CHEMISTRY_INDEX);
            }
        });
        ictStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                studyMenu.setVisibility(View.GONE);
                gameLayout.setAlpha(1f);
                GAME.study(ICT_INDEX);
            }
        });

        heistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                setUpStatusInfo();
                setUpFriendMenu();
                setUpHeistMenu();
                gameLayout.setAlpha(0.5f);
                statusMenu.setVisibility(View.VISIBLE);
                statusMenuHeist.setVisibility(View.VISIBLE);
                statusMenuHeist.bringToFront();
            }
        });

        heistTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GAME.playSFX(SFX_CLICK);
                statusMenuHeist.bringToFront();
            }
        });

        lessonCMapMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                gameLayout.setAlpha(1f);
                lessonCMapMenu.setVisibility(View.GONE);
                MiniGame miniGame = GAME.getMiniGame();
                if (miniGame != null) {
                    if (miniGame instanceof LessonC) {
                        if (((LessonC) miniGame).getTime() <= 0) { miniGame.endLesson(); }
                    }
                }
            }
        });

        shopMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                shopBuyMenu.bringToFront();
                gameLayout.setAlpha(1f);
                shopMenu.setVisibility(View.GONE);
            }
        });

        shopBuyTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                shopBuyMenu.bringToFront();
            }
        });

        shopSellTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                shopSellMenu.bringToFront();
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                gameLayout.setAlpha(0.5f);
                displayTextBox(new TextBoxStructure("> Do you want to quit the game?",
                        "Yes", "No",
                        new Runnable() {
                            @Override
                            public void run() {
                                BGMFader.stop(GAME.getBGM(), 100);
                                GAME.save();
                                if (TitleActivity.getInstance() == null) {
                                    startActivity(new Intent(instance, TitleActivity.class));
                                }
                                else { TitleActivity.getInstance().reset(); }
                                gameLayout.setAlpha(1f);
                                finish();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                gameLayout.setAlpha(1f);
                            }
                        }, null));
            }
        });
    }

    public void disableButtons() {
        ImageView statusButton = findViewById(R.id.status_button);
        ImageView mapButton = findViewById(R.id.map_button);
        ImageView inventoryButton = findViewById(R.id.inventory_button);
        ImageView runButton = findViewById(R.id.run_button);
        ImageView heistButton = findViewById(R.id.heist_button);
        ImageView quitButton = findViewById(R.id.quit_button);

        statusButton.setAlpha(0.5f);
        statusButton.setEnabled(false);
        mapButton.setAlpha(0.5f);
        mapButton.setEnabled(false);
        inventoryButton.setAlpha(0.5f);
        inventoryButton.setEnabled(false);
        runButton.setAlpha(0.5f);
        runButton.setEnabled(false);
        heistButton.setAlpha(0.5f);
        heistButton.setEnabled(false);
        quitButton.setAlpha(0.5f);
        quitButton.setEnabled(false);
    }

    public void enableButtons() {
        ImageView statusButton = findViewById(R.id.status_button);
        ImageView mapButton = findViewById(R.id.map_button);
        ImageView inventoryButton = findViewById(R.id.inventory_button);
        ImageView runButton = findViewById(R.id.run_button);
        ImageView heistButton = findViewById(R.id.heist_button);
        ImageView quitButton = findViewById(R.id.quit_button);

        statusButton.setAlpha(1f);
        statusButton.setEnabled(true);
        mapButton.setAlpha(1f);
        mapButton.setEnabled(true);
        inventoryButton.setAlpha(1f);
        inventoryButton.setEnabled(true);
        runButton.setAlpha(1f);
        runButton.setEnabled(true);
        heistButton.setAlpha(1f);
        heistButton.setEnabled(true);
        quitButton.setAlpha(1f);
        quitButton.setEnabled(true);
    }

    public void disableLessonCButtons() {
        MiniGame miniGame = GAME.getMiniGame();
        if (miniGame != null) {
            if (miniGame instanceof LessonC) {
                ((LessonC) miniGame).setButtons();
            }
        }

        ImageView lessonCRunButton = findViewById(R.id.lesson_c_run_button);
        ImageView lessonCWaitButton = findViewById(R.id.lesson_c_wait_button);
        ImageView lessonCDrinkButton = findViewById(R.id.lesson_c_drink_button);
        ImageView lessonCBookButton = findViewById(R.id.lesson_c_book_button);
        ImageView lessonCMapButton = findViewById(R.id.lesson_c_map_button);
        ImageView lessonCHelpButton = findViewById(R.id.lesson_c_help_button);
        ImageView lessonCInventoryButton = findViewById(R.id.lesson_c_inventory_button);

        ImageView lessonCDrinkIcon = findViewById(R.id.lesson_c_drink_icon);
        GameTextView lessonCDrinkText = findViewById(R.id.lesson_c_drink_quantity_text);
        GameTextView lessonCRunText = findViewById(R.id.lesson_c_run_text);
        GameTextView lessonCWaitText = findViewById(R.id.lesson_c_wait_text);
        ImageView lessonCBookIcon = findViewById(R.id.lesson_c_book_icon);
        GameTextView lessonCBookText = findViewById(R.id.lesson_c_book_quantity_text);
        GameTextView lessonCMapText = findViewById(R.id.lesson_c_map_text);


        lessonCRunButton.setAlpha(0.5f);
        lessonCRunButton.setEnabled(false);
        lessonCWaitButton.setAlpha(0.5f);
        lessonCWaitButton.setEnabled(false);
        lessonCDrinkButton.setAlpha(0.5f);
        lessonCDrinkButton.setEnabled(false);
        lessonCBookButton.setAlpha(0.5f);
        lessonCBookButton.setEnabled(false);
        lessonCMapButton.setAlpha(0.5f);
        lessonCMapButton.setEnabled(false);
        lessonCHelpButton.setAlpha(0.5f);
        lessonCHelpButton.setEnabled(false);
        lessonCInventoryButton.setAlpha(0.5f);
        lessonCInventoryButton.setEnabled(false);

        lessonCDrinkIcon.setAlpha(0.5f);
        lessonCDrinkText.setAlpha(0.5f);
        lessonCRunText.setAlpha(0.5f);
        lessonCWaitText.setAlpha(0.5f);
        lessonCBookIcon.setAlpha(0.5f);
        lessonCBookText.setAlpha(0.5f);
        lessonCMapText.setAlpha(0.5f);
    }

    public void enableLessonCButtons() {
        ImageView lessonCRunButton = findViewById(R.id.lesson_c_run_button);
        ImageView lessonCWaitButton = findViewById(R.id.lesson_c_wait_button);
        ImageView lessonCDrinkButton = findViewById(R.id.lesson_c_drink_button);
        ImageView lessonCBookButton = findViewById(R.id.lesson_c_book_button);
        ImageView lessonCMapButton = findViewById(R.id.lesson_c_map_button);
        ImageView lessonCHelpButton = findViewById(R.id.lesson_c_help_button);
        ImageView lessonCInventoryButton = findViewById(R.id.lesson_c_inventory_button);

        ImageView lessonCDrinkIcon = findViewById(R.id.lesson_c_drink_icon);
        GameTextView lessonCDrinkText = findViewById(R.id.lesson_c_drink_quantity_text);
        GameTextView lessonCRunText = findViewById(R.id.lesson_c_run_text);
        GameTextView lessonCWaitText = findViewById(R.id.lesson_c_wait_text);
        ImageView lessonCBookIcon = findViewById(R.id.lesson_c_book_icon);
        GameTextView lessonCBookText = findViewById(R.id.lesson_c_book_quantity_text);
        GameTextView lessonCMapText = findViewById(R.id.lesson_c_map_text);

        lessonCRunButton.setAlpha(1f);
        lessonCRunButton.setEnabled(true);
        lessonCWaitButton.setAlpha(1f);
        lessonCWaitButton.setEnabled(true);
        lessonCDrinkButton.setAlpha(1f);
        lessonCDrinkButton.setEnabled(true);
        lessonCBookButton.setAlpha(1f);
        lessonCBookButton.setEnabled(true);
        lessonCMapButton.setAlpha(1f);
        lessonCMapButton.setEnabled(true);
        lessonCHelpButton.setAlpha(1f);
        lessonCHelpButton.setEnabled(true);
        lessonCInventoryButton.setAlpha(1f);
        lessonCInventoryButton.setEnabled(true);

        lessonCDrinkIcon.setAlpha(1f);
        lessonCDrinkText.setAlpha(1f);
        lessonCRunText.setAlpha(1f);
        lessonCWaitText.setAlpha(1);
        lessonCBookIcon.setAlpha(1f);
        lessonCBookText.setAlpha(1f);
        lessonCMapText.setAlpha(1f);

        MiniGame miniGame = GAME.getMiniGame();
        if (miniGame != null) {
            if (miniGame instanceof LessonC) {
                ((LessonC) miniGame).setButtons();
            }
        }
    }

    public void disableSkipButton() {
        ImageView skipButton = findViewById(R.id.skip_button);

        skipButton.setAlpha(0.5f);
        skipButton.setEnabled(false);
    }

    public void enableSkipButton() {
        ImageView skipButton = findViewById(R.id.skip_button);

        skipButton.setAlpha(1f);
        skipButton.setEnabled(true);
    }

    public void disableCancelButton() {
        ImageView cancelButton = findViewById(R.id.cancel_button);
        ImageView lessonCancelButton = findViewById(R.id.lesson_c_cancel_button);

        cancelButton.setAlpha(0.5f);
        cancelButton.setEnabled(false);

        lessonCancelButton.setAlpha(0.5f);
        lessonCancelButton.setEnabled(false);
    }

    public void enableCancelButton() {
        ImageView cancelButton = findViewById(R.id.cancel_button);
        ImageView lessonCancelButton = findViewById(R.id.lesson_c_cancel_button);

        cancelButton.setAlpha(1f);
        cancelButton.setEnabled(true);

        lessonCancelButton.setAlpha(1f);
        lessonCancelButton.setEnabled(true);
    }

    public void enableLoadingScreen() {
        ImageView loadingScreen = findViewById(R.id.loading_screen);
        loadingScreen.setVisibility(View.VISIBLE);
    }

    public void disableLoadingScreen() {
        final ImageView loadingScreen = findViewById(R.id.loading_screen);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(400);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                GAME.setLoading(false);
                if (GAME.hasPointChange()) {
                    for (int[] pointChange : GAME.getPointChanges()) {
                        addPoints(pointChange[0], pointChange[1], pointChange[2]);
                    }
                    GAME.clearPointChange();
                }
                if (GAME.hasLoadingEndRunnable()) {
                    GAME.runLoadingScreenEndRunnable();
                    GAME.setLoadingScreenEndRunnable(null);
                }
                if (GAME.isLunchMoney()) {
                    GameActivity.getInstance().addMoney(250, true);
                    GAME.resetLunchMoney();
                }
                if (!GAME.getBGM().isPlaying()) {
                    BGMFader.start(GAME.getBGM(), 100);
                }
                if (GAME.getMiniGame() == null && GAME.getScript() == null) { GAME.save(); }
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        loadingScreen.setAnimation(fadeOut);
        loadingScreen.getAnimation().start();
        loadingScreen.setVisibility(View.GONE);
    }

    public void setSlideLoadingTransition(String before, final String after) {
        final GameTextView loadingText = findViewById(R.id.loading_text);
        loadingText.setText(before);
        TranslateAnimation slideOut = new TranslateAnimation(0,
                SCREEN_WIDTH, 0, 0);
        slideOut.setDuration(1000);
        slideOut.setStartOffset(500);

        final TranslateAnimation slideIn = new TranslateAnimation(
                -SCREEN_WIDTH, 0, 0, 0);
        slideIn.setDuration(1000);

        final AlphaAnimation delay = new AlphaAnimation(1f, 1f);
        delay.setDuration(1000);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                loadingText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadingText.setText(after);
                loadingText.setAnimation(slideIn);
                GAME.setLoadingScreen(true);
                loadingText.getAnimation().start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadingText.setAnimation(delay);
                GAME.setLoadingScreen(true);
                loadingText.getAnimation().start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        delay.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadingText.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        loadingText.setAnimation(slideOut);
    }

    public void setCalendarLoadingTransition(int dayBefore, int dayAfter) {
        final ConstraintLayout loadingDayOld = findViewById(R.id.loading_day_old);
        final ConstraintLayout loadingDayNew = findViewById(R.id.loading_day_new);

        GameTextView loadingDayOldText = findViewById(R.id.loading_day_old_content);
        GameTextView loadingDayNewText = findViewById(R.id.loading_day_content);

        if (dayBefore <= 3) {
            GameTextView calendarSubtitle = findViewById(R.id.loading_day_old_subtitle);
            loadingDayOldText.setTextColor(getResources().getColor(R.color.colorRedFont));
            calendarSubtitle.setTextColor(getResources().getColor(R.color.colorRedFont));
        }

        if (dayAfter <= 3) {
            GameTextView calendarSubtitle = findViewById(R.id.loading_day_subtitle);
            loadingDayNewText.setTextColor(getResources().getColor(R.color.colorRedFont));
            calendarSubtitle.setTextColor(getResources().getColor(R.color.colorRedFont));
            if (dayAfter == 1) { calendarSubtitle.setText("DAY"); }
        }

        loadingDayOldText.setText("" + dayBefore);
        loadingDayNewText.setText("" + dayAfter);

        if (dayAfter == 1) {
            GameTextView calendarSubtitle = findViewById(R.id.loading_day_subtitle);
            calendarSubtitle.setText("DAY");
        }

        RotateAnimation rotateAnimation = new RotateAnimation(0, -120, 0, 0);
        rotateAnimation.setDuration(1000);

        TranslateAnimation slideOut = new TranslateAnimation(0, -SCREEN_WIDTH, 0, -SCREEN_HEIGHT);
        slideOut.setDuration(2000);

        AnimationSet calendarAnimation = new AnimationSet(false);
        calendarAnimation.addAnimation(rotateAnimation);
        calendarAnimation.addAnimation(slideOut);

        calendarAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                loadingDayOld.setVisibility(View.VISIBLE);
                loadingDayNew.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadingDayOld.setVisibility(View.GONE);
                loadingDayNew.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        calendarAnimation.setDuration(2000);
        calendarAnimation.setStartOffset(500);

        loadingDayOld.setAnimation(calendarAnimation);
    }

    public void displayTextBox(TextBoxStructure textBoxStructure) {
        disableButtons();
        disableCancelButton();

        final ConstraintLayout textBox = findViewById(R.id.textbox);
        GameTextView textBoxText = findViewById(R.id.textbox_box_text);
        final FlexboxLayout textBoxButtons = findViewById(R.id.textbox_box_buttons);
        ConstraintLayout textBoxButtonsYes = findViewById(R.id.textbox_box_buttons_yes);
        ConstraintLayout textBoxButtonsNo = findViewById(R.id.textbox_box_buttons_no);
        GameTextView textBoxButtonsYesLabel = findViewById(R.id.textbox_box_buttons_yes_text);
        GameTextView textBoxButtonsNoLabel = findViewById(R.id.textbox_box_buttons_no_text);
        ImageView textBoxArrow = findViewById(R.id.textbox_box_arrow);

        ConstraintLayout npcLabel = findViewById(R.id.textbox_label);
        GameTextView npcLabelText = findViewById(R.id.textbox_label_text);

        ConstraintLayout textBoxImage = findViewById(R.id.textbox_image);
        ImageView textBoxImageContent = findViewById(R.id.textbox_image_content);

        textBoxText.scrollTo(0, 0);
        textBoxText.setText(textBoxStructure.getText());
        textBox.setVisibility(View.VISIBLE);
        textBox.getParent().requestTransparentRegion(findViewById(R.id.game_surface));
        textBoxArrow.setVisibility(View.VISIBLE);
        setUpTextBoxArrowAnimation();

        final Runnable runnable1 = textBoxStructure.getRunnable1();
        final Runnable runnable2 = textBoxStructure.getRunnable2();
        final NPC npc = textBoxStructure.getNpc();

        if (npc == null) {
            npcLabel.setVisibility(View.GONE);
            textBoxImage.setVisibility(View.GONE);
        }
        else {
            if (!GAME.spokenTo(npc)) { GAME.addSpokenNPC(npc); }
            npc.setWillWait(false);
            npcLabelText.setText(npc.getName());
            npcLabel.setVisibility(View.VISIBLE);
            if (npc.hasTextBoxImg()) {
                textBoxImageContent.setImageBitmap(npc.getTextBoxImg());
                textBoxImage.setVisibility(View.VISIBLE);
            } else {
                textBoxImage.setVisibility(View.GONE);
            }
        }

        if (runnable1 != null) {
            if (textBoxStructure.isNoButton()) {
                textBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        defaultTextBoxRunnable.run();
                        runnable1.run();
                        if (!(runnable1 instanceof TextBoxRunnable)) {
                            textBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    defaultTextBoxRunnable.run();
                                }
                            });
                        }
                    }
                });
            }
            else {
                String buttonText1 = textBoxStructure.getButtonText1();
                String buttonText2 = textBoxStructure.getButtonText2();

                if (buttonText1 != null) { textBoxButtonsYesLabel.setText(buttonText1); }
                else { textBoxButtonsYesLabel.setText("Yes"); }
                if (buttonText2 != null) { textBoxButtonsNoLabel.setText(buttonText2); }
                else { textBoxButtonsNoLabel.setText("No"); }

                textBoxButtons.setVisibility(View.VISIBLE);
                textBoxArrow.clearAnimation();
                textBoxArrow.setVisibility(View.INVISIBLE);

                textBoxButtonsYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GAME.playSFX(SFX_CLICK);
                        textBox.setVisibility(View.GONE);
                        textBoxButtons.setVisibility(View.GONE);
                        runnable1.run();
                    }
                });
                if (runnable2 != null) {
                    textBoxButtonsNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            GAME.playSFX(SFX_CLICK);
                            textBox.setVisibility(View.GONE);
                            textBoxButtons.setVisibility(View.GONE);
                            runnable2.run();
                        }
                    });
                }
                else {
                    textBoxButtonsNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            GAME.playSFX(SFX_CLICK);
                            textBox.setVisibility(View.GONE);
                            textBoxButtons.setVisibility(View.GONE);
                            GAME.getPlayer().resetMoving();
                        }
                    });
                }
            }
        }
    }

    public void displayInventory(final int type, NPC... npcs) {
        final ConstraintLayout gameLayout = findViewById(R.id.game_layout);
        final ConstraintLayout lessonA = findViewById(R.id.lesson_a);
        final ConstraintLayout lessonATextbox = findViewById(R.id.lesson_a_textbox);
        final ConstraintLayout lessonB = findViewById(R.id.lesson_b);
        final ConstraintLayout lessonBTextbox = findViewById(R.id.lesson_b_textbox);
        final ImageView lessonBCraftBarSlider = findViewById(R.id.lesson_b_craft_bar_slider);

        final ConstraintLayout inventoryMenu = findViewById(R.id.inventory_menu);

        gameLayout.setAlpha(0.5f);
        lessonA.setAlpha(0.5f);
        lessonATextbox.setAlpha(0.5f);
        lessonB.setAlpha(0.5f);
        lessonBTextbox.setAlpha(0.5f);
        lessonBCraftBarSlider.setVisibility(View.GONE);

        int keyIndex = 2;
        int normalIndex = 12;

        ArrayList<Item> items = new ArrayList<>(GAME.getItemSet());

        for (int i = 0; i < inventoryMenu.getChildCount(); i ++) {
            View view = inventoryMenu.getChildAt(i);
            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(Item.getEmptyIcon());
                view.setOnClickListener(null);
                view.setAlpha(1f);
                view.setEnabled(true);
            }

        }

        for (int j = 0; j < GAME.getInventorySize(); j++) {
            final Item item = items.get(j);
            View view;
            if (item.isKeyItem()) {
                view = inventoryMenu.getChildAt(keyIndex);
                keyIndex++;
            }
            else {
                view = inventoryMenu.getChildAt(normalIndex);
                normalIndex++;
            }

            if (npcs.length > 0) { inventoryRunnable.setReceiver(npcs[0]); }
            else { inventoryRunnable.setReceiver(null);}

            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(item.getMenuIcon());
                if ((type == INVENTORY_GIVE && !npcs[0].canGive(item)) ||
                        (type == INVENTORY_EQUIP && item.getId() != DRINK0_INDEX
                                && item.getId() != DRINK1_INDEX && item.getId() != DRINK2_INDEX)) {
                    view.setAlpha(0.5f);
                    view.setEnabled(false);
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inventoryRunnable.setType(type);
                        inventoryRunnable.setItem(item);
                        new ItemRunnable(item, inventoryRunnable).run();
                    }
                });
            }
        }
        inventoryMenu.setVisibility(View.VISIBLE);
    }

    public void displayBuyMenu(ArrayList<Item> items, NPC npc) {
        GAME.getPlayer().setResponseIndex(2);
        buyRunnable.setReceiver(npc);

        if (items.size() <= 0) { return; }

        final ConstraintLayout gameLayout = findViewById(R.id.game_layout);

        final ConstraintLayout buyMenu = findViewById(R.id.buy_menu);
        final GameTextView buyMenuTitle = findViewById(R.id.buy_menu_title);
        buyMenuTitle.setText("What will you buy?");

        buyMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                buyMenu.setVisibility(View.GONE);
                gameLayout.setAlpha(1f);
                displayTextBox(buyRunnable.getReceiver().getText());
                GAME.getPlayer().setResponseIndex(0);
            }
        });

        gameLayout.setAlpha(0.5f);

        int index = 2;

        for (int i = 0; i < buyMenu.getChildCount(); i ++) {
            View view = buyMenu.getChildAt(i);
            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(Item.getEmptyIcon());
                view.setOnClickListener(null);
                view.setAlpha(1f);
                view.setEnabled(true);
            }

        }

        for (int j = 0; j < items.size(); j++) {
            final Item item = items.get(j);
            View view;
            view = buyMenu.getChildAt(index);
            index++;

            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(item.getMenuIcon());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buyRunnable.setItem(item);
                        new ItemRunnable(item, buyRunnable).run();
                    }
                });
            }
        }
        buyMenu.setVisibility(View.VISIBLE);
    }

    public void displayShopMenu(ArrayList<Item> items, ArrayList<Item> inventory, NPC npc) {
        GAME.getPlayer().setResponseIndex(2);
        buyRunnable.setReceiver(npc);

        if (items.size() <= 0) { return; }

        final ConstraintLayout gameLayout = findViewById(R.id.game_layout);

        final ConstraintLayout shopMenu = findViewById(R.id.shop_menu);
        final ConstraintLayout shopBuyMenu = findViewById(R.id.shop_menu_buy);
        final ConstraintLayout shopSellMenu = findViewById(R.id.shop_menu_sell);

        shopMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                shopMenu.setVisibility(View.GONE);
                shopBuyMenu.bringToFront();
                gameLayout.setAlpha(1f);
                displayTextBox(buyRunnable.getReceiver().getText());
                GAME.getPlayer().setResponseIndex(0);
            }
        });

        gameLayout.setAlpha(0.5f);

        int index = 0;

        for (int i = 0; i < shopBuyMenu.getChildCount(); i ++) {
            View view = shopBuyMenu.getChildAt(i);
            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(Item.getEmptyIcon());
                view.setOnClickListener(null);
                view.setAlpha(1f);
                view.setEnabled(true);
            }

        }

        for (int j = 0; j < items.size(); j++) {
            while (!(shopBuyMenu.getChildAt(index) instanceof ItemImageView)) { index++; }
            final Item item = items.get(j);
            View view;
            view = shopBuyMenu.getChildAt(index);
            index++;

            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(item.getMenuIcon());
                ((ItemImageView) view).setItem(item);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buyRunnable.setItem(item);
                        new ItemRunnable(item, buyRunnable).run();
                    }
                });
                if (GAME.isSheetItem(item) && GAME.hasBook(item)) {
                    view.setEnabled(false);
                    view.setAlpha(0.5f);
                } else {
                    view.setEnabled(true);
                    view.setAlpha(1f);
                }
            }
        }

        index = 0;

        for (int i = 0; i < shopSellMenu.getChildCount(); i ++) {
            View view = shopSellMenu.getChildAt(i);
            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(Item.getEmptyIcon());
                view.setOnClickListener(null);
                view.setAlpha(1f);
                view.setEnabled(true);
            }

        }

        for (int j = 0; j < inventory.size(); j++) {
            while (!(shopSellMenu.getChildAt(index) instanceof ItemImageView)) { index++; }
            final Item item = inventory.get(j);
            if (item.isKeyItem()) { continue; }
            View view;
            view = shopSellMenu.getChildAt(index);
            index++;

            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(item.getMenuIcon());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sellRunnable.setItem(item);
                        new ItemRunnable(item, sellRunnable).run();
                    }
                });
            }
        }
        shopMenu.setVisibility(View.VISIBLE);
    }

    public void refreshBuyMenu() {
        final ConstraintLayout shopBuyMenu = findViewById(R.id.shop_menu_buy);

        for (int i = 0; i < shopBuyMenu.getChildCount(); i ++) {
            View view = shopBuyMenu.getChildAt(i);
            if (view instanceof ItemImageView) {
                if ((GAME.isSheetItem(((ItemImageView) view).getItem())
                        && GAME.hasBook(((ItemImageView) view).getItem()))
                        ||
                        (GAME.isBookItem(((ItemImageView) view).getItem())
                                && GAME.hasItem(((ItemImageView) view).getItem()))) {
                    view.setEnabled(false);
                    view.setAlpha(0.5f);
                } else {
                    view.setEnabled(true);
                    view.setAlpha(1f);
                }
            }

        }
    }

    public void refreshSellMenu() {
        ArrayList<Item> inventory = new ArrayList<>(GAME.getItemSet());
        final ConstraintLayout shopSellMenu = findViewById(R.id.shop_menu_sell);

        for (int i = 0; i < shopSellMenu.getChildCount(); i ++) {
            View view = shopSellMenu.getChildAt(i);
            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(Item.getEmptyIcon());
                view.setOnClickListener(null);
                view.setAlpha(1f);
                view.setEnabled(true);
            }

        }

        int index = 0;

        for (int j = 0; j < inventory.size(); j++) {
            while (!(shopSellMenu.getChildAt(index) instanceof ItemImageView)) { index++; }
            final Item item = inventory.get(j);
            if (item.isKeyItem()) { continue; }
            View view;
            view = shopSellMenu.getChildAt(index);
            index++;

            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(item.getMenuIcon());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sellRunnable.setItem(item);
                        new ItemRunnable(item, sellRunnable).run();
                    }
                });
                if (GAME.isSheetItem(item) && GAME.hasBook(item)) {
                    view.setEnabled(false);
                    view.setAlpha(0.5f);
                } else {
                    view.setEnabled(true);
                    view.setAlpha(1f);
                }
            }
        }
    }

    public void displayCraftMenu(ArrayList<Item> items) {

        if (items.size() == 0) {
            String type;
            if (GAME.getTileMap().getId() == MAP_SCHOOL_CLASSROOM_FT_ID) { type = "snacks"; }
            else { type = "crafts"; }
            displayTextBox(new TextBoxStructure("> You don't know how to make any " + type + "!"));
            return;
        }

        final ConstraintLayout gameLayout = findViewById(R.id.game_layout);

        final ConstraintLayout buyMenu = findViewById(R.id.buy_menu);
        final GameTextView buyMenuTitle = findViewById(R.id.buy_menu_title);
        buyMenuTitle.setText("What will you make?");

        buyMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_CLICK);
                buyMenu.setVisibility(View.GONE);
                gameLayout.setAlpha(1f);
                GAME.getPlayer().setResponseIndex(0);
            }
        });

        gameLayout.setAlpha(0.5f);

        int index = 2;

        for (int i = 0; i < buyMenu.getChildCount(); i ++) {
            View view = buyMenu.getChildAt(i);
            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(Item.getEmptyIcon());
                view.setOnClickListener(null);
                view.setAlpha(1f);
                view.setEnabled(true);
            }

        }

        for (int j = 0; j < items.size(); j++) {
            final Item item = items.get(j);
            View view;
            view = buyMenu.getChildAt(index);
            index++;

            if (view instanceof ItemImageView) {
                ((ItemImageView) view).setImageBitmap(item.getMenuIcon());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        craftRunnable.setItem(item);
                        new ItemRunnable(item, craftRunnable).run();
                    }
                });
            }
        }
        buyMenu.setVisibility(View.VISIBLE);
    }

    public void displayEndScreen() {

        ConstraintLayout endScreen = findViewById(R.id.end_screen);

        ((GameTextView) findViewById(R.id.end_screen_player_grades_points)).setText(
                String.format(Locale.ENGLISH, "%dpts", GAME.getGradePoints()));

        ((GameTextView) findViewById(R.id.end_screen_player_friend_points)).setText(
                String.format(Locale.ENGLISH, "%dpts", GAME.getFriendPoints()));

        ((GameTextView) findViewById(R.id.end_screen_player_heist_points)).setText(
                String.format(Locale.ENGLISH, "%dpts", GAME.getHeistPoints()));

        ((GameTextView) findViewById(R.id.end_screen_player_total_points)).setText(
                String.format(Locale.ENGLISH, "%dpts", GAME.getPoints()));

        endScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.setNGPlus();
                GAME.getProgressDataStructure().NGPlus();
                BGMFader.stop(GAME.getBGM(), 100);
                GAME.save();
                TitleActivity.getInstance().reset();
                findViewById(R.id.game_layout).setAlpha(1f);
                finish();
            }
        });
        endScreen.setVisibility(View.VISIBLE);
    }

    public void refreshHUD() {
        GameTextView pointsText = findViewById(R.id.top_menu_points_text);
        GameTextView timeText = findViewById(R.id.top_menu_time_text);
        GameTextView daysText = findViewById(R.id.top_menu_day_content);
        GameTextView daysSubtitle = findViewById(R.id.top_menu_day_subtitle);
        GameTextView moneyText = findViewById(R.id.top_menu_money_text);

        pointsText.setText(String.format("%spts", String.format(
                Locale.ENGLISH, "%d", GAME.getPoints())));
        moneyText.setText(String.format("£%s",
                String.format(Locale.ENGLISH, "%.2f", ((float) GAME.getMoney()) / 100)));

        switch (GAME.getTime()) {
            case TIME_MORNING:
                timeText.setText("MORNING");
                break;
            case TIME_LUNCH:
                timeText.setText("LUNCH");
                break;
            case TIME_AFTER_SCHOOL:
                timeText.setText("AFTER SCHOOL");
                break;
            case TIME_EVENING:
                timeText.setText("EVENING");
                break;
            case TIME_HEIST_PHASE_1:
            case TIME_HEIST_PHASE_2:
                timeText.setText("HEIST");
                break;
        }

        if (NUMBER_OF_DAYS - GAME.getDay() >= 0) {
            daysText.setText(String.format(
                    Locale.ENGLISH, "%d", NUMBER_OF_DAYS - GAME.getDay()));
            if (NUMBER_OF_DAYS - GAME.getDay() == 1) { daysSubtitle.setText("DAY"); }
            else { daysSubtitle.setText("DAYS"); }
        }
        if (GAME.getDay() > 27) {
            daysText.setTextColor(getResources().getColor(R.color.colorRedFont));
            daysSubtitle.setTextColor(getResources().getColor(R.color.colorRedFont));
        }
    }

    public void scriptHUD(boolean script) {
        ImageView statusButton = findViewById(R.id.status_button);
        ImageView mapButton = findViewById(R.id.map_button);
        ImageView inventoryButton = findViewById(R.id.inventory_button);
        ImageView runButton = findViewById(R.id.run_button);
        ImageView skipButton = findViewById(R.id.skip_button);
        ImageView cancelButton = findViewById(R.id.cancel_button);
        ImageView heistButton = findViewById(R.id.heist_button);
        ImageView quitButton = findViewById(R.id.quit_button);

        if (script) {
            skipButton.setVisibility(View.VISIBLE);
            statusButton.setVisibility(View.GONE);
            mapButton.setVisibility(View.GONE);
            inventoryButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            heistButton.setVisibility(View.GONE);
            quitButton.setVisibility(View.GONE);
            if (GAME.hasItem(Item.getItem(KEY2_INDEX))) { runButton.setVisibility(View.GONE); }
        } else {
            statusButton.setVisibility(View.VISIBLE);
            mapButton.setVisibility(View.VISIBLE);
            inventoryButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            quitButton.setVisibility(View.VISIBLE);
            if (GAME.getProgressDataStructure().hasHeistPlan()) {
                heistButton.setVisibility(View.VISIBLE);
            }
            skipButton.setVisibility(View.GONE);
            if (GAME.hasItem(Item.getItem(KEY2_INDEX))) { runButton.setVisibility(View.VISIBLE); }
        }
    }

    public void addMoney(int increase, boolean label) {
        final GameTextView moneyText = findViewById(R.id.top_menu_money_text);
        final GameTextView moneyLabel = findViewById(R.id.top_menu_money_label);

        int oldMoney = GAME.getMoney();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(oldMoney,
                GAME.increaseMoney(increase));
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                moneyText.setText(String.format("£%s",
                        (String.format(Locale.ENGLISH, "%.2f",
                                ((float) animation.getAnimatedValue()) / 100))));
            }
        });

        if (label) {
            moneyLabel.setVisibility(View.VISIBLE);

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(moneyLabel, "alpha",
                    0f, 1f);
            fadeIn.setDuration(0);
            final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(moneyLabel, "alpha",
                    0f, 1f);
            fadeOut.setDuration(0);

            fadeIn.addListener(new AnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(Animator animation) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fadeOut.start();
                        }
                    }, 3000);
                }
            });
            fadeOut.addListener(new AnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(Animator animation) {
                    moneyLabel.setVisibility(View.GONE);
                }
            });

            fadeIn.start();
        }

        GAME.playSFX(SFX_MONEY);

        valueAnimator.start();
    }

    public void addPoints(int id, int index, int increase) {
        final GameTextView pointsText = findViewById(R.id.top_menu_points_text);
        GameTextView friendLabel = findViewById(R.id.top_menu_friend_label);
        GameTextView gradeLabel = findViewById(R.id.top_menu_grade_label);
        GameTextView heistLabel = findViewById(R.id.top_menu_heist_label);

        final GameTextView label;

        switch (id) {
            case FRIEND_INCREASE:
                friendLabel.setText("FRIEND BONUS");
                label = friendLabel;
                break;
            case FRIEND_DECREASE:
                friendLabel.setText("FRIEND PENALTY");
                label = friendLabel;
                break;
            case GRADE_INCREASE:
                gradeLabel.setText("GRADE BONUS");
                label = gradeLabel;
                break;
            case HEIST_BONUS:
                label = heistLabel;
                break;
            case EXAM_INCREASE:
                gradeLabel.setText("EXAM BONUS");
                label = gradeLabel;
                break;
            default:
                return;
        }
        label.setVisibility(View.VISIBLE);

        int oldPoints = GAME.getPoints();

        ValueAnimator valueAnimator = ValueAnimator.ofInt(oldPoints,
                GAME.increasePoints(id, index, increase));
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pointsText.setText(String.format(Locale.ENGLISH,
                        "%d", (Integer) animation.getAnimatedValue()) + "pts");
            }
        });

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(label, "alpha", 0f, 1f);
        fadeIn.setDuration(0);

        final ObjectAnimator fadeOut =
                ObjectAnimator.ofFloat(label, "alpha", 0f, 1f);
        fadeOut.setDuration(0);

        fadeIn.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fadeOut.start();
                    }
                }, 3000);
            }
        });
        fadeOut.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                label.setVisibility(View.GONE);
            }
        });

        valueAnimator.start();
        fadeIn.start();
    }

    public void runningShoes(boolean equip) {
        ImageView runButton = findViewById(R.id.run_button);
        if (equip) {
            GAME.getPlayer().setSpeed(4);
            runButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable._ui_main_walk_button));
        } else {
            GAME.getPlayer().setSpeed(2);
            runButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable._ui_main_run_button));
        }
    }

    public void enableRunButton() {
        ImageView runButton = findViewById(R.id.run_button);
        if (GAME.hasItem(Item.getItem(KEY2_INDEX))) { runButton.setVisibility(View.VISIBLE); }
    }

    public void enableHeistUI() {
        ImageView heistButton = findViewById(R.id.heist_button);
        ConstraintLayout heistMenu = findViewById(R.id.status_menu_heist);

        heistButton.setVisibility(View.VISIBLE);
        heistMenu.setVisibility(View.VISIBLE);
    }

    public void displayLessonAUI() {
        final ConstraintLayout lessonA = findViewById(R.id.lesson_a);
        ConstraintLayout lessonATextbox = findViewById(R.id.lesson_a_textbox);
        final GameTextView lessonATextboxLabelText = findViewById(R.id.lesson_a_textbox_label_text);

        lessonA.setVisibility(View.VISIBLE);
        lessonATextbox.setVisibility(View.VISIBLE);
        lessonA.setAlpha(1f);
        lessonATextbox.setAlpha(1f);
        hideButtons();

        final LessonA gameLessonA = (LessonA) GAME.getMiniGame();

        final ImageView skillIcon = findViewById(R.id.lesson_a_skill_icon);
        skillIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonA.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonA.displayText("> Your current skill level. A higher skill gives a " +
                            "higher chance of successfully completing a task.");
                }
            }
        });
        ImageView skillBar = findViewById(R.id.lesson_a_skill_bar);
        skillBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skillIcon.performClick();
            }
        });

        final ImageView energyIcon = findViewById(R.id.lesson_a_attn_icon);
        energyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonA.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonA.displayText("> Your current energy level. Energy allows you to " +
                            "diligently complete tasks.");
                }
            }
        });
        ImageView energyBar = findViewById(R.id.lesson_a_attn_bar);
        energyBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                energyIcon.performClick();
            }
        });

        final ImageView timeIcon = findViewById(R.id.lesson_a_time_icon);
        timeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonA.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonA.displayText("> The time currently remaining in the lesson. " +
                            "Every action takes time, so be sure not to run out!");
                }
            }
        });
        ImageView timeBar = findViewById(R.id.lesson_a_time_bar);
        timeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIcon.performClick();
            }
        });

        ImageView answer0 = findViewById(R.id.lesson_a_answer0_button);
        answer0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonA.isHelp()) {
                    gameLessonA.displayText("> Attempt to complete the current task. If your " +
                            "skill is low you may fail.");
                } else {
                    gameLessonA.answer0();
                    gameLessonA.setButtons();
                    gameLessonA.refreshHUD();
                }
            }
        });

        ImageView answer1 = findViewById(R.id.lesson_a_answer1_button);
        answer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonA.isHelp()) {
                    gameLessonA.displayText("> Diligently attempt to complete the current task. " +
                            "Requires energy and takes more time, but you have a higher chance " +
                            "of success!");
                }
                else {
                    gameLessonA.answer1();
                    gameLessonA.setButtons();
                    gameLessonA.refreshHUD();
                }
            }
        });

        ImageView drink = findViewById(R.id.lesson_a_drink_button);
        drink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonA.isHelp()) {
                    gameLessonA.displayText("> Drink the currently equipped energy drink to " +
                            "increase your energy!");
                }
                else {
                    gameLessonA.drink();
                    gameLessonA.setButtons();
                    gameLessonA.refreshHUD();
                }
            }
        });

        ImageView reread = findViewById(R.id.lesson_a_reread_button);
        reread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonA.isHelp()) {
                    gameLessonA.displayText("> Reread the current task to temporarily increase " +
                            "your skill. This temporary increase only affects the current task.");
                }
                else {
                    gameLessonA.reread();
                    gameLessonA.setButtons();
                    gameLessonA.refreshHUD();
                }
            }
        });

        ImageView book = findViewById(R.id.lesson_a_book_button);
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonA.isHelp()) {
                    gameLessonA.displayText("> Use an item to increase your skill level for the " +
                            "remainder of the lesson.");
                } else {
                    gameLessonA.book();
                    gameLessonA.setButtons();
                    gameLessonA.refreshHUD();
                }
            }
        });

        ImageView inventory = findViewById(R.id.lesson_a_inventory_button);
        inventory.setAlpha(1f);
        inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonA.isHelp()) {
                    gameLessonA.displayText("> Open your inventory to change the currently " +
                            "equipped energy drink.");
                }
                else {
                    displayInventory(INVENTORY_EQUIP);
                }
            }
        });

        final ImageView help = findViewById(R.id.lesson_a_help_button);
        final ImageView back = findViewById(R.id.lesson_a_back_button);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                help.setVisibility(View.GONE);
                findViewById(R.id.game_surface).setAlpha(0.25f);
                gameLessonA.setHelp(true);
                gameLessonA.setButtons();
                lessonATextboxLabelText.setText("Help");
                gameLessonA.displayText("> Tap an icon to get an in-depth description.");
                back.setVisibility(View.VISIBLE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                back.setVisibility(View.GONE);
                findViewById(R.id.game_surface).setAlpha(1f);
                gameLessonA.setHelp(false);
                gameLessonA.setButtons();
                gameLessonA.refreshHUD();
                gameLessonA.displayText(gameLessonA.getQuestionText());
                help.setVisibility(View.VISIBLE);
            }
        });

    }

    public void displayLessonBUI() {
        final ConstraintLayout lessonB = findViewById(R.id.lesson_b);
        ConstraintLayout lessonBTextbox = findViewById(R.id.lesson_b_textbox);
        final GameTextView lessonBTextboxLabelText = findViewById(R.id.lesson_b_textbox_label_text);
        final ImageView craftBarSlider = findViewById(R.id.lesson_b_craft_bar_slider);

        lessonB.setVisibility(View.VISIBLE);
        lessonBTextbox.setVisibility(View.VISIBLE);
        craftBarSlider.setVisibility(View.VISIBLE);

        lessonB.setAlpha(1f);
        lessonBTextbox.setAlpha(1f);
        hideButtons();

        final LessonB gameLessonB = (LessonB) GAME.getMiniGame();

        final ImageView skillIcon = findViewById(R.id.lesson_b_skill_icon);
        skillIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonB.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonB.displayText("> Your current skill level. A higher skill " +
                            "increases the success area of the crafting bar.");
                }
            }
        });
        ImageView skillBar = findViewById(R.id.lesson_b_skill_bar);
        skillBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skillIcon.performClick();
            }
        });

        final ImageView energyIcon = findViewById(R.id.lesson_b_attn_icon);
        energyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonB.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonB.displayText("> Your current energy level. Energy slows the " +
                            "slider speed on the crafting bar.");
                }
            }
        });
        ImageView energyBar = findViewById(R.id.lesson_b_attn_bar);
        energyBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                energyIcon.performClick();
            }
        });

        final ImageView timeIcon = findViewById(R.id.lesson_b_time_icon);
        timeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonB.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonB.displayText("> The time currently remaining in the lesson. " +
                            "Every action takes time, so be sure not to run out!");
                }
            }
        });
        ImageView timeBar = findViewById(R.id.lesson_b_time_bar);
        timeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIcon.performClick();
            }
        });

        ImageView answer = findViewById(R.id.lesson_b_answer_button);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonB.isHelp()) {
                    gameLessonB.displayText("> Attempt to complete the current task. You will " +
                            "fail if the slider is in the red area of the crafting bar.");
                } else {
                    gameLessonB.answer();
                    gameLessonB.setButtons();
                    gameLessonB.refreshHUD();
                }
            }
        });

        ImageView drink = findViewById(R.id.lesson_b_drink_button);
        drink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonB.isHelp()) {
                    gameLessonB.displayText("> Drink the currently equipped energy drink to " +
                            "increase your energy!");
                }
                else {
                    gameLessonB.drink();
                    gameLessonB.setButtons();
                    gameLessonB.refreshHUD();
                }
            }
        });

        ImageView reread = findViewById(R.id.lesson_b_reread_button);
        reread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonB.isHelp()) {
                    gameLessonB.displayText("> Reread the current task to temporarily increase " +
                            "your skill. This temporary increase only affects the current task.");
                }
                else {
                    gameLessonB.reread();
                    gameLessonB.setButtons();
                    gameLessonB.refreshHUD();
                }
            }
        });

        ImageView book = findViewById(R.id.lesson_b_book_button);
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonB.isHelp()) {
                    gameLessonB.displayText("> Use an item to increase your skill level for the " +
                            "remainder of the lesson.");
                } else {
                    gameLessonB.book();
                    gameLessonB.setButtons();
                    gameLessonB.refreshHUD();
                }
            }
        });

        ImageView inventory = findViewById(R.id.lesson_b_inventory_button);
        inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonB.isHelp()) {
                    gameLessonB.displayText("> Open your inventory to change the currently " +
                            "equipped energy drink.");
                }
                else {
                    displayInventory(INVENTORY_EQUIP);
                }
            }
        });

        ImageView craftBar = findViewById(R.id.lesson_b_craft_bar);
        craftBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonB.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonB.displayText("> The crafting bar and slider. Stop the slider in " +
                            "the yellow or green areas to complete the current task..");
                }
            }
        });

        final ImageView help = findViewById(R.id.lesson_b_help_button);
        final ImageView back = findViewById(R.id.lesson_b_back_button);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                help.setVisibility(View.INVISIBLE);
                craftBarSlider.setVisibility(View.GONE);
                findViewById(R.id.game_surface).setAlpha(0.25f);
                gameLessonB.setHelp(true);
                gameLessonB.setButtons();
                lessonBTextboxLabelText.setText("Help");
                gameLessonB.displayText("> Tap an icon to get an in-depth description.");
                back.setVisibility(View.VISIBLE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                back.setVisibility(View.GONE);
                craftBarSlider.setVisibility(View.VISIBLE);
                findViewById(R.id.game_surface).setAlpha(1f);
                gameLessonB.setHelp(false);
                gameLessonB.setButtons();
                gameLessonB.refreshHUD();
                gameLessonB.displayText(gameLessonB.getQuestionText());
                help.setVisibility(View.VISIBLE);
            }
        });

    }

    public void displayLessonCUI() {
        final ConstraintLayout lessonC = findViewById(R.id.lesson_c);
        final ConstraintLayout lessonCTextbox = findViewById(R.id.lesson_c_textbox);

        lessonC.setVisibility(View.VISIBLE);
        lessonCTextbox.setVisibility(View.VISIBLE);
        lessonC.setAlpha(1f);
        lessonCTextbox.setAlpha(1f);
        hideButtons();

        final LessonC gameLessonC = (LessonC) GAME.getMiniGame();

        final ImageView skillIcon = findViewById(R.id.lesson_c_skill_icon);
        skillIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonC.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonC.displayText("> Your current skill level. A higher skill " +
                            "reduces the time taken to read the map.");
                }
            }
        });
        ImageView skillBar = findViewById(R.id.lesson_c_skill_bar);
        skillBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skillIcon.performClick();
            }
        });

        final ImageView energyIcon = findViewById(R.id.lesson_c_attn_icon);
        energyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonC.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonC.displayText("> Your current energy level. Energy allows you " +
                            "to run for a little while.");
                }
            }
        });
        ImageView energyBar = findViewById(R.id.lesson_c_attn_bar);
        energyBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                energyIcon.performClick();
            }
        });

        final ImageView timeIcon = findViewById(R.id.lesson_c_time_icon);
        timeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonC.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonC.displayText("> The time currently remaining in the lesson. " +
                            "Every action takes time, so be sure not to run out!");
                }
            }
        });
        ImageView timeBar = findViewById(R.id.lesson_c_time_bar);
        timeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIcon.performClick();
            }
        });

        ImageView run = findViewById(R.id.lesson_c_run_button);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonC.isHelp()) {
                    gameLessonC.displayText("> Run for a little while. Movement takes less time.");
                } else {
                    gameLessonC.running();
                    gameLessonC.setButtons();
                    gameLessonC.refreshHUD();
                }
            }
        });

        ImageView wait = findViewById(R.id.lesson_c_wait_button);
        wait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonC.isHelp()) {
                    gameLessonC.displayText("> Rest for a short time to regain some energy.");
                }
                else {
                    gameLessonC.rest();
                    gameLessonC.setButtons();
                    gameLessonC.refreshHUD();
                }
            }
        });

        ImageView drink = findViewById(R.id.lesson_c_drink_button);
        drink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonC.isHelp()) {
                    gameLessonC.displayText("> Drink the currently equipped energy drink to " +
                            "increase your energy!");
                }
                else {
                    gameLessonC.drink();
                    gameLessonC.setButtons();
                    gameLessonC.refreshHUD();
                }
            }
        });

        final ImageView map = findViewById(R.id.lesson_c_map_button);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonC.isHelp()) {
                    gameLessonC.displayText("> Check your map to find the control points. " +
                            "Opening your map takes time, so be careful.");
                }
                else {
                    gameLessonC.map();
                    setUpLessonCMapPointAnimation();
                    gameLessonC.setButtons();
                    gameLessonC.refreshHUD();
                }
            }
        });

        ImageView book = findViewById(R.id.lesson_c_book_button);
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonC.isHelp()) {
                    gameLessonC.displayText("> Use an item to increase your skill level for " +
                            "the remainder of the lesson.");
                } else {
                    gameLessonC.book();
                    gameLessonC.setButtons();
                    gameLessonC.refreshHUD();
                }
            }
        });

        ImageView inventory = findViewById(R.id.lesson_c_inventory_button);
        inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (gameLessonC.isHelp()) {
                    gameLessonC.displayText("> Open your inventory to change the currently " +
                            "equipped energy drink.");
                }
                else {
                    displayInventory(INVENTORY_EQUIP);
                }
            }
        });

        final ImageView help = findViewById(R.id.lesson_c_help_button);
        final ImageView back = findViewById(R.id.lesson_c_back_button);
        final ConstraintLayout textBox = findViewById(R.id.lesson_c_textbox_box);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                help.setVisibility(View.GONE);
                findViewById(R.id.game_surface).setAlpha(0.25f);
                gameLessonC.setHelp(true);
                gameLessonC.setButtons();
                textBox.setVisibility(View.VISIBLE);
                lessonCTextbox.setClickable(false);
                gameLessonC.displayText("> Tap an icon to get an in-depth description.");
                back.setVisibility(View.VISIBLE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                back.setVisibility(View.GONE);
                findViewById(R.id.game_surface).setAlpha(1f);
                gameLessonC.setHelp(false);
                textBox.setVisibility(View.GONE);
                lessonCTextbox.setClickable(false);
                gameLessonC.setButtons();
                gameLessonC.refreshHUD();
                help.setVisibility(View.VISIBLE);
            }
        });

        ImageView cancelButton = findViewById(R.id.lesson_c_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameLessonC.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    gameLessonC.displayText("> Cancel your current movement.");
                } else {
                    GAME.playSFX(SFX_CLICK);
                    GAME.getPlayer().cancelMovement();
                }
            }
        });

    }

    public void displayExamUI() {
        final ConstraintLayout lessonA = findViewById(R.id.lesson_a);
        ConstraintLayout lessonATextbox = findViewById(R.id.lesson_a_textbox);
        final GameTextView lessonATextboxLabelText = findViewById(R.id.lesson_a_textbox_label_text);

        lessonA.setVisibility(View.VISIBLE);
        lessonATextbox.setVisibility(View.VISIBLE);
        lessonA.setAlpha(1f);
        lessonATextbox.setAlpha(1f);
        hideButtons();

        final Exam exam = (Exam) GAME.getMiniGame();

        final ImageView skillIcon = findViewById(R.id.lesson_a_skill_icon);
        skillIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    exam.displayText("> Your current skill level. A higher skill gives a higher " +
                            "chance of answering a question to a good standard.");
                }
            }
        });
        ImageView skillBar = findViewById(R.id.lesson_a_skill_bar);
        skillBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skillIcon.performClick();
            }
        });

        final ImageView energyIcon = findViewById(R.id.lesson_a_attn_icon);
        energyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    exam.displayText("> Your current energy level. Energy allows you answer " +
                            "questions to a higher standard.");
                }
            }
        });
        ImageView energyBar = findViewById(R.id.lesson_a_attn_bar);
        energyBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                energyIcon.performClick();
            }
        });

        final ImageView timeIcon = findViewById(R.id.lesson_a_time_icon);
        timeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.isHelp()) {
                    GAME.playSFX(SFX_MOVE);
                    exam.displayText("> The time currently remaining in the exam. Every action " +
                            "takes time, so be sure not to run out!");
                }
            }
        });
        ImageView timeBar = findViewById(R.id.lesson_a_time_bar);
        timeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIcon.performClick();
            }
        });

        ImageView answer0 = findViewById(R.id.lesson_a_answer0_button);
        answer0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (exam.isHelp()) {
                    exam.displayText("> Attempt to answer the current question. If your skill " +
                            "is low you may fail.");
                } else {
                    exam.answer0();
                    exam.setButtons();
                    exam.refreshHUD();
                }
            }
        });

        ImageView answer1 = findViewById(R.id.lesson_a_answer1_button);
        answer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (exam.isHelp()) {
                    exam.displayText("> Diligently attempt to answer the current question. " +
                            "Requires energy and takes more time, but you have a higher chance " +
                            "of success!");
                }
                else {
                    exam.answer1();
                    exam.setButtons();
                    exam.refreshHUD();
                }
            }
        });

        ImageView drink = findViewById(R.id.lesson_a_drink_button);
        drink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (exam.isHelp()) {
                    exam.displayText("> You can't drink energy drinks in exams!");
                    GAME.playSFX(SFX_DEBUFF);
                } else {
                    exam.drink();
                    exam.setButtons();
                    exam.refreshHUD();
                }
            }
        });

        ImageView reread = findViewById(R.id.lesson_a_reread_button);
        reread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                if (exam.isHelp()) {
                    exam.displayText("> Reread the current question to temporarily increase your " +
                            "skill. This temporary increase only affects the current question.");
                }
                else {
                    exam.reread();
                    exam.setButtons();
                    exam.refreshHUD();
                }
            }
        });

        ImageView book = findViewById(R.id.lesson_a_book_button);
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.isHelp()) {
                    exam.displayText("> You can't read books in exams!");
                    GAME.playSFX(SFX_DEBUFF);
                }
                else {
                    exam.book();
                    exam.setButtons();
                    exam.refreshHUD();
                }
            }
        });

        ImageView inventory = findViewById(R.id.lesson_a_inventory_button);
        inventory.setAlpha(0.5f);
        inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.isHelp()) {
                    exam.displayText("> You can't open your bag in exams!");
                } else {
                    exam.displayFeedbackText(new TextBoxStructure("> You can't open your bag " +
                            "in exams!"));
                }
                GAME.playSFX(SFX_DEBUFF);
            }
        });

        final ImageView help = findViewById(R.id.lesson_a_help_button);
        final ImageView back = findViewById(R.id.lesson_a_back_button);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                help.setVisibility(View.GONE);
                findViewById(R.id.game_surface).setAlpha(0.25f);
                exam.setHelp(true);
                exam.setButtons();
                lessonATextboxLabelText.setText("Help");
                exam.displayText("> Tap an icon to get an in-depth description.");
                back.setVisibility(View.VISIBLE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GAME.playSFX(SFX_MOVE);
                back.setVisibility(View.GONE);
                findViewById(R.id.game_surface).setAlpha(1f);
                exam.setHelp(false);
                exam.setButtons();
                exam.refreshHUD();
                exam.displayText(exam.getQuestionText());
                help.setVisibility(View.VISIBLE);
            }
        });

    }

    public void setUpLessonCMapPointAnimation() {
        ImageView mapPoint = findViewById(R.id.lesson_c_map_menu_point);
        mapPointAnimator1 = ObjectAnimator.ofInt(mapPoint, "colorFilter",
                ContextCompat.getColor(this, R.color.colorWhiteFont),
                ContextCompat.getColor(this, R.color.colorRedFont));
        mapPointAnimator1.setEvaluator(new ArgbEvaluator());
        mapPointAnimator1.setDuration(0);
        mapPointAnimator1.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapPointAnimator2.start();
                    }
                }, 500);
            }
        });

        mapPointAnimator2 = ObjectAnimator.ofInt(mapPoint, "colorFilter",
                ContextCompat.getColor(this, R.color.colorRedFont),
                ContextCompat.getColor(this, R.color.colorWhiteFont));
        mapPointAnimator2.setEvaluator(new ArgbEvaluator());
        mapPointAnimator2.setDuration(0);
        mapPointAnimator2.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapPointAnimator1.start();
                    }
                }, 500);
            }
        });
        mapPointAnimator1.start();
    }

    public void hideButtons() {
        ConstraintLayout topMenu = findViewById(R.id.top_menu);

        ImageView statusButton = findViewById(R.id.status_button);
        ImageView mapButton = findViewById(R.id.map_button);
        ImageView inventoryButton = findViewById(R.id.inventory_button);
        ImageView runButton = findViewById(R.id.run_button);
        ImageView heistButton = findViewById(R.id.heist_button);
        ImageView cancelButton = findViewById(R.id.cancel_button);
        ImageView quitButton = findViewById(R.id.quit_button);

        topMenu.setVisibility(View.GONE);

        statusButton.setVisibility(View.GONE);
        mapButton.setVisibility(View.GONE);
        inventoryButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        runButton.setVisibility(View.GONE);
        heistButton.setVisibility(View.GONE);
        quitButton.setVisibility(View.GONE);
    }

    public void showButtons() {
        ConstraintLayout topMenu = findViewById(R.id.top_menu);

        ImageView statusButton = findViewById(R.id.status_button);
        ImageView mapButton = findViewById(R.id.map_button);
        ImageView inventoryButton = findViewById(R.id.inventory_button);
        ImageView runButton = findViewById(R.id.run_button);
        ImageView heistButton = findViewById(R.id.heist_button);
        ImageView cancelButton = findViewById(R.id.cancel_button);
        ImageView quitButton = findViewById(R.id.quit_button);

        topMenu.setVisibility(View.VISIBLE);

        statusButton.setVisibility(View.VISIBLE);
        mapButton.setVisibility(View.VISIBLE);
        inventoryButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        if (GAME.hasItem(Item.getItem(KEY2_INDEX))) { runButton.setVisibility(View.VISIBLE); }
        if (GAME.getProgressDataStructure().hasHeistPlan()) {
            heistButton.setVisibility(View.VISIBLE);
            setUpHeistMenu();
        }
        quitButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.status_menu_friends_info).getVisibility() == View.VISIBLE) {
            findViewById(R.id.status_menu_friends_info).callOnClick();
        } else if (findViewById(R.id.status_menu_grades_info).getVisibility() == View.VISIBLE) {
            findViewById(R.id.status_menu_grades_info).callOnClick();
        } else if (findViewById(R.id.inventory_menu_info).getVisibility() == View.VISIBLE) {
            findViewById(R.id.inventory_menu_info).callOnClick();
        } else if (findViewById(R.id.status_menu).getVisibility() == View.VISIBLE) {
            findViewById(R.id.status_menu).callOnClick();
        } else if (findViewById(R.id.map_menu).getVisibility() == View.VISIBLE) {
            findViewById(R.id.map_menu).callOnClick();
        } else if (findViewById(R.id.buy_menu).getVisibility() == View.VISIBLE) {
            findViewById(R.id.buy_menu).callOnClick();
        } else if (findViewById(R.id.study_menu).getVisibility() == View.VISIBLE) {
            findViewById(R.id.study_menu).callOnClick();
        } else if (findViewById(R.id.lesson_c_map_menu).getVisibility() == View.VISIBLE) {
            findViewById(R.id.lesson_c_map_menu).callOnClick();
        } else if (findViewById(R.id.shop_menu).getVisibility() == View.VISIBLE) {
            findViewById(R.id.shop_menu).callOnClick();
        } else if (findViewById(R.id.inventory_menu).getVisibility() == View.VISIBLE) {
            findViewById(R.id.inventory_menu).callOnClick();
        } else if (findViewById(R.id.textbox).getVisibility() == View.VISIBLE) {
            findViewById(R.id.textbox).callOnClick();
        } else if (GAME.getMiniGame() == null && findViewById(R.id.quit_button).isEnabled()) {
            findViewById(R.id.quit_button).callOnClick();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        GAME.getBGM().pause();
        GAME.getJingle().pause();
        if (GAME.getMiniGame() == null && GAME.getScript() == null&& GAME.getTime() != TIME_HEIST_PHASE_2 && !isGamePause()) {
            GAME.save();
        }
        if (TitleActivity.getInstance() != null) { TitleActivity.getInstance().reset(); }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!GAME.getBGM().isPlaying()) { GAME.getBGM().start(); }
    }
}
