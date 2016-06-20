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
