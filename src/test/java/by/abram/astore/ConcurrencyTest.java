package by.abram.astore;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyTest {

    private int unsafeCounter = 0;
    private final AtomicInteger safeCounter = new AtomicInteger(0);
    private int synchronizedCounter = 0;

    private final Object lock = new Object();

    @Test
    void demonstrateRaceConditionAndSolution() throws InterruptedException {
        boolean lostUpdateObserved = false;

        for (int attempt = 0; attempt < 5 && !lostUpdateObserved; attempt++) {
            unsafeCounter = 0;
            safeCounter.set(0);
            synchronizedCounter = 0;

            int numberOfThreads = 50;
            int incrementsPerThread = 5000;

            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(() -> {
                    readyLatch.countDown();

                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    for (int j = 0; j < incrementsPerThread; j++) {
                        int current = unsafeCounter;
                        if ((current & 15) == 0) {
                            Thread.yield();
                        }
                        unsafeCounter = current + 1;

                        synchronized (lock) {
                            synchronizedCounter++;
                        }

                        safeCounter.incrementAndGet();
                    }

                    doneLatch.countDown();
                });
            }

            readyLatch.await();
            startLatch.countDown();
            doneLatch.await();
            executorService.shutdown();

            int expectedTotal = numberOfThreads * incrementsPerThread;

            if (unsafeCounter != expectedTotal) {
                lostUpdateObserved = true;
            }

            assertEquals(expectedTotal, safeCounter.get());
            assertEquals(expectedTotal, synchronizedCounter);
        }

        assertTrue(lostUpdateObserved);
    }
}
