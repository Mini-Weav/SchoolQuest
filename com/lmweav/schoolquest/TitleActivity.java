package com.lmweav.schoolquest;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.flexbox.FlexboxLayout;
import com.lmweav.schoolquest.utilities.BGMFader;
import com.lmweav.schoolquest.utilities.GameTextView;
import com.lmweav.schoolquest.utilities.billing.BillingManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Typeface.NORMAL;
import static com.lmweav.schoolquest.Constants.SFX_CLICK;
import static com.lmweav.schoolquest.Constants.SFX_DEBUFF;

/*
 * School Quest: TitleActivity
 * This class is the Android activity that runs the title screen and main menu.
 *
 * Methods in this class set up, alter and display UI elements found in the main menu.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class TitleActivity extends Activity {

    private static TitleActivity instance;

    private int currentLoadingTime = 0;
    private int loadingTime = 6;

    private boolean loading;

    private ObjectAnimator textAnimator1;
    private ObjectAnimator textAnimator2;

    private Runnable displayInputTextBox;
    private Runnable closeTextBox;
    private Runnable nameConfirm;
    private Runnable submitName;
    private Runnable deleteConfirm;
    private Runnable aboutGame;

    private String playerName;
    private String fileName;

    private MediaPlayer bgm;
    private transient static SoundPool sfx;
    private static int[] sfxIds;

    private BillingManager billingManager;

    List<String> skuList = new ArrayList<>();

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public static TitleActivity getInstance() { return instance; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_title);

        instance = this;
        skuList.add("donation");

        billingManager = new BillingManager(this);

        bgm = MediaPlayer.create(this, R.raw._music_theme);
        bgm.setLooping(true);
        bgm.start();

        loadSFX();

        ConstraintLayout titleScreen = findViewById(R.id.title_screen);
        final ConstraintLayout mainMenu = findViewById(R.id.main_menu);
        final ConstraintLayout mainMenuButtons = findViewById(R.id.main_menu_buttons);
        final ConstraintLayout dataButtons = findViewById(R.id.data_buttons);


        titleScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(SFX_CLICK);
                mainMenu.setVisibility(View.VISIBLE);
                mainMenuButtons.setVisibility(View.VISIBLE);
                changeBGM(R.raw._music_results);
                setLoadingScreen(true);
            }
        });

        ConstraintLayout newGameButton = findViewById(R.id.new_game_button);
        ConstraintLayout continueButton = findViewById(R.id.continue_button);
        final ConstraintLayout data1Button = findViewById(R.id.data1_button);
        final ConstraintLayout data2Button = findViewById(R.id.data2_button);
        final ConstraintLayout data3Button = findViewById(R.id.data3_button);
        final ConstraintLayout data4Button = findViewById(R.id.data4_button);

        final ImageView aboutButton = findViewById(R.id.about_button);
        final ImageView shareButton = findViewById(R.id.share_button);
        final ImageView donateButton = findViewById(R.id.donate_button);

        final ConstraintLayout textBox = findViewById(R.id.textbox);
        final ConstraintLayout textBoxInput = findViewById(R.id.textbox_box_input);
        final FlexboxLayout textBoxButton = findViewById(R.id.textbox_box_buttons);

        final GameTextView textBoxText = findViewById(R.id.textbox_box_text);


        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(SFX_CLICK);
                mainMenuButtons.setVisibility(View.GONE);
                dataButtons.setVisibility(View.VISIBLE);
                setDataClickListener(data1Button, true, "schoolQuest1.dat");
                setDataClickListener(data2Button, true, "schoolQuest2.dat");
                setDataClickListener(data3Button, true, "schoolQuest3.dat");
                setDataClickListener(data4Button, true, "schoolQuest4.dat");

            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(SFX_CLICK);
                mainMenuButtons.setVisibility(View.GONE);
                dataButtons.setVisibility(View.VISIBLE);
                setDataClickListener(data1Button, false, "schoolQuest1.dat");
                setDataClickListener(data2Button, false, "schoolQuest2.dat");
                setDataClickListener(data3Button, false, "schoolQuest3.dat");
                setDataClickListener(data4Button, false, "schoolQuest4.dat");
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(SFX_CLICK);
                textBox.setVisibility(View.VISIBLE);
                textBoxButton.setVisibility(View.VISIBLE);
                findViewById(R.id.textbox_box_buttons_no).setVisibility(View.GONE);
                ((GameTextView) findViewById(R.id.textbox_box_buttons_yes_text)).
                        setText("Full Attributions");
                findViewById(R.id.textbox_box_buttons_yes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        aboutGame.run();
                    }
                });
                textBoxInput.setVisibility(View.GONE);
                textBoxText.setText(R.string.credits);
                textBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textBox.setVisibility(View.GONE);
                        findViewById(R.id.textbox_box_buttons_no).setVisibility(View.VISIBLE);
                        ((GameTextView) findViewById(R.id.textbox_box_buttons_yes_text)).
                                setText("Yes");
                        textBox.setOnClickListener(null);
                    }
                });
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(SFX_CLICK);
                share();
            }
        });

        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(SFX_CLICK);
                billingManager.querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList,
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                                billingManager.startPurchaseFlow(skuDetailsList.get(0));
                            }
                        });
            }
        });



        setUpStartTextAnimation();

        GameTextView data1Text = findViewById(R.id.data1_text);
        GameTextView data1NGPlus = findViewById(R.id.data1_ngplus);
        GameTextView data1SubText = findViewById(R.id.data1_info);
        GameTextView data2Text = findViewById(R.id.data2_text);
        GameTextView data2NGPlus = findViewById(R.id.data2_ngplus);
        GameTextView data2SubText = findViewById(R.id.data2_info);
        GameTextView data3Text = findViewById(R.id.data3_text);
        GameTextView data3NGPlus = findViewById(R.id.data3_ngplus);
        GameTextView data3SubText = findViewById(R.id.data3_info);
        GameTextView data4Text = findViewById(R.id.data4_text);
        GameTextView data4NGPlus = findViewById(R.id.data4_ngplus);
        GameTextView data4SubText = findViewById(R.id.data4_info);

        setDataInfo(data1Text, data1SubText, data1NGPlus, "schoolQuest1.dat", "1.");
        setDataInfo(data2Text, data2SubText, data2NGPlus, "schoolQuest2.dat", "2.");
        setDataInfo(data3Text, data3SubText, data3NGPlus, "schoolQuest3.dat", "3.");
        setDataInfo(data4Text, data4SubText, data4NGPlus, "schoolQuest4.dat", "4.");

        EditText nameInput = findViewById(R.id.textbox_box_input_text);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/press_start_2p.ttf");
        nameInput.setTypeface(tf ,NORMAL);

        setTextBoxRunnables();


        final Handler handler = new Handler();
        class MyRunnable implements Runnable {
            private Handler handler;
            private MyRunnable(Handler handler) {
                this.handler = handler;
            }
            @Override
            public void run() {
                this.handler.postDelayed(this, 50);
                if (loading) {
                    if (loadingTime <= 0) { return; }
                    if (currentLoadingTime > loadingTime) {
                        setLoadingScreen(false);
                        currentLoadingTime = 0;
                    } else { currentLoadingTime++; }
                }
            }
        }
        handler.post(new MyRunnable(handler));
    }

    private void setUpStartTextAnimation() {
        GameTextView startText = findViewById(R.id.startText);
        textAnimator1 = ObjectAnimator.ofFloat(startText, "alpha",
                0f, 1f);
        textAnimator1.setEvaluator(new FloatEvaluator());
        textAnimator1.setDuration(0);
        textAnimator1.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textAnimator2.start();
                    }
                }, 500);
            }
        });

        textAnimator2 = ObjectAnimator.ofFloat(startText, "alpha",
                1f, 0f);
        textAnimator2.setEvaluator(new FloatEvaluator());
        textAnimator2.setDuration(0);
        textAnimator2.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textAnimator1.start();
                    }
                }, 500);
            }
        });
        textAnimator1.start();
    }

    public void setDataInfo(GameTextView dataText, GameTextView dataSubText, GameTextView NGPlus,
                            String fileName, String fileNumber) {
        Game data;

        try {
            FileInputStream saveData = openFileInput(fileName);
            ObjectInputStream in = new ObjectInputStream(saveData);
            data = (Game) in.readObject();
            in.close();
            saveData.close();

            String time = Game.getTimeKey(data.getTime());
            time = time.substring(0,1).toUpperCase() + time.substring(1);

            String data1Name = fileNumber + data.getPlayer().getName();
            int days = (Constants.NUMBER_OF_DAYS - data.getDay());
            if (days < 0) { days = 0; }
            String data1Info = (days + " days/" + time);

            if (data.isNGPlus()) { NGPlus.setVisibility(View.VISIBLE);}
            else { NGPlus.setVisibility(View.GONE); }

            dataText.setText(data1Name);
            dataSubText.setText(data1Info);
        } catch (NullPointerException e) {
            e.printStackTrace();
            dataText.setText("CORRUPTED");
            dataSubText.setText(" ");
        } catch (Exception e) {
            dataText.setText("No Data");
            dataSubText.setText(" ");
            e.printStackTrace();
        }

    }

    public void setDataClickListener(final ConstraintLayout button, final boolean newGame,
                                     final String fileName) {
        final File file = new File(getApplicationContext().getFilesDir(),fileName);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(SFX_CLICK);
                instance.fileName = fileName;
                if (newGame) {
                    if (file.exists()) { deleteConfirm.run(); }
                    else {
                        displayInputTextBox.run();
                    }

                } else {
                    Intent intent = new Intent(instance, GameActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("newGame", false);
                    bundle.putString("data", fileName);
                    playerName = ((GameTextView) button.getChildAt(0)).getText().
                            toString().substring(2);
                    bundle.putString("name", playerName);
                    intent.putExtras(bundle);
                    BGMFader.stop(bgm, 100);
                    setLoadingScreen(true, -1);
                    startActivity(intent);
                }
            }
        });

        if (!newGame) {
            if(!file.exists()){
                button.setAlpha(0.5f);
                button.setEnabled(false);
            }
        } else {
            button.setAlpha(1f);
            button.setEnabled(true);
        }
    }


    public void setTextBoxRunnables() {
        final ConstraintLayout textBox = findViewById(R.id.textbox);
        final ConstraintLayout textBoxInput = findViewById(R.id.textbox_box_input);
        final FlexboxLayout textBoxButton = findViewById(R.id.textbox_box_buttons);

        final GameTextView textBoxText = findViewById(R.id.textbox_box_text);

        final ConstraintLayout textBoxInputButton = findViewById(R.id.textbox_box_input_button);
        final ConstraintLayout textBoxYesButton = findViewById(R.id.textbox_box_buttons_yes);
        final ConstraintLayout textBoxNoButton = findViewById(R.id.textbox_box_buttons_no);

        final EditText nameInput = findViewById(R.id.textbox_box_input_text);


        displayInputTextBox = new Runnable() {
            @Override
            public void run() {
                textBox.setVisibility(View.VISIBLE);
                textBoxInput.setVisibility(View.VISIBLE);
                textBoxButton.setVisibility(View.GONE);
                textBoxText.setText("What is your name?");
                textBoxInputButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSFX(SFX_CLICK);
                        submitName.run();
                    }
                });
            }
        };

        closeTextBox = new Runnable() {
            @Override
            public void run() {
                textBox.setVisibility(View.GONE);
                textBoxInput.setVisibility(View.GONE);
                textBoxButton.setVisibility(View.GONE);
            }
        };

        submitName = new Runnable() {
            @Override
            public void run() {
                playerName = nameInput.getText().toString();

                if (playerName.length() > 0) {
                    InputMethodManager inputMethodManager = (InputMethodManager) instance.
                            getSystemService(Activity.INPUT_METHOD_SERVICE);

                    View view = instance.getCurrentFocus();
                    assert inputMethodManager != null;
                    assert view != null;
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    textBox.setVisibility(View.VISIBLE);
                    textBoxInput.setVisibility(View.GONE);
                    textBoxButton.setVisibility(View.VISIBLE);
                    textBoxText.setText("Your name is " + playerName + "?");
                    textBoxYesButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playSFX(SFX_CLICK);
                            nameInput.setText("");
                            nameConfirm.run();
                        }
                    });
                    textBoxNoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playSFX(SFX_CLICK);
                            displayInputTextBox.run();
                        }
                    });
                } else {
                    playSFX(SFX_DEBUFF);
                }
            }
        };

        deleteConfirm = new Runnable() {
            @Override
            public void run() {
                textBox.setVisibility(View.VISIBLE);
                textBoxInput.setVisibility(View.GONE);
                textBoxButton.setVisibility(View.VISIBLE);
                textBoxText.setText("Your existing save data will be overwritten. Is that ok?");
                textBoxYesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSFX(SFX_CLICK);
                        displayInputTextBox.run();
                    }
                });
                textBoxNoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSFX(SFX_CLICK);
                        closeTextBox.run();
                    }
                });
            }
        };

        nameConfirm = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(instance, GameActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("newGame", true);
                bundle.putString("data", fileName);
                bundle.putString("name", playerName);
                intent.putExtras(bundle);
                BGMFader.stop(bgm, 100);
                setLoadingScreen(true, -1);
                startActivity(intent);
            }
        };

        aboutGame = new Runnable() {
            @Override
            public void run() {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://schoolquestgame.com/attributions"));
                startActivity(browserIntent);
            }
        };
    }

    public void reset() {
        GameTextView data1Text = findViewById(R.id.data1_text);
        GameTextView data1NGPlus = findViewById(R.id.data1_ngplus);
        GameTextView data1SubText = findViewById(R.id.data1_info);
        GameTextView data2Text = findViewById(R.id.data2_text);
        GameTextView data2NGPlus = findViewById(R.id.data2_ngplus);
        GameTextView data2SubText = findViewById(R.id.data2_info);
        GameTextView data3Text = findViewById(R.id.data3_text);
        GameTextView data3NGPlus = findViewById(R.id.data3_ngplus);
        GameTextView data3SubText = findViewById(R.id.data3_info);
        GameTextView data4Text = findViewById(R.id.data4_text);
        GameTextView data4NGPlus = findViewById(R.id.data4_ngplus);
        GameTextView data4SubText = findViewById(R.id.data4_info);

        setDataInfo(data1Text, data1SubText, data1NGPlus, "schoolQuest1.dat", "1.");
        setDataInfo(data2Text, data2SubText, data2NGPlus, "schoolQuest2.dat", "2.");
        setDataInfo(data3Text, data3SubText, data3NGPlus, "schoolQuest3.dat", "3.");
        setDataInfo(data4Text, data4SubText, data4NGPlus, "schoolQuest4.dat", "4.");

        changeBGM(R.raw._music_theme);
        findViewById(R.id.textbox).setVisibility(View.GONE);
        findViewById(R.id.data_buttons).setVisibility(View.GONE);
        findViewById(R.id.main_menu_buttons).setVisibility(View.GONE);
        findViewById(R.id.main_menu).setVisibility(View.GONE);
        setLoadingScreen(false);
    }

    public void loadSFX() {
        sfx = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        sfxIds = new int[10];
        sfxIds[SFX_CLICK] = sfx.load(this, R.raw._sfx_click, 0);
        sfxIds[SFX_DEBUFF] = sfx.load(this, R.raw._sfx_debuff, 1);
    }

    public void playSFX(int id) {
        sfx.play(sfxIds[id], 1, 1, 1, 0, 1.0f);
    }

    public void changeBGM(final int newBGM, final int... pos) {
        BGMFader.stop(bgm, 400, new Runnable() {
            @Override
            public void run() {
                bgm.reset();

                AssetFileDescriptor afd = getResources().openRawResourceFd(newBGM);
                if (afd == null) return;

                try {
                    bgm.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                            afd.getLength());
                    afd.close();
                    bgm.prepare();
                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                }

                bgm.setLooping(true);
                if (pos.length > 0) { bgm.seekTo(pos[0]); }
            }
        });
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
                loading = false;
                BGMFader.start(bgm, 100);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        loadingScreen.setAnimation(fadeOut);
        loadingScreen.getAnimation().start();
        loadingScreen.setVisibility(View.GONE);
    }

    public void setLoadingScreen(boolean loading, int... frames) {
        currentLoadingTime = 0;

        if (frames.length == 0) { loadingTime = 6; }
        else { loadingTime = frames[0]; }

        if (loading) {
            this.loading = true;
            enableLoadingScreen();
        }
        else {
            disableLoadingScreen();
        }
    }

    public void share() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Play School Quest for FREE!");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share));

        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.textbox).getVisibility() == View.VISIBLE) {
            playSFX(SFX_CLICK);
            findViewById(R.id.textbox).setVisibility(View.GONE);
        } else if (findViewById(R.id.data_buttons).getVisibility() == View.VISIBLE) {
            playSFX(SFX_CLICK);
            findViewById(R.id.data_buttons).setVisibility(View.GONE);
            findViewById(R.id.main_menu_buttons).setVisibility(View.VISIBLE);
        } else if (findViewById(R.id.main_menu_buttons).getVisibility() == View.VISIBLE) {
            playSFX(SFX_CLICK);
            findViewById(R.id.main_menu_buttons).setVisibility(View.GONE);
            findViewById(R.id.main_menu).setVisibility(View.GONE);
            changeBGM(R.raw._music_theme);
            setLoadingScreen(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        bgm.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!bgm.isPlaying()) { bgm.start(); }
    }
}
