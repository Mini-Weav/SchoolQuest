package com.lmweav.schoolquest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/*
 * School Quest: SplashActivity
 * This class is the Android activity that runs the splash screen.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class SplashActivity extends Activity {

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent intent = new Intent(getApplicationContext(), TitleActivity.class);
        startActivity(intent);
        finish();
    }
}
