package misc1.commons.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WorkPool implements Executor, AutoCloseable {
    private final ExecutorService es;

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

    @Override
    public void execute(Runnable r) {
        es.execute(r);
    }

    @Override
    public void close() {
        es.shutdown();
    }
}
