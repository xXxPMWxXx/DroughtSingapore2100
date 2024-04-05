package com.example.droughtsingapore2100;

public class Water {
    private int waterLevel;
    private int maxWaterLevel = 10;
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
                        if(waterLevel != 1) {
                            decreaseWater(1); // Decrease water after the interval
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        waterThread.start(); // Start the thread
        return waterThread;
    }

    public synchronized void decreaseWater(int amount) {
        // Decrease water by amount

        //end game if water level below or equal 0
        if(getWaterLevel() - amount<=0){
            setWaterLevel(0);
            endGame();
        } else{
            setWaterLevel(getWaterLevel() - amount);
        };
    }

    public synchronized void increaseWater() {
        // If water reaches 0, end the game
        if (getWaterLevel() < maxWaterLevel) {
            setWaterLevel(getWaterLevel() + 1);
        }
    }

    private void endGame() {
        // Stop the water thread
        isRunning = false;
    }

    public synchronized int getWaterLevel() {
        return waterLevel;
    }

    public synchronized void setWaterLevel(int amount) {
        waterLevel = amount;
    }

    public synchronized void stopWaterThread() {
        isRunning = false;
    }
}
