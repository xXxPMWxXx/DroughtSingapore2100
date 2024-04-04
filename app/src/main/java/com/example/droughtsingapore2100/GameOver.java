package com.example.droughtsingapore2100;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GameOver extends AppCompatActivity {
    //Instance for DBUtils
    private DBUtils dbUtils;
    //declaring instance variables
    TextView tvPoints;
    TextView tvHighest;
    SharedPreferences sharedPreferences;
    ImageView ivNewHighest;
    //Override the onCreate method of AppCompatActivity
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the layout for this activity
        setContentView(R.layout.game_over);

        // Init DBUtils
        dbUtils = new DBUtils(this);

        //Init instance variables with their respective views from the layout
        tvPoints = findViewById(R.id.tvPoints);
//        tvHighest = findViewById(R.id.tvHighest);
//        ivNewHighest = findViewById(R.id.ivNewHighest);
        //Get the points passed from GameView
        int points = getIntent().getExtras().getInt("points");
        dbUtils.insertScore(points);

        //Set the points text view with the received points
        tvPoints.setText("" + points);

        // Fetch the top 3 scores
        List<DBUtils.ScoreEntry> topScores = dbUtils.getTopScores();

        TextView tvTop1 = findViewById(R.id.tvTop1);
        TextView tvTop2 = findViewById(R.id.tvTop2);
        TextView tvTop3 = findViewById(R.id.tvTop3);

        if (topScores.size() >= 1) {
            DBUtils.ScoreEntry top1 = topScores.get(0);
            tvTop1.setText(top1.getScore() + " on " + formatDate(top1.getDate()));
        } else {
            tvTop1.setText("-");
        }

        if (topScores.size() >= 2) {
            DBUtils.ScoreEntry top2 = topScores.get(1);
            tvTop2.setText(top2.getScore() + " on " + formatDate(top2.getDate()));
        } else {
            tvTop2.setText("-");
        }

        if (topScores.size() >= 3) {
            DBUtils.ScoreEntry top3 = topScores.get(2);
            tvTop3.setText(top3.getScore() + " on " + formatDate(top3.getDate()));
        } else {
            tvTop3.setText("-");
        }

    }


    // Method to format the date to display only month and date
    private String formatDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd");
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // Return original string if parsing fails
        }
    }

    // Define the restart method that is called when the restart button is clicked
    public void restart(View view) {
        // Start the GameView activity to start a new game
        GameView gameView = new GameView(GameOver.this);
        setContentView(gameView);
    }


    // Define the exit method that is called when the exit button is clicked
    public void exit(View view) {
        // Start the MainActivity activity to return to the main menu
        Intent intent = new Intent(GameOver.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
