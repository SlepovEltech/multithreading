package etu.lab.clh;

import etu.lab.AbstractLock;

import java.util.concurrent.atomic.AtomicReference;

public class CLHLock extends AbstractLock {
    private static AtomicReference<CLHNode> tail = new AtomicReference<>(new CLHNode());
    private ThreadLocal<CLHNode> myPred;
    private ThreadLocal<CLHNode> myNode;

    public static class CLHNode {
        public volatile boolean locked = false;
    }

    public CLHLock() {
        myNode = ThreadLocal.withInitial(() -> new CLHNode());
        myPred = ThreadLocal.withInitial(() -> null);
    }

    public void lock() {
        CLHNode que_node = myNode.get();
        que_node.locked = true;
        CLHNode pred = tail.getAndSet(que_node);
        myPred.set(pred);
        while (pred.locked) {  }
    }

    public void unlock() {
        CLHNode que_node = myNode.get();
        que_node.locked = false;
        myNode.set(myPred.get());
    }

}