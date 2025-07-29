package com.example.server.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
public class UpdateSignalService {

    private final CountDownLatch latch = new CountDownLatch(1);

    public void signalUpdate() {
        latch.countDown();
    }

    public void awaitUpdate() throws InterruptedException {
        latch.await();
    }
}
