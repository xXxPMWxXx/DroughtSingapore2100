package com.example.droughtsingapore2100;

public class Water {
    private int waterLevel;
    private int maxWaterLevel = 10;
    private final long LIFE_DECREASE_INTERVAL = 5000; // Decrease water every 5 seconds
    private boolean isRunning = true;

    public Water(int initialWaterLevel) {
        this.waterLevel = initialWaterLevel;
    }

    public Thread startWaterThread() {
        // Sleep for the interval
        // Decrease water after the interval
        Thread waterThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(LIFE_DECREASE_INTERVAL); // Sleep for the interval
                    if (getWaterLevel() > 1) {
                        decreaseWater(1); // Decrease water after the interval
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        waterThread.start(); // Start the thread
        return waterThread;
    }

    public synchronized void decreaseWater(int amount) {
        // Decrease water by amount
        if(setWaterLevel(getWaterLevel() - amount) <= 0){
            setWaterLevel(0);
            // If water reaches 0, end the game
            stopWaterThread();
        }
    }

    public synchronized void increaseWater() {
        // If water reaches 0, end the game
        if (getWaterLevel() < maxWaterLevel) {
            waterLevel = setWaterLevel(getWaterLevel() + 1);
        }
    }

    //get water level synchronously
    public synchronized int getWaterLevel() {
        return waterLevel;
    }

    //set and return water level synchronously
    private synchronized int setWaterLevel(int amount) {
        waterLevel = amount;
        return amount;
    }

    //stop the current thread
    public synchronized void stopWaterThread() {
        isRunning = false;
    }
}
