package misc1.commons.concurrent;

import misc1.commons.Maybe;
import misc1.commons.NiceCallable;

public final class ConcurrencyUtils {
    private ConcurrencyUtils() {
        // no
    }

    public static <T> Maybe<T> poll(NiceCallable<Maybe<T>> attempt, Object monitor, long limitMs) throws InterruptedException {
        long t0 = System.currentTimeMillis();

        synchronized(monitor) {
            while(true) {
                Maybe<T> result = attempt.call();
                if(result.isPresent()) {
                    return result;
                }
                long now = System.currentTimeMillis();
                if(limitMs < 0) {
                    monitor.wait();
                }
                else {
                    long left = t0 + limitMs - now;
                    if(left <= 0) {
                        return Maybe.not();
                    }
                    monitor.wait(left);
                }
            }
        }
    }

    public static <T> T pollForever(NiceCallable<Maybe<T>> attempt, Object monitor) throws InterruptedException {
        Maybe<T> r = poll(attempt, monitor, -1);
        if(r.isPresent()) {
            return r.get(null);
        }
        throw new IllegalStateException();
    }
}
