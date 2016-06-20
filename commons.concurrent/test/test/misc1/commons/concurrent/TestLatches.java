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
