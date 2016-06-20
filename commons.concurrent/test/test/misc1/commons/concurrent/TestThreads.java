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
