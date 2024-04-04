package com.example.droughtsingapore2100;
// imports context class
import android.content.Context;
// imports bitmap class
import android.graphics.Bitmap;
// imports BitmapFactory class
import android.graphics.BitmapFactory;

// import class Random
import java.util.Random;
public class Bomb {
    Bitmap bomb[] = new Bitmap[4];
    int bombFrame = 0;
    int bombX, bombY, bombVelocity;
    Random random;
    // to initialize bomb bitmap array and load 3 bitmap images
    public Bomb(Context context){
        bomb[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb0);
        bomb[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb1);
        bomb[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb2);
        bomb[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb3);
        random = new Random();
        resetPosition();
    }

    // returns bitmap object from the bomb array
    public Bitmap getBomb(int bombFrame){

        return bomb[bombFrame];
    }

    // returns width of the first bitmap object
    public int getBombWidth(){

        return bomb[0].getWidth();
    }

    // returns height of the first bitmap object
    public int getBombHeight(){

        return bomb[0].getHeight();
    }

    // set a new random position, x and y coordinates and bombVelocity for bomb object
    public void resetPosition() {
        bombX = random.nextInt(GameView.dWidth - getBombWidth());
        bombY = -200 + random.nextInt(600) * -1;
        bombVelocity = 35 + random.nextInt(16);
    }
}
