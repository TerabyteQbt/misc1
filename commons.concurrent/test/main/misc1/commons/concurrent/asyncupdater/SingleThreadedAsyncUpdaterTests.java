package misc1.commons.concurrent.asyncupdater;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import misc1.commons.concurrent.TestLatches;
import org.junit.Assert;
import org.junit.Test;

public class SingleThreadedAsyncUpdaterTests {
    @Test
    public void testSimple() throws InterruptedException {
        SingleThreadedAsyncUpdater<String, BaseUpdate> au = new SingleThreadedAsyncUpdater<String, BaseUpdate>("testSimple");

        TestLatches latches = new TestLatches();
        List<String> failTracker = new LinkedList<String>();

        AtomicInteger a = new AtomicInteger(0);
        AtomicInteger aCount = new AtomicInteger(0);
        AtomicInteger b = new AtomicInteger(0);
        AtomicInteger bCount = new AtomicInteger(0);
        AtomicInteger c = new AtomicInteger(0);
        AtomicInteger cCount = new AtomicInteger(0);

        au.enqueue(new IntegerAddUpdate(latches, "1", "2", "a", a, aCount, 1, failTracker));
        latches.await("2");
        au.enqueue(new IntegerAddUpdate(latches, null, null, "b", b, bCount, 1, failTracker));
        au.enqueue(new IntegerAddUpdate(latches, null, null, "c", c, cCount, 1, failTracker));
        au.enqueue(new IntegerAddUpdate(latches, null, null, "c", c, cCount, 2, failTracker));

        Assert.assertEquals("b", 0, b.get());
        Assert.assertEquals("bCount", 0, bCount.get());
        Assert.assertEquals("c", 0, c.get());
        Assert.assertEquals("cCount", 0, cCount.get());
        Assert.assertEquals("failTracker: " + failTracker, 0, failTracker.size());

        latches.signal("1");
        Assert.assertTrue("forceDispatch", au.test_only_awaitEmpty());

        Assert.assertEquals("b", 1, b.get());
        Assert.assertEquals("bCount", 1, bCount.get());
        Assert.assertEquals("c", 3, c.get());
        Assert.assertEquals("cCount", 1, cCount.get());
        Assert.assertEquals("failTracker: " + failTracker, 0, failTracker.size());

        au.enqueue(new IntegerAddUpdate(latches, "3", "4", "c", c, cCount, 3, failTracker));
        latches.await("4");
        au.enqueue(new IntegerAddUpdate(latches, null, null, "c", c, cCount, 4, failTracker));

        latches.signal("3");
        Assert.assertTrue("forceDispatch", au.test_only_awaitEmpty());

        Assert.assertEquals("c", 10, c.get());
        Assert.assertEquals("cCount", 3, cCount.get());
        Assert.assertEquals("failTracker: " + failTracker, 0, failTracker.size());
    }

    @Test
    public void testException() throws InterruptedException {
        SingleThreadedAsyncUpdater<String, BaseUpdate> au = new SingleThreadedAsyncUpdater<String, BaseUpdate>("testException");

        TestLatches latches = new TestLatches();
        List<String> failTracker = new LinkedList<String>();

        AtomicInteger a = new AtomicInteger(0);
        AtomicInteger aCount = new AtomicInteger(0);

        au.enqueue(new BailUpdate(latches, "1", "2", failTracker));
        latches.await("2");
        au.enqueue(new IntegerAddUpdate(latches, null, null, "a", a, aCount, 1, failTracker));

        Assert.assertEquals("a", 0, a.get());
        Assert.assertEquals("aCount", 0, aCount.get());
        Assert.assertEquals("failTracker: " + failTracker, 0, failTracker.size());

        latches.signal("1");
        Assert.assertTrue("forceDispatch", au.test_only_awaitEmpty());

        Assert.assertEquals("a", 1, a.get());
        Assert.assertEquals("aCount", 1, aCount.get());
        Assert.assertEquals("failTracker: " + failTracker, 0, failTracker.size());
    }

    private abstract static class BaseUpdate implements KeyedAsyncUpdate<String, BaseUpdate> {
    }

    private static class IntegerAddUpdate extends BaseUpdate {
        private final TestLatches latches;
        private final String block;
        private final String notify;
        private final String targetName;
        private final AtomicInteger target;
        private final AtomicInteger targetCount;
        private int delta;
        private final List<String> failTracker;

        public IntegerAddUpdate(TestLatches latches, String block, String notify, String targetName, AtomicInteger target, AtomicInteger targetCount, int delta, List<String> failTracker) {
            this.latches = latches;
            this.block = block;
            this.notify = notify;
            this.targetName = targetName;
            this.target = target;
            this.targetCount = targetCount;
            this.delta = delta;
            this.failTracker = failTracker;
        }

        @Override
        public synchronized void fire() {
            if(notify != null) {
                latches.signal(notify);
            }
            if(block != null) {
                try {
                    latches.await(block);
                }
                catch(InterruptedException e) {
                    synchronized(failTracker) {
                        failTracker.add("Interrupted");
                        return;
                    }
                }
            }
            target.addAndGet(delta);
            targetCount.addAndGet(1);
        }

        @Override
        public String getKey() {
            return getClass() + ":" + targetName;
        }

        @Override
        public synchronized BaseUpdate merge(BaseUpdate _other) {
            IntegerAddUpdate other = (IntegerAddUpdate) _other;
            if(!targetName.equals(other.targetName)) {
                synchronized(failTracker) {
                    failTracker.add("Merge of mismatched " + targetName + "/" + other.targetName);
                }
            }
            if(other.block != null) {
                synchronized(failTracker) {
                    failTracker.add("Merge with second blocked");
                }
            }
            if(other.notify != null) {
                synchronized(failTracker) {
                    failTracker.add("Merge with second blocked");
                }
            }
            return new IntegerAddUpdate(latches, block, notify, targetName, target, targetCount, delta + other.delta, failTracker);
        }
    }

    private static class BailUpdate extends BaseUpdate {
        private final TestLatches latches;
        private final String block;
        private final String notify;
        private final List<String> failTracker;

        public BailUpdate(TestLatches latches, String block, String notify, List<String> failTracker) {
            this.latches = latches;
            this.block = block;
            this.notify = notify;
            this.failTracker = failTracker;
        }

        @Override
        public synchronized void fire() {
            if(notify != null) {
                latches.signal(notify);
            }
            if(block != null) {
                try {
                    latches.await(block);
                }
                catch(InterruptedException e) {
                    synchronized(failTracker) {
                        failTracker.add("Interrupted");
                        return;
                    }
                }
            }
            throw new RuntimeException("Test exception, please ignore");
        }

        @Override
        public String getKey() {
            return String.valueOf(getClass());
        }

        @Override
        public synchronized BaseUpdate merge(BaseUpdate _other) {
            synchronized(failTracker) {
                failTracker.add("BailUpdate merged?!");
            }
            return this;
        }
    }
}
