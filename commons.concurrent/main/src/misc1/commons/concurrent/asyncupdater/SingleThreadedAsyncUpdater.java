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
package misc1.commons.concurrent.asyncupdater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.annotation.CheckForNull;
import misc1.commons.Maybe;
import misc1.commons.concurrent.ConcurrencyUtils;
import misc1.commons.concurrent.misc.PollingServiceThread;

/**
 * An engine to take {@link KeyedAsyncUpdate} instances and execute them as
 * fast as possible on one thread and squash them where necessary.
 */
final class SingleThreadedAsyncUpdater<K, A extends KeyedAsyncUpdate<K, A>> implements KeyedAsyncUpdater<K, A> {
    private final Object lock = new Object() { /* anonymous for more useful class name */ };
    private final Queue<K> keys = new LinkedList<K>();
    private final Map<K, A> eventMap = Maps.newHashMap();
    private boolean threadWaiting = false;

    // package protected for AsyncUpdaters only
    SingleThreadedAsyncUpdater(String name) {
        PollingServiceThread<A> t = new PollingServiceThread<A>("async-updater-" + name) {
            @Override
            protected A poll() throws InterruptedException {
                synchronized(lock) {
                    K key;
                    while(true) {
                        if(!keys.isEmpty()) {
                            key = keys.remove();
                            break;
                        }
                        threadWaiting = true;
                        lock.notifyAll();
                        try {
                            lock.wait();
                        }
                        finally {
                            threadWaiting = false;
                        }
                    }
                    return eventMap.remove(key);
                }
            }

            @Override
            protected void fire(A event) {
                currentlyFiring = event;
                try {
                    event.fire();
                }
                finally {
                    currentlyFiring = null;
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void enqueue(A event) {
        K key = event.getKey();
        synchronized(lock) {
            if(eventMap.containsKey(key)) {
                A current = eventMap.get(key);
                A merged = current.merge(event);
                if(merged != current) {
                    eventMap.put(key, merged);
                }
            }
            else {
                eventMap.put(key, event);
                keys.add(key);
            }

            lock.notifyAll();
        }
    }

    @CheckForNull
    private volatile A currentlyFiring = null;

    @Override
    public ImmutableList<A> getAllInFlightEvents() {
        A current = currentlyFiring;
        return current != null ? ImmutableList.of(current) : ImmutableList.<A>of();
    }

    public boolean test_only_awaitEmpty() throws InterruptedException {
        return ConcurrencyUtils.poll(() -> {
            if(keys.isEmpty() && threadWaiting) {
                return Maybe.of(null);
            }
            return Maybe.not();
        }, lock, 10000).isPresent();
    }
}
