package misc1.commons.concurrent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.CountDownLatch;
import misc1.commons.ExceptionUtils;

public class TestLatches {
    private final LoadingCache<String, CountDownLatch> latches = CacheBuilder.newBuilder().build(new CacheLoader<String, CountDownLatch>() {
        @Override
        public CountDownLatch load(String key) throws Exception {
            return new CountDownLatch(1);
        }
    });

    public void await(String name) throws InterruptedException {
        latches.getUnchecked(name).await();
    }

    public void awaitCommute(String name) {
        try {
            await(name);
        }
        catch(InterruptedException e) {
            throw ExceptionUtils.commute(e);
        }
    }

    public void signal(String name) {
        latches.getUnchecked(name).countDown();
    }
}
