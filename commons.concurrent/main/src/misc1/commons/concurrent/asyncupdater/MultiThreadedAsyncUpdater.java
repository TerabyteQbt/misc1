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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MultiThreadedAsyncUpdater<K, A extends KeyedAsyncUpdate<K, A>> implements KeyedAsyncUpdater<K, A> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiThreadedAsyncUpdater.class);

    private final Object lock = new Object() { /* anonymous for more useful class name */ };
    private final Map<K, A> currentlyFiring = Maps.newHashMap();
    private final Map<K, A> blockedMap = Maps.newHashMap();

    private final Executor executor;

    // package protected for AsyncUpdaters only
    MultiThreadedAsyncUpdater(String name) {
        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("async-update-" + name + "-%d").setDaemon(true).build());
    }

    // package protected for AsyncUpdaters only
    MultiThreadedAsyncUpdater(Executor executor) {
        this.executor = executor;
    }

    private class FireRunnable implements Runnable {
        private final K key;
        private final A event;

        public FireRunnable(K key, A event) {
            this.key = key;
            this.event = event;
        }

        @Override
        public void run() {
            try {
                event.fire();
            }
            catch(RuntimeException e) {
                LOGGER.error("Async update " + event + " threw", e);
            }
            synchronized(lock) {
                if(blockedMap.containsKey(key)) {
                    A nextEvent = blockedMap.remove(key);
                    executor.execute(new FireRunnable(key, nextEvent));
                    currentlyFiring.put(key, nextEvent);
                }
                else {
                    currentlyFiring.remove(key);
                }
            }
        }
    }

    @Override
    public void enqueue(final A event) {
        final K key = event.getKey();
        synchronized(lock) {
            if(!currentlyFiring.containsKey(key)) {
                currentlyFiring.put(key, event);
                executor.execute(new FireRunnable(key, event));
            }
            else {
                if(blockedMap.containsKey(key)) {
                    A current = blockedMap.get(key);
                    A merged = current.merge(event);
                    if(merged != current) {
                        blockedMap.put(key, merged);
                    }
                }
                else {
                    blockedMap.put(key, event);
                }
            }
        }
    }

    @Override
    public String toString() {
        synchronized(lock) {
            return Objects.toStringHelper(this)
                .add("currentlyFiring", currentlyFiring)
                .add("blockedMap", blockedMap)
                .toString();
        }
    }

    @Override
    public ImmutableList<A> getAllInFlightEvents() {
        synchronized(lock) {
            return ImmutableList.<A> builder()
                .addAll(currentlyFiring.values())
                .addAll(blockedMap.values())
                .build();
        }
    }
}
