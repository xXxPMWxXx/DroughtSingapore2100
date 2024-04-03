package com.example.savetherobot;

import android.app.Activity;
import android.content.Intent;

public class Water {
    private int waterLevel;
    private final long LIFE_DECREASE_INTERVAL = 5000; // Decrease water every 5 seconds
    private Thread waterThread;
    private boolean isRunning = true;

    public Water(int initialWaterLevel) {
        this.waterLevel = initialWaterLevel;
    }

    public Thread startWaterThread() {
        waterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(LIFE_DECREASE_INTERVAL); // Sleep for the interval
                        decreaseWater(); // Decrease water after the interval
                        System.out.println(waterLevel);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        waterThread.start(); // Start the thread
        return waterThread;
    }

    public synchronized void decreaseWater() {
        // Decrease water by 1
        waterLevel--;

        // If water reaches 0, end the game
        if (waterLevel <= 0) {
            endGame();
        }
    }

    private void endGame() {
        // Stop the water thread
        isRunning = false;
    }

    public synchronized int getWaterLevel() {
        return waterLevel;
    }

    public synchronized void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public synchronized void stopWaterThread() {
        isRunning = false;
    }
}
