package etu.lab;
import java.util.*;
import java.util.concurrent.*;

public class EliminationArray<T> {
    public Exchanger<T>[] exchangers;
    public final long TIMEOUT;
    public final TimeUnit UNIT;
    private Random random;

    @SuppressWarnings("unchecked")
    public EliminationArray(int capacity, long timeout, TimeUnit unit) {
        exchangers = new Exchanger[capacity];
        for (int i=0; i<capacity; i++)
            exchangers[i] = new Exchanger<>();
        random = new Random();
        TIMEOUT = timeout;
        UNIT = unit;
    }

    public T visit(T x) throws TimeoutException, InterruptedException {
        int i = random.nextInt(exchangers.length);
        return exchangers[i].exchange(x, TIMEOUT, UNIT);
    }
}