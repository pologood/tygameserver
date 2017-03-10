package com.netease.pangu.game.test;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // start thread backed receiver.
        // Lighweight fibers can also be created using a thread pool
        Fiber receiver = new ThreadFiber();
        receiver.start();

        // create java.util.concurrent.CountDownLatch to notify when message arrives
        final CountDownLatch latch = new CountDownLatch(1);

        // create channel to message between threads
        Channel<String> channel = new MemoryChannel<String>();

        Callback<String> onMsg = new Callback<String>() {
            public void onMessage(String message) {
                //open latch
                System.out.println("receive: " + message);
                latch.countDown();
            }
        };
        //add subscription for message on receiver thread
        channel.subscribe(receiver, onMsg);

        //publish message to receive thread. the publish method is thread safe.
        for (int i = 0; i < 10; i++)
            channel.publish("Hello" + i);

        //wait for receiving thread to receive message
        latch.await(10, TimeUnit.SECONDS);

        //shutdown thread
        receiver.dispose();
    }
}
