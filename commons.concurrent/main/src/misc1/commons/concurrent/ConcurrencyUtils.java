//   Copyright 2016 Keith Amling
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
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
