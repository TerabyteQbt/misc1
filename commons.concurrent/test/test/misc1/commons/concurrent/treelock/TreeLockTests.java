package misc1.commons.concurrent.treelock;

import java.util.concurrent.atomic.AtomicBoolean;
import misc1.commons.ExceptionUtils;
import misc1.commons.concurrent.TestLatches;
import misc1.commons.concurrent.TestThreads;
import org.junit.Assert;
import org.junit.Test;

public class TreeLockTests {
    private static void negativeSleep() {
        try {
            Thread.sleep(500);
        }
        catch(InterruptedException e) {
            throw ExceptionUtils.commute(e);
        }
    }

    @Test
    public void testBlocks() throws InterruptedException {
        testBlock("x", "");
        testBlock("x", "x");
        testBlock("x", "x/y");
    }

    @Test
    public void testNoBlocks() throws InterruptedException {
        testNoBlock("x", "y");
        testNoBlock("x/y", "y");
        testNoBlock("x/y", "x/z");
        testNoBlock("x/y", "x/z/w");
    }

    private void testBlock(String p1, String p2) throws InterruptedException {
        testBlockOneWay(p1, p2);
        testBlockOneWay(p2, p1);
    }

    private void testBlockOneWay(final String p1, final String p2) throws InterruptedException {
        final ArrayTreeLock<String> l = new ArrayTreeLock<String>();
        TestThreads tt = new TestThreads();
        final TestLatches tl = new TestLatches();
        final AtomicBoolean p2Locked = new AtomicBoolean(false);
        tt.start(new Runnable() {
            @Override
            public void run() {
                l.lock(ArrayTreeLockPath.split(p1, '/'));
                tl.signal("p1 has locked");
                negativeSleep();
                Assert.assertFalse("[" + p2 + "] got lock despite [" + p1 + "] locked", p2Locked.get());
                l.unlock(ArrayTreeLockPath.split(p1, '/'));
            }
        });
        tt.start(new Runnable() {
            @Override
            public void run() {
                tl.awaitCommute("p1 has locked");
                l.lock(ArrayTreeLockPath.split(p2, '/'));
                p2Locked.set(true);
                l.unlock(ArrayTreeLockPath.split(p2, '/'));
            }
        });
        tt.join();
    }

    private void testNoBlock(String p1, String p2) throws InterruptedException {
        testNoBlockOneWay(p1, p2);
        testNoBlockOneWay(p2, p1);
    }

    private void testNoBlockOneWay(final String p1, final String p2) throws InterruptedException {
        final ArrayTreeLock<String> l = new ArrayTreeLock<String>();
        TestThreads tt = new TestThreads();
        final TestLatches tl = new TestLatches();
        tt.start(new Runnable() {
            @Override
            public void run() {
                l.lock(ArrayTreeLockPath.split(p1, '/'));
                tl.signal("p1 has locked");
                tl.awaitCommute("p2 has locked");
                tl.signal("p1 knows p2 has locked");
                l.unlock(ArrayTreeLockPath.split(p1, '/'));
            }
        });
        tt.start(new Runnable() {
            @Override
            public void run() {
                tl.awaitCommute("p1 has locked");
                l.lock(ArrayTreeLockPath.split(p2, '/'));
                tl.signal("p2 has locked");
                tl.awaitCommute("p1 knows p2 has locked");
                l.unlock(ArrayTreeLockPath.split(p2, '/'));
            }
        });
        tt.join();
    }
}
