package com.example.droughtsingapore2100;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
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
    private MediaPlayer mediaPlayer, backgroundMediaPlayer;
    Rect rectBackground, rectGround; //define position and size of background and size images
    Context context; //to retrieve resources and perform other task
    Handler handler; //schedule tasks to be run
    final long UPDATE_MILLIS = 30; //time for each frame in game
    Runnable runnable; //update graphic
    Paint textPaint = new Paint(); //render text on screen
    Paint waterLevelPaint = new Paint(); //render the player's health bar
    Paint robotPaint = new Paint();
    float TEXT_SIZE = 120; //size of text on screen
    int points = 0; //player score
    static int dWidth, dHeight; //width and height of the game's display
    Random random; //generate random numbers for certain game
    float robotX, robotY; //position of player character
    float oldX;
    float oldRobotX; //character's position on previous frame
    ArrayList<Bomb> bombs;
    ArrayList<Explosion> explosions;
    ArrayList<Droplet> droplets;
    private final Water water;
    private final Thread waterThread;
    // To make the robot shake
    private boolean isShaking = false;
    private long shakeStartTime;
    private final long SHAKE_DURATION = 800;
    private volatile boolean gameOverTriggered = false; // Volatile keyword ensures visibility across threads

    public void startShaking() {
        if (!isShaking) {
            isShaking = true;
            shakeStartTime = System.currentTimeMillis();
        }
    }

    public GameView(Context context) {
        super(context);
        this.context = context;
//        //play game start audio
//        mediaPlayer = MediaPlayer.create(this.getContext(),R.raw.gamestart );
//        if (mediaPlayer == null) {
//            Log.e("MediaPlayer", "Failed to create MediaPlayer.");
//        } else {
//            mediaPlayer.start();
//        }

//        //play background music
//        backgroundMediaPlayer = MediaPlayer.create(this.getContext(), R.raw.bgmusic);
//        if (backgroundMediaPlayer == null) {
//            Log.e("backgroundMediaPlayer", "Failed to create backgroundMediaPlayer.");
//        } else {
//            backgroundMediaPlayer.start();
//        }

        initializeBackgroundMusic();

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
                handler.postDelayed(this, 1500);
            }
        }, 1500);

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
        waterLevelPaint.setColor(Color.parseColor("#3ea4f0"));

        //initial position of the robot
        random = new Random();
        robotX = dWidth / 2 - robot.getWidth() / 2;
        robotY = dHeight - ground.getHeight() - robot.getHeight();
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        droplets = new ArrayList<>();
        for (int i=0; i<4; i++){
            Bomb bomb = new Bomb(context);
            bombs.add(bomb);
        }
        water = new Water(10); // Initial water level
        waterThread = water.startWaterThread();
    }

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

        // For shaking effect
        if (isShaking) {
            // Check the duration
            if (System.currentTimeMillis() - shakeStartTime < SHAKE_DURATION) {
                // For shaking effect
                int shakeAmplitude = 20;
                robotX += (random.nextInt(shakeAmplitude * 2) - shakeAmplitude);
                robotPaint.setColorFilter(new LightingColorFilter(0xFFFFFF, 0xFFFF0000));
            } else {
                // Stop shaking after the duration
                isShaking = false;
                robotPaint.setColorFilter(null);
            }
        }

        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);

        canvas.drawBitmap(robot, robotX, robotY, robotPaint);
        // Load the bitmap from drawable resources
        waterBarBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_bar);

        //draw all bomb on the canvas
        for (Bomb bomb : bombs) {
            canvas.drawBitmap(bomb.getBomb(bomb.bombFrame), bomb.bombX, bomb.bombY, null);
            bomb.bombFrame++;
            if (bomb.bombFrame > 3) {
                bomb.bombFrame = 0;
            }
            //velocity & resets bomb position
            bomb.bombY += bomb.bombVelocity;
            if (bomb.bombY + bomb.getBombHeight() >= dHeight - ground.getHeight()) {
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = bomb.bombX;
                explosion.explosionY = bomb.bombY;
                explosions.add(explosion);
                bomb.resetPosition();
            }
        }

        //if robot collides with any bombs, decrease the player's water by 1
        for (Bomb bomb : bombs) {
            if (bomb.bombX + bomb.getBombWidth() >= robotX
                    && bomb.bombX <= robotX + robot.getWidth()
                    && bomb.bombY + bomb.getBombWidth() >= robotY
                    && bomb.bombY + bomb.getBombWidth() <= robotY + robot.getHeight()) {
                bomb.resetPosition();
                startShaking();
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
                water.decreaseWater(3);
                //if water level reach 0, redirect to game overview
                if (water.getWaterLevel() == 0 && !isGameOverTriggered()) {
                    // Set the flag to true to indicate that game over condition is triggered
                    setGameOverTriggered(true);

                    if (backgroundMediaPlayer != null) {
                        backgroundMediaPlayer.stop();
                        backgroundMediaPlayer.release();
                        backgroundMediaPlayer = null;
                    }

                    // Start game over activity
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
        }

        //draw all droplets on the canvas
        for (int i=0; i<droplets.size(); i++){
            canvas.drawBitmap(droplets.get(i).getDroplet(droplets.get(i).dropletFrame), droplets.get(i).dropletX, droplets.get(i).dropletY, null);
            droplets.get(i).dropletFrame++;
            if (droplets.get(i).dropletFrame > 3){
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

                //play water audio
                mediaPlayer = MediaPlayer.create(this.getContext(), R.raw.water);
                if (mediaPlayer == null) {
                    Log.e("MediaPlayer", "Failed to create MediaPlayer.");
                } else {
                    mediaPlayer.start();
                }
                water.increaseWater();
                points += 10;
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


        if (water.getWaterLevel() <= 5 && water.getWaterLevel() > 2){
            waterLevelPaint.setColor(Color.parseColor("#ffffbf"));
        } else if(water.getWaterLevel() <= 2){
            waterLevelPaint.setColor(Color.parseColor("#ff3632"));
        } else {
            waterLevelPaint.setColor(Color.parseColor("#3ea4f0"));

        }

        // Variables for drawing water level rectangle
        int maxWaterLevel = 10;
        int maxWidth = waterBarBitmap.getWidth() + 20;
        int padding = (int) (dWidth * 0.1) / 2;
        int currentWidth = (int) ((water.getWaterLevel() / (float) maxWaterLevel) * maxWidth);
        // For rectangle positioning
        int waterLevelRectRight = dWidth - padding;
        int waterLevelRectLeft = waterLevelRectRight - currentWidth;
        // Determine the scale factor for resizing the water bar bitmap
        float scaleFactor = dWidth / (float) waterBarBitmap.getWidth() * 0.5f;
        // Resize the bitmap => to make it bigger
        Bitmap scaledWaterBar = Bitmap.createScaledBitmap(waterBarBitmap, (int) (waterBarBitmap.getWidth() * scaleFactor), (int) (waterBarBitmap.getHeight() * scaleFactor), true);
        // For resolution 1080
        // Align the water bar image with the rectangle
        int waterBarImageX = waterLevelRectRight - scaledWaterBar.getWidth() - (int) (padding) + 150;
        int waterBarImageY = (120 + 50) / 2 - scaledWaterBar.getHeight() / 2;
        if(dWidth == 1440) {
            waterBarImageX += 70;
        }
        // Draw the water level
        canvas.drawRect(waterLevelRectLeft, 50, waterLevelRectRight, 120, waterLevelPaint);
        // Draw the bitmap first to ensure it's behind the rectangle
        canvas.drawBitmap(scaledWaterBar, waterBarImageX, waterBarImageY, null);
        
        //draw score on canvas
        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
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

    // Method to get the value of gameOverTriggered with synchronization
    private synchronized boolean isGameOverTriggered() {
        return gameOverTriggered;
    }

    // Method to set the value of gameOverTriggered with synchronization
    private synchronized void setGameOverTriggered(boolean value) {
        gameOverTriggered = value;
    }

    // Method to initialize background music
    private void initializeBackgroundMusic() {
        backgroundMediaPlayer = MediaPlayer.create(this.getContext(), R.raw.bgmusic);
        if (backgroundMediaPlayer == null) {
            Log.e("backgroundMediaPlayer", "Failed to create backgroundMediaPlayer.");
        } else {
            backgroundMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    // Replay background music when it ends
                    replayBackgroundMusic();
                }
            });
            backgroundMediaPlayer.start();
        }
    }

    // Method to replay the background music
    private void replayBackgroundMusic() {
        if (backgroundMediaPlayer != null) {
            backgroundMediaPlayer.stop();
            backgroundMediaPlayer.release();
        }
        backgroundMediaPlayer = MediaPlayer.create(this.getContext(), R.raw.bgmusic);
        if (backgroundMediaPlayer == null) {
            Log.e("backgroundMediaPlayer", "Failed to create backgroundMediaPlayer.");
        } else {
            backgroundMediaPlayer.start();
        }
    }

}
