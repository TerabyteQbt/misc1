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
package misc1.commons.concurrent.misc;

import java.util.concurrent.TimeUnit;
import misc1.commons.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PollingServiceThread<EVENT> extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollingServiceThread.class);

    public PollingServiceThread(String name) {
        super(name);
    }

    /**
     * "Poll" (block) for the next event to act on.  Note that if the service
     * thread is being shut down this will be interrupted and should throw
     * InterruptedException.
     */
    protected abstract EVENT poll() throws InterruptedException;

    /**
     * Actually fire the event once poll() has returned one.  This isn't
     * forbidden to do blocking things but poll() should be doing any sort of
     * waiting for the next event to act on.
     */
    protected abstract void fire(EVENT event);

    private final Object stateLock = new Object() {};
    private boolean currentlyPolling = false;
    private boolean cancelled = false;

    private volatile RuntimeException lastFailure = null;

    @Override
    public void run() {
        while(true) {
            synchronized(stateLock) {
                if(cancelled) {
                    return;
                }
                currentlyPolling = true;
            }
            EVENT event;
            try {
                event = poll();
            }
            catch(Exception e) {
                if(e instanceof InterruptedException) {
                    synchronized(stateLock) {
                        currentlyPolling = false;
                        if(cancelled) {
                            return;
                        }
                    }
                }
                LOGGER.error("Service thread poll() threw unexpectedly?", e);
                throw lastFailure = ExceptionUtils.commute(e);
            }
            synchronized(stateLock) {
                currentlyPolling = false;
                if(cancelled) {
                    return;
                }
            }
            try {
                fire(event);
            }
            catch(RuntimeException e) {
                LOGGER.error("Service thread fire() threw for " + event, e);
                // ignore
                lastFailure = e;
            }
        }
    }

    public void destroyServiceThread() {
        synchronized(stateLock) {
            cancelled = true;
            if(currentlyPolling) {
                interrupt();
            }
        }
    }

    public void joinServiceThread(long timeout, TimeUnit unit) throws InterruptedException {
        destroyServiceThread();
        join(unit.toMillis(timeout));
        RuntimeException failure = lastFailure;
        if(failure != null) {
            throw failure;
        }
    }
}
