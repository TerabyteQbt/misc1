package misc1.commons.concurrent;

import com.google.common.collect.Lists;
import java.util.Deque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import misc1.commons.ExceptionUtils;

public final class WorkPool {
    private final ExecutorService es;
    private final Object lock = new Object();
    private final Deque<Future<?>> pending = Lists.newLinkedList();

    public static WorkPool defaultParallelism() {
        return explicitParallelism(Runtime.getRuntime().availableProcessors());
    }

    public static WorkPool explicitParallelism(int parallelism) {
        return new WorkPool(Executors.newFixedThreadPool(parallelism));
    }

    public static WorkPool infiniteParallelism() {
        return new WorkPool(Executors.newCachedThreadPool());
    }

    private WorkPool(ExecutorService es) {
        this.es = es;
    }

    public void submit(Runnable r) {
        Future<?> f = es.submit(r);
        synchronized(lock) {
            pending.addLast(f);
        }
    }

    public void join() {
        while(true) {
            Future<?> f;
            synchronized(lock) {
                if(pending.isEmpty()) {
                    break;
                }
                f = pending.removeFirst();
            }
            try {
                f.get();
            }
            catch(InterruptedException e) {
                throw ExceptionUtils.commute(e);
            }
            catch(ExecutionException e) {
                throw ExceptionUtils.commute(e.getCause());
            }
        }
    }

    public void shutdown() {
        es.shutdown();
    }

    private final Executor asExecutor = new Executor() {
        @Override
        public void execute(Runnable command) {
            submit(command);
        }
    };
    public Executor asExecutor() {
        return asExecutor;
    }
}
