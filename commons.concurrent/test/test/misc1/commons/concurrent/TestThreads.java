package misc1.commons.concurrent;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.List;

public class TestThreads {
    private final Object lock = new Object();
    private List<Throwable> fails = Lists.newLinkedList();
    private int outstanding = 0;

    public void start(final Runnable r) {
        synchronized(lock) {
            outstanding++;
        }
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    r.run();
                }
                catch(Throwable e) {
                    synchronized(lock) {
                        fails.add(e);
                        lock.notifyAll();
                    }
                }
                finally {
                    synchronized(lock) {
                        outstanding--;
                        lock.notifyAll();
                    }
                }
            }
        };
        t.start();
    }

    public void join() throws InterruptedException {
        synchronized(lock) {
            while(true) {
                if(!fails.isEmpty()) {
                    if(fails.size() > 1) {
                        for(Throwable fail : fails) {
                            fail.printStackTrace(System.err);
                        }
                    }
                    throw Throwables.propagate(fails.get(0));
                }
                if(outstanding == 0) {
                    return;
                }
                lock.wait();
            }
        }
    }
}
