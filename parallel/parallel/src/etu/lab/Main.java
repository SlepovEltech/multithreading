package etu.lab;

import etu.lab.clh.CLHLock;
import etu.lab.combiner.Combiner;
import etu.lab.combiner.FlatCombiner;
import etu.lab.mcs.MCSLock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private final static int threads = 10;

    static final int ops = 1 << 20;
    static CountDownLatch latch;

    private final static MCSLock mcs_lock = new MCSLock();
    private final static CLHLock clh_lock = new CLHLock();
    private final static FlatCombiner combiner_lock = new FlatCombiner(100);

    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        for(int i = 0; i < 3; i++){
        	count.getAndSet(0);
            latch = new CountDownLatch(threads);
            long startTime = System.nanoTime();
            switch (i){
                case 0:
                    testFlatCombiner();
                    break;
                case 2:
                    testCLHorMCS(mcs_lock);
                    break;
                case 1:
                    testCLHorMCS(clh_lock);
                    break;
            }
            do {
                try {
                    latch.await();
                    break;
                } catch (InterruptedException e) {}
            } while (true);

            long estimatedTime = System.nanoTime() - startTime;
            System.out.println("Locks count: " + count);
            System.out.println("Time: " + estimatedTime / 1000000);
            System.out.println("===================================================================");

        }
    }


    private static void testFlatCombiner() {
        System.out.println("TEST: Flat combiner. Threads: "+threads+" Ops: "+ops);
        for (int i = 0; i < threads; ++i) {
            new Thread(() -> {
                for (int j = 0; j < ops; ++j) {
                    combiner_lock.combine(() -> {
                        count.incrementAndGet();
                    }, Combiner.IdleStrategy.yield(100));
                }
                //System.out.println("Thread # " + Thread.currentThread().getId() + " shutdown");
                latch.countDown();
            }).start();
        }
    }

    private static void testCLHorMCS(AbstractLock lock) {
        System.out.println("TEST "+lock.getClass()+". Threads: "+threads+" Ops: "+ops+"\n");
        for (int i = 0; i < threads; ++i) {
            new Thread(() -> {
                for (int j = 0; j < ops; ++j) {
                    lock.lock();
                    count.incrementAndGet();
                    //System.out.println("Thread # " + Thread.currentThread().getId() + " in section");

                    lock.unlock();

                }
                //System.out.println("Thread # " + Thread.currentThread().getId() + " shutdown");
                latch.countDown();
            }).start();
        }
    }

}