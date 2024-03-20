package com.example.savetherobot;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Water {
    Bitmap water[] = Bitmap[3];
    int waterFrame = 0;
    int waterX,waterY,waterVelocity;
    Random random;



    public Water(Context context) {
        water[0] = BitmapFactory.decodeResource(context.getResource(), R.drawable.water0);
        water[1] = BitmapFactory.decodeResource(context.getResource(), R.drawable.water1);
        water[2] = BitmapFactory.decodeResource(context.getResource(), R.drawable.water2);
        random = new Random();
        resetPostition();
    }

    public Bitmap getWater(int waterFrame) {
        return  water[waterFrame];
    }

    private Object getWaterWidth() {
        return water[0].getWidth();
    }

    private Object getWaterHeight() {
        return water[0].getHeight();
    }

    public void resetPostition() {
        waterX = random.nextInt(GameView.dWidth - getWaterWidth());
        waterY = -200 + random.nextInt(600) * -1;
        waterVelocity = 35 + random.nextInt(16);
    }




}
