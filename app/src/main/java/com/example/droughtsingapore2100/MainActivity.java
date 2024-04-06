package com.example.droughtsingapore2100;

import android.app.ActionBar;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private GameView gameView; // Member variable to hold the GameView instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep the screen on while the app is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            // Never show the action bar if the status bar is hidden, so hide that too if necessary.
            ActionBar actionBar = getActionBar();
            if(actionBar != null){
                actionBar.hide();
            }
        }
        // Set the layout to the activity_main.xml file
        setContentView(R.layout.activity_main);
    }

    public void startGame(View view) {
        gameView = new GameView(this);
        // Set the layout to the GameView object
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity", "onPause() called");
        super.onPause();
        // Pause the background music when the activity is paused
        if (gameView != null) {
            gameView.pauseBackgroundMusic();
        }
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity", "onStop() called");
        super.onStop();
        // Pause the background music when the activity is stopped
        if (gameView != null) {
            gameView.pauseBackgroundMusic();
        }
    }

    @Override
    protected void onResume() {
        Log.d("MainActivity", "onResume() called");
        super.onResume();
        // Resume the background music when the activity is resumed
        if (gameView != null) {
            gameView.resumeBackgroundMusic();
        }
    }
}