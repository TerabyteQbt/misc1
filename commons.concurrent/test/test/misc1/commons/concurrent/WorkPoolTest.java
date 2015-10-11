package misc1.commons.concurrent;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public final class WorkPoolTest {

    @Test
    public void testBasic() {
        WorkPool workPool = WorkPool.defaultParallelism();
        workPool.join();
        workPool.submit(new EmptyRunnable());
        workPool.join();
    }

    @Test
    public void testExecution() {
        WorkPool workPool = WorkPool.defaultParallelism();

        AtomicInteger atomicInteger = new AtomicInteger(0);
        int incrementCount = 500;

        for(int i = 0; i < incrementCount; ++i) {
            workPool.submit(new IncrementAtomicIntegerRunnable(atomicInteger));
        }

        workPool.join();

        Assert.assertEquals("each runnable should increment our counter by one", incrementCount, atomicInteger.get());
    }

    @Test
    public void testShutdown() {
        WorkPool workPool = WorkPool.defaultParallelism();
        workPool.shutdown();
        try {
            workPool.submit(new EmptyRunnable());
        }
        catch(RejectedExecutionException _e) {
            // nice
            return;
        }

        Assert.fail("should refuse runnables after call to shutdown");
    }

    private final class EmptyRunnable implements Runnable {
        @Override
        public void run() { }
    }

    private final class IncrementAtomicIntegerRunnable implements Runnable {
        private final AtomicInteger atomicInteger;

        public IncrementAtomicIntegerRunnable(AtomicInteger atomicInteger) {
            this.atomicInteger = atomicInteger;
        }

        @Override
        public void run() {
            atomicInteger.incrementAndGet();
        }
    }
}
