package etu.lab.mcs;

import etu.lab.AbstractLock;
import java.util.concurrent.atomic.AtomicReference;

public class MCSLock extends AbstractLock {

    private class QNode{
        volatile boolean wait = false;
        volatile QNode next = null ;
    }

    AtomicReference<QNode> tail = new AtomicReference<QNode>(null);
    ThreadLocal<QNode> myNode = ThreadLocal.withInitial(() -> new QNode());
    public void lock(){
        QNode qnode = myNode.get();
        QNode prev = tail.getAndSet(qnode);
        if (prev != null){
            qnode.wait = true;
            prev.next = qnode;
            while (qnode.wait){}
        }
    }

    public void unlock(){
        QNode qnode = myNode.get();
        if(qnode.next == null){
            if(tail.compareAndSet(qnode,null))
                return;
            while(qnode.next == null) {}
        }
        qnode.next.wait = false;
        qnode.next = null;

    }
}