package misc1.commons.concurrent.ctree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import misc1.commons.ExceptionUtils;
import misc1.commons.Result;

public abstract class ComputationTreeComputer {
    private enum StatusStatus {
        UNSTARTED,
        STARTED,
        DONE;
    }
    private class Status {
        private final ComputationTree<?> tree;
        private final List<Status> children;

        public Status(ComputationTree<?> tree, List<Status> children) {
            this.tree = tree;
            this.children = children;
        }

        private StatusStatus status = StatusStatus.UNSTARTED;
        private final Set<Status> outwards = Sets.newHashSet();
        private Result<Object> result;

        public synchronized void checkStart() {
            if(status != StatusStatus.UNSTARTED) {
                return;
            }

            ImmutableList.Builder<Result<Object>> childrenResultsBuilder = ImmutableList.builder();
            for(Status child : children) {
                synchronized(child) {
                    switch(child.status) {
                        case DONE:
                            childrenResultsBuilder.add(child.result);
                            break;

                        default:
                            return;
                    }
                }
            }
            final ImmutableList<Result<Object>> childrenResults = childrenResultsBuilder.build();
            submit(new Runnable() {
                @Override
                public void run() {
                    complete(Result.newFromCallable(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            ImmutableList.Builder<Object> childrenBuilder = ImmutableList.builder();
                            for(Result<Object> childrenResult : childrenResults) {
                                childrenBuilder.add(childrenResult.getCommute());
                            }
                            return tree.postProcess.apply(childrenBuilder.build());
                        }
                    }));
                }
            });
            status = StatusStatus.STARTED;
        }

        private synchronized void complete(Result<Object> newResult) {
            if(status != StatusStatus.STARTED) {
                throw new IllegalStateException();
            }

            status = StatusStatus.DONE;
            result = newResult;
            notifyAll();

            for(final Status outward : outwards) {
                submitCheck(outward);
            }
        }

        public synchronized Result<Object> await() throws InterruptedException {
            while(status != StatusStatus.DONE) {
                wait();
            }
            return result;
        }
    }

    private final Object lock = new Object();
    private final Map<ComputationTree<?>, Status> statuses = Maps.newIdentityHashMap();

    private void submitCheck(final Status status) {
        submit(new Runnable() {
            @Override
            public void run() {
                status.checkStart();
            }
        });
    }

    private Status vivify(ComputationTree<?> tree) {
        synchronized(lock) {
            return vivifyHelper(tree);
        }
    }

    private Status vivifyHelper(ComputationTree<?> tree) {
        Status ret = statuses.get(tree);
        if(ret != null) {
            return ret;
        }
        ImmutableList.Builder<Status> childrenBuilder = ImmutableList.builder();
        for(ComputationTree<?> childTree : tree.children) {
            childrenBuilder.add(vivifyHelper(childTree));
        }
        ImmutableList<Status> children = childrenBuilder.build();
        ret = new Status(tree, children);
        for(Status child : children) {
            synchronized(child) {
                child.outwards.add(ret);
            }
        }
        submitCheck(ret);
        statuses.put(tree, ret);
        return ret;
    }

    public void start(ComputationTree<?> tree) {
        vivify(tree);
    }

    @SuppressWarnings("unchecked")
    private static <V> Result<V> castResult(Result<Object> result) {
        return (Result<V>)result;
    }

    public <V> Result<V> await(ComputationTree<V> tree) {
        try {
            return castResult(vivify(tree).await());
        }
        catch(InterruptedException e) {
            throw ExceptionUtils.commute(e);
        }
    }

    protected abstract void submit(Runnable r);
}
