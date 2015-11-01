package misc1.commons.resources;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;

public final class FreeScope implements AutoCloseable {
    private static class Instance<RR extends RawResource, R extends Resource<R>> {
        public final ResourceType<RR, R> type;
        private final RR raw;

        private int refCt = 0;
        private boolean closed = false;

        public Instance(ResourceType<RR, R> type, RR raw) {
            this.type = type;
            this.raw = raw;
        }

        private final Function<FreeScope, R> onCopyInto = new Function<FreeScope, R>() {
            @Override
            public R apply(FreeScope scope) {
                scope.register(Instance.this);
                return type.wrap(raw, onCopyInto);
            }
        };

        private synchronized void ref() {
            if(closed) {
                throw new IllegalStateException();
            }
            ++refCt;
        }

        private synchronized void free() {
            if(closed) {
                throw new IllegalStateException();
            }
            --refCt;
            if(refCt == 0) {
                closed = true;
                raw.free();
            }
        }
    }

    public <RR extends RawResource, R extends Resource<R>> R initial(ResourceType<RR, R> type, RR raw) {
        return new Instance<RR, R>(type, raw).onCopyInto.apply(this);
    }

    private List<Instance<?, ?>> is = Lists.newLinkedList();
    private boolean closed = false;

    private synchronized void register(Instance<?, ?> i) {
        if(closed) {
            throw new IllegalStateException();
        }
        i.ref();
        is.add(i);
    }

    @Override
    public synchronized void close() {
        if(closed) {
            // Sigh, I guess...
            return;
        }
        closed = true;
        for(Instance<?, ?> i : is) {
            i.free();
        }
    }
}
