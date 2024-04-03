package com.example.savetherobot;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.Random;

public class Droplet {
    Bitmap droplets[] = new Bitmap[4];
    int dropletFrame = 0;
    int dropletX, dropletY, dropletVelocity;
    Random random;

    public Droplet(Context context){
        droplets[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.waterdroplet0);
        droplets[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.waterdroplet1);
        droplets[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.waterdroplet2);
        droplets[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.waterdroplet3);
        random = new Random();
        resetPosition();
    }

    public Bitmap getDroplet(int dropletFrame){
        return droplets[dropletFrame];
    }

    // returns width of the first bitmap object
    public int getDropletWidth(){
        return droplets[0].getWidth();
    }

    // returns height of the first bitmap object
    public int getDropletHeight(){

        return droplets[0].getHeight();
    }

    // set a new random position, x and y coordinates and velocity for the object
    public void resetPosition() {
        dropletX = random.nextInt(GameView.dWidth - getDropletWidth());
        dropletY = -200 + random.nextInt(600) * -1;
        dropletVelocity = 35 + random.nextInt(16);
    }

}
