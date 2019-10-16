package com.lmweav.schoolquest;

import android.content.res.Resources;

/*
 * School Quest: Constants
 * This class is where all engine and game constants are stored.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class Constants {

    /*---------------------------------------------------------------------------------------------
    | Engine Constants
    ----------------------------------------------------------------------------------------------*/

    private static final float LOGICAL_WIDTH = 320;
    private static final float LOGICAL_HEIGHT = 480;
    private static final float LOGICAL_RATIO = LOGICAL_WIDTH / LOGICAL_HEIGHT;

    public static final float SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static final float SCREEN_HEIGHT =
            Resources.getSystem().getDisplayMetrics().heightPixels;
    public static final float SCREEN_RATIO = SCREEN_WIDTH / SCREEN_HEIGHT;
    public static final float SCREEN_DENSITY = Resources.getSystem().getDisplayMetrics().density;

    private static final float LESS_SCALE_FACTOR = SCREEN_WIDTH / LOGICAL_WIDTH;
    private static final float GREATER_SCALE_FACTOR = SCREEN_HEIGHT / LOGICAL_HEIGHT;

    private static final float SCALE_FACTOR = SCREEN_RATIO < LOGICAL_RATIO ?
            LESS_SCALE_FACTOR : GREATER_SCALE_FACTOR;

    private static final int GAME_WIDTH = (int) (LOGICAL_WIDTH * SCALE_FACTOR);
    private static final int GAME_HEIGHT = (int) (LOGICAL_HEIGHT * SCALE_FACTOR);

    public static final int HORIZONTAL_OFFSET = SCREEN_RATIO < LOGICAL_RATIO ?
            0 : (int) ((SCREEN_WIDTH / 2) - (GAME_WIDTH / 2));
    public static final int VERTICAL_OFFSET = SCREEN_RATIO < LOGICAL_RATIO ?
            (int) ((SCREEN_HEIGHT / 2) - (GAME_HEIGHT / 2)) : 0;

    public static final int RAW_TILE_SIZE = 16;
    public static final int TILE_FACTOR = 8;
    public static final int SCALED_TILE_SIZE = (GAME_WIDTH / 11)  -
            ((GAME_WIDTH / 11) % TILE_FACTOR);

    public static final int CAMERA_HEIGHT = GAME_HEIGHT / SCALED_TILE_SIZE;
    public static final int CAMERA_WIDTH = GAME_WIDTH / SCALED_TILE_SIZE;

    public static final int X_PADDING = (HORIZONTAL_OFFSET / SCALED_TILE_SIZE) + 2;
    public static final int Y_PADDING = (VERTICAL_OFFSET / SCALED_TILE_SIZE) + 3;

    public static final int TARGET_FPS = 20;

    public static final int OBJECT_TILESET_DOWN_INDEX = 0;
    public static final int OBJECT_TILESET_LEFT_INDEX = 8;
    public static final int OBJECT_TILESET_UP_INDEX = 5;
    public static final int OBJECT_TILESET_RIGHT_INDEX = 3;

    public static final int EMOTION_SURPRISE_INDEX = 0;
    public static final int EMOTION_QUESTION_INDEX = 1;
    public static final int EMOTION_HAPPY_INDEX = 2;
    public static final int EMOTION_LOVE_INDEX = 3;
    public static final int EMOTION_SICK_INDEX = 4;
    public static final int EMOTION_SAD_INDEX = 5;
    public static final int EMOTION_SPEECH_INDEX = 6;
    public static final int EMOTION_SIGH_INDEX = 7;
    public static final int EMOTION_ANGER_INDEX = 8;
    public static final int EMOTION_DISTRESS_INDEX = 9;
    public static final int EMOTION_IDEA_INDEX = 10;
    public static final int EMOTION_THOUGHT_INDEX = 11;
    public static final int EMOTION_SELL_INDEX = 12;

    public static final int EMOTION_FRAMES = 40;

    public static final int OBJECT_DIRECTION_UP = 0;
    public static final int OBJECT_DIRECTION_DOWN = 1;
    public static final int OBJECT_DIRECTION_LEFT = 2;
    public static final int OBJECT_DIRECTION_RIGHT = 3;

    public static final int[] UP_ANIMATION_1 = new int[] { 5, 6, 6, 5 };
    public static final int[] UP_ANIMATION_2 = new int[] { 5, 7, 7, 5 };
    public static final int[] DOWN_ANIMATION_1 = new int[] { 0, 1, 1, 0 };
    public static final int[] DOWN_ANIMATION_2 = new int[] { 0, 2, 2, 0 };
    public static final int[] LEFT_ANIMATION = new int[] { 8, 9, 9, 8 };
    public static final int[] RIGHT_ANIMATION = new int[] { 3, 4, 4, 3 };

    public static final String TEXTBOX_AUTO_RUN = "AUTORUN";

    /*---------------------------------------------------------------------------------------------
    | Game Constants
    ----------------------------------------------------------------------------------------------*/

    public static final int NO_DATA = -1;

    public static final int MAP_SCHOOL_HALL_G_ID = 0;
    public static final int MAP_SCHOOL_HALL_1F_ID = 1;
    public static final int MAP_SCHOOL_CLASSROOM_DT_ID = 2;
    public static final int MAP_SCHOOL_CLASSROOM_FT_ID = 3;
    public static final int MAP_SCHOOL_CLASSROOM_1F_ID = 4;
    public static final int MAP_SCHOOL_CANTEEN_ID = 5;
    public static final int MAP_SCHOOL_YARD_ID = 6;
    public static final int MAP_BEDROOM_ID = 7;
    public static final int MAP_SCHOOL_STAFFROOM_ID = 8;
    public static final int MAP_PE_ID = 9;
    public static final int MAP_SCHOOL_EXAM_HALL_ID = 10;
    public static final int MAP_SCHOOL_SHOP_ID = 11;

    public static final int NEW_GAME_X = 9;
    public static final int NEW_GAME_Y = 24;

    public static final int DT_X = 4;
    public static final int DT_Y = 6;

    public static final int FT_X = 14;
    public static final int FT_Y = 7;

    public static final int PE_X = 44;
    public static final int PE_Y = 38;

    public static final int PE_END_X = 27;
    public static final int PE_END_Y = 24;

    public static final int CHEM_X = 6;
    public static final int CHEM_Y = 7;

    public static final int ICT_X = 14;
    public static final int ICT_Y = 7;

    public static final int HOME_X = 6;
    public static final int HOME_Y = 2;

    public static final int EXAM_X = 8;
    public static final int EXAM_y = 8;

    public static final int NEW_GAME_MAP_ID = MAP_SCHOOL_HALL_G_ID;

    public static final int NUMBER_OF_DAYS = 31;

    public static final int TIME_MORNING = 0;
    public static final int TIME_LUNCH = 1;
    public static final int TIME_AFTER_SCHOOL = 2;
    public static final int TIME_EVENING = 3;
    public static final int TIME_HEIST_PHASE_1 = 4;
    public static final int TIME_HEIST_PHASE_2 = 5;

    public static final int TIME_CHEM = 6;
    public static final int TIME_ICT = 7;
    public static final int TIME_DT = 8;
    public static final int TIME_FT = 9;

    public static final int DT_INDEX = 0;
    public static final int FT_INDEX = 1;
    public static final int PE_INDEX = 2;
    public static final int CHEMISTRY_INDEX = 3;
    public static final int ICT_INDEX = 4;

    public static final int ATHLETE_INDEX = 0;
    public static final int CLASSMATE_INDEX = 1;
    public static final int NERD_INDEX = 2;
    public static final int DELINQUENT_INDEX = 3;
    public static final int TUTEE_INDEX = 4;

    public static final int DT_TEACHER_INDEX = 5;
    public static final int FT_TEACHER_INDEX = 6;
    public static final int PE_TEACHER_INDEX = 7;
    public static final int CHEM_TEACHER_INDEX = 8;
    public static final int ICT_TEACHER_INDEX = 9;

    public static final int BOY_INDEX = 20;
    public static final int GIRL_INDEX = 40;
    public static final int WOMAN_INDEX = 60;
    public static final int MAN_INDEX = 80;

    public static final int TRACK_CLUB_CUTSCENE = 0;
    public static final int CHEMISTRY_CUTSCENE = 1;
    public static final int TUTORING_CUTSCENE = 2;
    public static final int DT_HEIST_CUTSCENE = 3;
    public static final int CHEMISTRY_HEIST_CUTSCENE = 4;
    public static final int TUTORING_HEIST_CUTSCENE = 5;

    public static final int SFX_CLICK = 0;
    public static final int SFX_MOVE = 1;
    public static final int SFX_POINT = 2;
    public static final int SFX_MONEY = 3;
    public static final int SFX_BUFF = 4;
    public static final int SFX_DEBUFF = 5;
    public static final int SFX_DOOR = 6;
    public static final int SFX_STINKBOMB = 7;
    public static final int SFX_ALARM = 8;


    public static final int DT_BOOK_INDEX = 0;
    public static final int FT_BOOK_INDEX = 1;
    public static final int PE_BOOK_INDEX = 2;
    public static final int CHEM_BOOK_INDEX = 3;
    public static final int ICT_BOOK_INDEX = 4;

    public static final int DT_SHEET_INDEX = 5;
    public static final int FT_SHEET_INDEX = 6;
    public static final int PE_SHEET_INDEX = 7;
    public static final int CHEM_SHEET_INDEX = 8;
    public static final int ICT_SHEET_INDEX = 9;

    public static final int KEY0_INDEX = 10; //Key
    public static final int KEY1_INDEX = 11; //Chocolate
    public static final int KEY2_INDEX = 12; //Shoes
    public static final int KEY3_INDEX = 13; //Stink bomb
    public static final int KEY4_INDEX = 14; //USB
    public static final int KEY5_INDEX = 15; //Cake
    public static final int KEY6_INDEX = 30; //Exam Questions

    public static final int DRINK0_INDEX = 16;
    public static final int DRINK1_INDEX = 17;
    public static final int DRINK2_INDEX = 18;

    public static final int CANTEEN0_INDEX = 19;
    public static final int CANTEEN1_INDEX = 20;
    public static final int CANTEEN2_INDEX = 21;

    public static final int CRAFT_D_INDEX = 22;
    public static final int CRAFT_C_INDEX = 23;
    public static final int CRAFT_B_INDEX = 24;
    public static final int CRAFT_A_INDEX = 25;

    public static final int FOOD_D_INDEX = 26;
    public static final int FOOD_C_INDEX = 27;
    public static final int FOOD_B_INDEX = 28;
    public static final int FOOD_A_INDEX = 29;

    public static final int MAX_GP = 150;
    public static final int MAX_FP = 100;

    public static final int GRADE_INCREASE = 0;
    public static final int FRIEND_INCREASE = 1;
    public static final int FRIEND_DECREASE = -1;
    public static final int EXAM_INCREASE = 2;
    public static final int HEIST_BONUS = 3;

    public static final int NORMAL_CONDITION = 0;
    public static final int GREAT_CONDITION = 1;
    public static final int UNWELL_CONDITION = 2;

    public static final int INVENTORY_USE = 0;
    public static final int INVENTORY_GIVE = 1;
    public static final int INVENTORY_BUY = 2;
    public static final int INVENTORY_SELL = 3;
    public static final int INVENTORY_EQUIP = 4;
    public static final int INVENTORY_CRAFT = 5;

    public static final int PATROL_STOP_TICKS = 16;
    public static final float RANDOM_STOP_CHANCE = 0.05f;
    public static final float RANDOM_MOVE_CHANCE = 0.01f;

    public static final float GAME_BUFF_CHANCE = 0.75f;

    public static final int LESSON_MAX_SKILL = 6;
    public static final int LESSON_MAX_ATTN = 10;

    public static final int PE_MAP_LEFT_MARGIN = 21;
    public static final int PE_MAP_TOP_MARGIN = 9;

}
