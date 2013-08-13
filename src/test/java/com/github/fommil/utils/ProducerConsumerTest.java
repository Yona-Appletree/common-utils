package com.github.fommil.utils;

import com.google.common.base.Stopwatch;
import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.logging.Level.INFO;

@Log
public class ProducerConsumerTest {

  private int delay(int ratio, Random random) {
    if (ratio <= 0)
      return 0;

    int wait = random.nextInt(ratio);
    try {
      Thread.sleep(wait);
    } catch (InterruptedException e) {
    }
    return wait;
  }

  @Test
  public void testFastProducer() throws Exception {
    assertSame(testOneProducerOneConsumer(10000, new ProducerConsumer<String>(), 0, 10, 0, 0));
  }

  @Test
  public void testFastBufferedProducer() throws Exception {
    assertSame(testOneProducerOneConsumer(10000, new ProducerConsumer<String>(100), 0, 10, 0, 0));
  }

  @Test
  public void testFastConsumer() throws Exception {
    assertSame(testOneProducerOneConsumer(10000, new ProducerConsumer<String>(), 10, 0, 0, 0));
  }

  @Test
  public void testBabyBear() throws Exception {
    assertSame(testOneProducerOneConsumer(10000, new ProducerConsumer<String>(), 5, 5, 0, 0));
  }

  @Test
  public void testNoContention() throws Exception {
    assertSame(testOneProducerOneConsumer(Integer.MAX_VALUE, new ProducerConsumer<String>(), 0, 0, 0, 0));
  }

  // 21474836 per 10 seconds on my machine (2 million / second... not bad!)
  @Test
  public void testNoContentionBufferred() throws Exception {
    Stopwatch watch = new Stopwatch();
    watch.start();
    assertSame(testOneProducerOneConsumer(Integer.MAX_VALUE, new ProducerConsumer<String>(10000), 0, 0, 0, 0));
    watch.stop();
    log.log(INFO, "took {} to produce/consume loads of numbers", watch);
  }

  @Ignore("21474836 per second on my machine")
  @Test
  public void testComparisonToLoop() throws Exception {
    Stopwatch watch = new Stopwatch();
    watch.start();
    Random random = new Random();
    long report = Integer.MAX_VALUE / 100;
    for (long i = 0 ; i < Integer.MAX_VALUE ; i++) {
      Integer.toHexString(random.nextInt(100));
      if (i % report == 0)
        log.info("done " + i);
    }
    watch.stop();
    log.log(INFO, "took {} to for loop loads of numbers", watch);
  }

  @Test
  public void testStop() throws Exception {
    long[] out = testOneProducerOneConsumer(100000, new ProducerConsumer<String>(), 0, 1, 10000, 0);
    Assert.assertEquals(10000, out[2]);
    Assert.assertTrue(out[1] + " " + out[2], out[1] >= out[2] && out[1] < 20000);
  }

  @Test
  public void testClose() throws Exception {
    long[] out = testOneProducerOneConsumer(100000, new ProducerConsumer<String>(), 0, 1, 0, 10000);
    Assert.assertEquals(10000, out[1]);
    Assert.assertEquals(10000, out[2]);
  }

  public long[] testOneProducerOneConsumer(final long loops,
                                           final ProducerConsumer<String> pc,
                                           final int producerRatio,
                                           final int consumerRatio,
                                           final int stopAfter,
                                           final int closeAfter) throws Exception {
    Executor executor = Executors.newFixedThreadPool(2);

    final long report = loops / 100;

    final AtomicLong produced = new AtomicLong();
    final AtomicLong consumed = new AtomicLong();

    final ReentrantLock lock = new ReentrantLock();
    final Condition condition = lock.newCondition();

    Runnable producer = new Runnable() {
      private final Random random = new Random();

      @Override
      public void run() {
        do {
          int wait = delay(producerRatio, random);
          pc.produce(Integer.toHexString(wait));
          produced.incrementAndGet();
          if (closeAfter > 0 && produced.get() >= closeAfter)
            break;
        } while (produced.get() < loops && !pc.stopped());
        pc.close();
        log.info("Stopped Producing on " + produced.get());
      }
    };

    Runnable consumer = new Runnable() {
      private final Random random = new Random();

      @Override
      public void run() {
        while (pc.hasNext()) {
          String value = pc.next();
          Assert.assertNotNull(value);
          long got = consumed.incrementAndGet();
          if (stopAfter > 0 && got >= stopAfter) {
            pc.stop();
            break;
          }
          delay(consumerRatio, random);
          if (got % report == 0)
            log.info("Consumed " + got);
        }
        log.info("Stopped Consuming after " + consumed.get());
        lock.lock();
        try {
          condition.signalAll();
        } finally {
          lock.unlock();
        }
      }
    };

    executor.execute(producer);
    executor.execute(consumer);

    lock.lock();
    try {
      long timeout = loops * Math.max(1, Math.max(producerRatio, consumerRatio));
      condition.await(timeout, TimeUnit.MILLISECONDS);
    } finally {
      lock.unlock();
    }

    return new long[]{loops, produced.get(), consumed.get()};
  }

  private void assertSame(long... numbers) {
    Assert.assertTrue(numbers.length > 1);
    for (int i = 1; i < numbers.length; i++) {
      Assert.assertEquals(numbers[0], numbers[i]);
    }
  }

}