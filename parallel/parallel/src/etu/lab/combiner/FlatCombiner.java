package etu.lab.combiner;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class FlatCombiner implements Combiner {

    private static final class Node {
        public Runnable request;
        public final AtomicInteger wait = new AtomicInteger();
        public boolean complete;
        public final AtomicReference<Node> next = new AtomicReference<Node>();
    }

    private ThreadLocal<Node> myNode = new ThreadLocal<Node>() {
        @Override
        protected Node initialValue() {
            return new Node();
        }
    };

    private final int limit;
    private final AtomicReference<Node> tail;

    public FlatCombiner(int limit) {
        this.limit = limit;
        this.tail = new AtomicReference<Node>(new Node());
    }

    public void combine(Runnable action, Combiner.IdleStrategy idleStrategy) {
        Node nextNode = myNode.get();
        nextNode.complete = false;
        nextNode.wait.set(1);

        Node curNode = tail.getAndSet(nextNode);
        myNode.set(curNode);
        
        curNode.request = action;
        curNode.next.lazySet(nextNode);

        int idleCount = 0;
        while (curNode.wait.get() == 1) {
            idleCount = idleStrategy.idle(idleCount);
        }

        if (curNode.complete)
            return;

        Node n = curNode;
        Node nn;
        for (int c = 0; c < limit && (nn = n.next.get()) != null; ++c, n = nn) {
            n.request.run();

            n.next.set(null);
            n.request = null;
            n.complete = true;
            n.wait.lazySet(0);
        }

        n.wait.set(0);
    }
}