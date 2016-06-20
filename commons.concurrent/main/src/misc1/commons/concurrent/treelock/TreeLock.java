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
package misc1.commons.concurrent.treelock;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.Semaphore;
import misc1.commons.ExceptionUtils;

public abstract class TreeLock<K, PR> implements TreeLockInterface<TreeLockPath<K, PR>> {
    private static final class NodeLock {
        private static final int MAX = Integer.MAX_VALUE;

        private final Semaphore semaphore = new Semaphore(MAX, true);

        public void sharedLock() {
            try {
                semaphore.acquire(1);
            }
            catch(InterruptedException e) {
                throw ExceptionUtils.commute(e);
            }
        }

        public void sharedUnlock() {
            semaphore.release(1);
        }

        public void exclusiveLock() {
            try {
                semaphore.acquire(MAX);
            }
            catch(InterruptedException e) {
                throw ExceptionUtils.commute(e);
            }
        }

        public void exclusiveUnlock() {
            semaphore.release(MAX);
        }
    }

    private final NodeLock lock = new NodeLock();
    private final Object childrenLock = new Object();
    private final Map<K, TreeLockInterface<? super PR>> children = Maps.newHashMap();

    private TreeLockInterface<? super PR> getChild(K next) {
        synchronized(childrenLock) {
            TreeLockInterface<? super PR> child = children.get(next);
            if(child == null) {
                children.put(next, child = newChild());
            }
            return child;
        }
    }

    @Override
    public void lock(TreeLockPath<K, PR> path) {
        if(path == null) {
            lock.exclusiveLock();
            return;
        }
        K next = path.car();
        lock.sharedLock();
        boolean unlock = true;
        try {
            getChild(next).lock(path.cdr());
            unlock = false;
        }
        finally {
            if(unlock) {
                lock.sharedUnlock();
            }
        }
    }

    @Override
    public void unlock(TreeLockPath<K, PR> path) {
        if(path == null) {
            lock.exclusiveUnlock();
            return;
        }
        K next = path.car();
        lock.sharedUnlock();
        getChild(next).unlock(path.cdr());
    }

    abstract protected TreeLockInterface<? super PR> newChild();
}
