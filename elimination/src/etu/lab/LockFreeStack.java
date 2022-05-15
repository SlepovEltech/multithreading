package etu.lab;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack<T> {
    AtomicReference<Node<T>> top = new AtomicReference<Node<T>>(null);
    static final int MIN_DELAY = 100;
    static final int MAX_DELAY = 1000;
    Backoff backoff = new Backoff(MIN_DELAY, MAX_DELAY);

    protected boolean tryPush(Node<T> node){
        Node<T> oldTop = top.get();
        node.next = oldTop;
        return(top.compareAndSet(oldTop, node));
    }
    public void push(T value) {
        Node<T> node = new Node<T>(value);
        while (true) {
            if (tryPush(node)) {
                return;
            } else {
                backoff.backoff();
            }
        }
    }

    protected Node<T> tryPop()  {
        Node<T> oldTop = top.get();
        if (oldTop == null) {
            return null;
        }
        Node<T> newTop = oldTop.next;
        if (top.compareAndSet(oldTop, newTop)) {
            return oldTop;
            } else {
            return null;
            }
        }

    public T pop()  {
        while (true) {
            Node<T> returnNode = tryPop();
            if (returnNode != null) {
                return returnNode.value;
            } else {
                backoff.backoff();
            }
        }
    }

}
