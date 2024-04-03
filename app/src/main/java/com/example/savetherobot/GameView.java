package com.example.savetherobot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Vibrator;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;
public class GameView extends View{
    Bitmap background, ground, robot; //display images in the game
    private MediaPlayer mediaPlayer;

    Rect rectBackground, rectGround; //define position and size of background and size images
    Context context; //to retrieve resources and perform other task
    Handler handler; //schedule tasks to be run
    final long UPDATE_MILLIS = 30; //time for each frame in game
    Runnable runnable; //update graphic
    Paint textPaint = new Paint(); //render text on screen
    Paint healthPaint = new Paint(); //render the player's health bar
    float TEXT_SIZE = 120; //size of text on screen
    int points = 0; //player score
//    int water = 10; //remaining water
    static int dWidth, dHeight; //width and height of the game's display
    Random random; //generate random numbers for certain game
    float robotX, robotY; //position of player character
    float oldX;
    float oldRobotX; //character's position on previous frame
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;
    ArrayList<Droplet> droplets;

//    private final long LIFE_DECREASE_INTERVAL = 5000; // Decrease water every 5 seconds
    private final Water water;
    private final Thread waterThread;

    public GameView(Context context) {
        super(context);
        this.context = context;
        //play game start audio
        mediaPlayer = MediaPlayer.create(this.getContext(),R.raw.gamestart );
        if (mediaPlayer == null) {
            Log.e("MediaPlayer", "Failed to create MediaPlayer.");
        } else {
            mediaPlayer.start();
        }

        //Load the background, ground, and robot Bitmap images from the app's resources
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        robot = BitmapFactory.decodeResource(getResources(), R.drawable.robot_blue);


        //Get the device screen size
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;

        //Create Rect objects to define the positions and sizes of the background and ground
        rectBackground = new Rect(0,0,dWidth,dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);

        //Create a Handler object to manage tasks that need to be run on the UI thread
        handler = new Handler();

        //Post a delayed Runnable to the Handler's message queue to executed after a certain delay
        handler.postDelayed(new Runnable() {
            @Override
            //generate droplet and schedules the next execution
            public void run() {
                generateDroplet();
                handler.postDelayed(this, 3000);
            }
        }, 3000);

        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate(); //View method that invalidates the entire view, causing it to be redrawn on the next frame
            }
        };
        //objects used for drawing text on the screen
        textPaint.setColor(Color.rgb(255, 165, 0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.b04));
        healthPaint.setColor(Color.BLUE);

        //initial position of the robot
        random = new Random();
        robotX = dWidth / 2 - robot.getWidth() / 2;
        robotY = dHeight - ground.getHeight() - robot.getHeight();
        spikes = new ArrayList<>();
        explosions = new ArrayList<>();
        droplets = new ArrayList<>();
        for (int i=0; i<3; i++){
            Spike spike = new Spike(context);
            Droplet droplet = new Droplet(context);
            spikes.add(spike);
            droplets.add(droplet);
        }
        water = new Water(10); // Initial water level
        waterThread = water.startWaterThread();
    }

//    private void startWaterThread() {
//        waterThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (isRunning) {
//                    try {
//                        Thread.sleep(LIFE_DECREASE_INTERVAL); // Sleep for the interval
//                        decreaseWater(); // Decrease water after the interval
//                        System.out.println(water);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        waterThread.start(); // Start the thread
//    }

//    private synchronized void decreaseWater() {
//        // Decrease water by 1
//        water--;
//
//        // If water reaches 0, end the game
//        if (water <= 0) {
//            endGame();
//        }
//    }

//    private void endGame() {
//        // Stop the water thread
//        isRunning = false;
//
//        // Redirect to game over activity
//        Intent intent = new Intent(context, GameOver.class);
//        intent.putExtra("points", points);
//        context.startActivity(intent);
//        ((Activity) context).finish();
//    }

    // Override onDetachedFromWindow to stop the thread when the view is detached
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        water.stopWaterThread();
        try {
            waterThread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //create new Droplet object and add into 'droplets' Arraylist
    private void generateDroplet(){
        Droplet droplet = new Droplet(getContext());
        droplets.add(droplet);
    }

    private Bitmap waterBarBitmap;
    @Override
    //responsible for drawing tha game screen
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(robot, robotX, robotY, null);
        // Load the bitmap from drawable resources
        waterBarBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_bar);

        //draw all spike on the canvas
        for (Spike spike : spikes) {
            canvas.drawBitmap(spike.getSpike(spike.spikeFrame), spike.spikeX, spike.spikeY, null);
            spike.spikeFrame++;
            if (spike.spikeFrame > 2) {
                spike.spikeFrame = 0;
            }
            //velocity & resets spike position
            spike.spikeY += spike.spikeVelocity;
            if (spike.spikeY + spike.getSpikeHeight() >= dHeight - ground.getHeight()) {
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = spike.spikeX;
                explosion.explosionY = spike.spikeY;
                explosions.add(explosion);
                spike.resetPosition();
            }
        }

        //if robot collides with any spikes, decrease the player's water by 1
        for (Spike spike : spikes) {
            if (spike.spikeX + spike.getSpikeWidth() >= robotX
                    && spike.spikeX <= robotX + robot.getWidth()
                    && spike.spikeY + spike.getSpikeWidth() >= robotY
                    && spike.spikeY + spike.getSpikeWidth() <= robotY + robot.getHeight()) {
                spike.resetPosition();
                //play explosion audio
                mediaPlayer = MediaPlayer.create(this.getContext(), R.raw.explosion);
                if (mediaPlayer == null) {
                    Log.e("MediaPlayer", "Failed to create MediaPlayer.");
                } else {
                    mediaPlayer.start();
                }
                //vibrate upon contact with bomb
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        Log.d("vibrate", "vibrate");
                    } else {
                        // Deprecated in API 26 (Android 8.0 Oreo)
                        vibrator.vibrate(500);
                    }
                }
                //if player's water reach 0, redirect to game overview
                if (water.getWaterLevel() <= 0) {
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
                water.decreaseWater();
            }
        }

        //draw all droplets on the canvas
        for (int i=0; i<droplets.size(); i++){
            canvas.drawBitmap(droplets.get(i).getDroplet(droplets.get(i).dropletFrame), droplets.get(i).dropletX, droplets.get(i).dropletY, null);
            droplets.get(i).dropletFrame++;
            if (droplets.get(i).dropletFrame > 1){
                droplets.get(i).dropletFrame = 0;
            }
            //Droplet position based on its velocity, and removes Droplet when it reaches the ground
            droplets.get(i).dropletY += droplets.get(i).dropletVelocity;
            if (droplets.get(i).dropletY + droplets.get(i).getDropletHeight() >= dHeight - ground.getHeight()){
                droplets.remove(i);
            }
        }

        //if robot collides with any droplet, increase the water level
        for (Droplet droplet : droplets){
            if (droplet.dropletX + droplet.getDropletWidth() >= robotX
                    && droplet.dropletX <= robotX + robot.getWidth()
                    && droplet.dropletY + droplet.getDropletWidth() >= robotY
                    && droplet.dropletY + droplet.getDropletWidth() <= robotY + robot.getHeight()){
                water.increaseWater();
                droplet.resetPosition();
            }
        }

        //draw all explosions on the canvas
        for (int i=0; i<explosions.size(); i++){
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).explosionX,
                    explosions.get(i).explosionY, null);
            explosions.get(i).explosionFrame++;
            if (explosions.get(i).explosionFrame > 3){
                explosions.remove(i);
            }
        }

        if (water.getWaterLevel() == 4){
            healthPaint.setColor(Color.YELLOW);
        } else if(water.getWaterLevel() == 2){
            healthPaint.setColor(Color.RED);
        }
        //draw player's score and water on canvas
        // Draw the health bar

        canvas.drawRect(dWidth - 200 - 60 * water.getWaterLevel(), 30, dWidth - 100, 80, healthPaint);
        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
        // Assuming waterBarBitmap is your loaded bitmap
        int x = dWidth - 300;
        int y = 30;

        // Adjust the position where you want to draw the bitmap if necessary
        canvas.drawBitmap(waterBarBitmap, x, y, null);
        //creates loop that updates the game state and draws the game screen repeatedly
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }


    @Override
    //whenever user interacts with the screen and receives motion
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        //checks if the touch event occurred below the current y-coordinate of the robot, indicating that the touch to control the robot.
        if (touchY >= robotY){
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN){
                oldX = event.getX();
                oldRobotX = robotX;
            }
            //if the action is a finger moving on the screen, the code calculates the horizontal shift of the finger
            if (action == MotionEvent.ACTION_MOVE){
                float shift = oldX - touchX;
                //calculates the new x-coordinate of the robot
                float newRobotX = oldRobotX - shift;
                //if the new position is less than or equal to 0, the robot is moved to the leftmost edge of the screen
                if (newRobotX <= 0)
                    robotX = 0;
                    //else to the right
                else if(newRobotX >= dWidth - robot.getWidth())
                    robotX = dWidth - robot.getWidth();
                else
                    //or new position based on the finger shift
                    robotX = newRobotX;
            }
        }
        return true;
    }

}
