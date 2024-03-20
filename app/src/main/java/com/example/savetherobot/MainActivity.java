package com.example.savetherobot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout to the activity_main.xml file
        setContentView(R.layout.activity_main);
        // Keep the screen on while the app is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void startGame(View view) {
        GameView gameView = new GameView(this);
        // Set the layout to the GameView object
        setContentView(gameView);
    }
}