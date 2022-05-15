package etu.lab;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class EliminationBackoffStack<T> extends Stack<T>{
    AtomicReference<Node<T>> top;
    EliminationArray<T> eliminationArray;
    static final int CAPACITY = 100;
    static final long TIMEOUT = 10;
    static final TimeUnit UNIT = TimeUnit.MILLISECONDS;

    public EliminationBackoffStack() {
        top = new AtomicReference<>(null);
        eliminationArray = new EliminationArray<>(
                CAPACITY, TIMEOUT, UNIT
        );
    }

    public T push(T value) {
        Node<T> n = new Node<>(value);
        while (true) {
            if (tryPush(n)) return value;
            try {
                T y = null;
                try {
                    y = eliminationArray.visit(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (y == null) return value;
            }
            catch (TimeoutException e) {}
        }
    }

    public T pop() throws EmptyStackException {
        while (true) {
            Node<T> n = tryPop();         
            if (n != null) return n.value; 
            try {
                T y = null; 
                try {
                    y = eliminationArray.visit(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (y != null) return y;            
            }
            catch (TimeoutException e) {} 
        }
    }

    protected boolean tryPush(Node<T> n) {
        Node<T> m = top.get();
        n.next = m;                     
        return top.compareAndSet(m, n);
    }

    protected Node<T> tryPop() throws EmptyStackException {
        Node<T> m = top.get();                          
        if (m == null) throw new EmptyStackException(); 
        Node<T> n = m.next;                       
        return top.compareAndSet(m, n)? m : null;
    }
}
