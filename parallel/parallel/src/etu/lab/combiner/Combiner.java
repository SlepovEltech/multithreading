package etu.lab.combiner;

public interface Combiner {
    interface IdleStrategy {
        int idle(int idleCount);

        static IdleStrategy busySpin() {
            return ignore -> ignore;
        }

        static IdleStrategy yield(int maxSpins) {
            return idleCount -> {
                if (idleCount < maxSpins) {
                    idleCount++;
                } else {
                    Thread.yield();
                }
                return idleCount;
            };
        }
    }

    void combine(Runnable action, IdleStrategy idleStrategy);
}
