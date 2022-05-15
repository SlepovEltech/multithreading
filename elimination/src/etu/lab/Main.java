package etu.lab;

import java.util.*;

public class Main {
    static int THREADS =2, OPS = 1000;

    final static Deque<Integer> classicalStack = new ArrayDeque<>();
    final static LockFreeStack<Integer> lockFreeStack = new LockFreeStack<>();
    final static EliminationBackoffStack<Integer> eliminationStack = new EliminationBackoffStack<>();

    static List<Integer>[] poppedValues;

    private enum stack{
        classical, lockFree, elimination
    }

    static Thread stackTest(int id,  stack stackOption) {
        return new Thread(() -> {
                for (int i = 0; i < 2 * OPS; i++) {
                    if (i % 2 == 0) {
                        try {
                            switch (stackOption) {
                                case classical:
                                    classicalStack.push(i);
                                    break;
                                case lockFree:
                                    lockFreeStack.push(i);
                                    break;
                                case elimination:
                                    eliminationStack.push(i);
                                    break;
                            }
                        } catch (Exception e) {
                            //System.out.println("Thread #"+id+": push() failed ");
                        }
                    } else {
                        try {
                            switch (stackOption) {
                                case classical:
                                    poppedValues[id].add(classicalStack.pop());
                                    break;
                                case lockFree:
                                    poppedValues[id].add(lockFreeStack.pop());
                                    break;
                                case elimination:
                                    poppedValues[id].add(eliminationStack.pop());
                                    break;
                            }
                        } catch (Exception e) {
                            //System.out.println("Thread #"+id+": push() failed ");
                        }

                    }

                }
        });
    }

    static boolean checkPopedValues(int N) {
        boolean passed = true;
        for (int i=0; i<THREADS; i++) {
            int n = poppedValues[i].size();
            if (n != N) {
                //System.out.println("Thread #"+ i +": popped "+n+"/"+N+" values");
                passed = false;
            }
        }
        return passed;
    }

    @SuppressWarnings("unchecked")
    static void threadTest(stack stackOption) {
        poppedValues = new List[THREADS];
        for (int i=0; i<THREADS; i++)
            poppedValues[i] = new ArrayList<>();
        Thread[] threads = new Thread[THREADS];
        for (int i=0; i<THREADS; i++) {
            threads[i] = stackTest(i, stackOption);
            threads[i].start();
        }
        try {
            for (int i=0; i<THREADS; i++)
                threads[i].join();
        }
        catch (Exception e) {}
    }

    public static void main(String[] args) {
        long startTime ,estimatedTime;

        System.out.println("===========================================");
        System.out.println("Starting "+THREADS+" threads with classical stack");
        startTime = System.nanoTime();
        threadTest(stack.classical);
        estimatedTime = System.nanoTime() - startTime;
        System.out.println("Check pops(): "+checkPopedValues(OPS));
        System.out.println("Time: " + estimatedTime / 10000);


        System.out.println("===========================================");
        System.out.println("Starting "+THREADS+" threads with elimination backoff stack");
        startTime = System.nanoTime();
        threadTest(stack.elimination);
        estimatedTime = System.nanoTime() - startTime;
        System.out.println("Check pops(): "+checkPopedValues(OPS));
        System.out.println("Time: " + estimatedTime / 10000);
        System.out.println("===========================================");

        System.out.println("Starting "+THREADS+" threads with classical lock free ");
        startTime = System.nanoTime();
        threadTest(stack.lockFree);
        estimatedTime = System.nanoTime() - startTime;
        System.out.println("Check pops(): "+checkPopedValues(OPS));
        System.out.println("Time: " + estimatedTime / 10000);

    }

}


