package etu.lab;

import java.util.Random;

public class Backoff {
    final int minDelay, maxDelay;
    int limit;
    final Random random;
    public Backoff(int min, int max) {
        minDelay = min;
        maxDelay = min;
        limit = minDelay;
        random = new Random();
    }
    public void backoff()  {
        int delay = random.nextInt(limit);
        limit = Math.min(maxDelay, 2 * limit);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}