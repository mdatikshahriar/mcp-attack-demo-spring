package com.example.server.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UpdateSignalService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateSignalService.class);

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicBoolean signalSent = new AtomicBoolean(false);
    private final AtomicLong signalCount = new AtomicLong(0);
    @Getter
    private volatile LocalDateTime lastSignalTime;

    public UpdateSignalService() {
        logger.info("UpdateSignalService initialized");
        logger.debug("Initial latch count: {}", latch.getCount());
    }

    public void signalUpdate() {
        logger.info("Sending update signal to MCP server");

        try {
            long currentCount = signalCount.incrementAndGet();
            lastSignalTime = LocalDateTime.now();

            if (signalSent.compareAndSet(false, true)) {
                latch.countDown();
                logger.info("Update signal sent successfully - Signal #{} at {}", currentCount,
                        lastSignalTime);
                logger.debug("Latch count after signal: {}", latch.getCount());
            } else {
                logger.warn(
                        "Additional update signal received (Signal #{}) - " + "latch already triggered at {}",
                        currentCount, lastSignalTime);
            }

        } catch (Exception e) {
            logger.error("Error sending update signal", e);
            throw e;
        }
    }

    public void awaitUpdate() throws InterruptedException {
        logger.info("Waiting for update signal...");
        logger.debug("Current latch count: {}", latch.getCount());

        try {
            latch.await();
            logger.info("Update signal received and processed successfully");
            logger.debug("Total signals received: {}, Last signal at: {}", signalCount.get(),
                    lastSignalTime);

        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for update signal", e);
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    public boolean awaitUpdate(long timeout, TimeUnit unit) throws InterruptedException {
        logger.info("Waiting for update signal with timeout: {} {}", timeout, unit);
        logger.debug("Current latch count: {}", latch.getCount());

        try {
            boolean received = latch.await(timeout, unit);

            if (received) {
                logger.info("Update signal received within timeout period");
                logger.debug("Total signals received: {}, Last signal at: {}", signalCount.get(),
                        lastSignalTime);
            } else {
                logger.warn("Timeout waiting for update signal after {} {}", timeout, unit);
            }

            return received;

        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for update signal with timeout", e);
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    // Status methods for monitoring
    public boolean isSignalSent() {
        return signalSent.get();
    }

    public long getSignalCount() {
        return signalCount.get();
    }

    public long getRemainingLatchCount() {
        return latch.getCount();
    }
}
