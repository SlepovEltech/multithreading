package etu.lab;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class LockFreeExchanger<T> {
    AtomicStampedReference<T> slot;
    static final int EMPTY = 0;
    static final int WAITING = 1;
    static final int BUSY = 2;

    public LockFreeExchanger() {
        slot = new AtomicStampedReference<>(null, 0);
    }

    public T exchange(T y, long timeout, TimeUnit unit)
            throws TimeoutException {
        long w = unit.toNanos(timeout);
        long W = System.nanoTime() + w;
        int[] stamp = {EMPTY};
        while (System.nanoTime() < W) {
            T x = slot.get(stamp);
            switch (stamp[0]) {
                case EMPTY:
                    if (add(y)) {
                        while (System.nanoTime() < W)
                            if ((x = remove()) != null) return x;
                        throw new TimeoutException();
                    }
                    break;
                case WAITING:
                    if (add(x, y))
                        return x;
                    break;
                case BUSY:
                    break;
                default:
            }
        }
        throw new TimeoutException();
    }

    private boolean add(T y) { 
        return slot.compareAndSet(null, y, EMPTY, WAITING);
    }

    private boolean add(T x, T y) {
        return slot.compareAndSet(x, y, WAITING, BUSY);
    }

    private T remove() {
        int[] stamp = {EMPTY};
        T x = slot.get(stamp);
        if (stamp[0] != BUSY) return null;
        slot.set(null, EMPTY);
        return x;
    }
}