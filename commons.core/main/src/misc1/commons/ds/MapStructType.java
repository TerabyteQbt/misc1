package misc1.commons.ds;

import com.google.common.collect.ImmutableMap;

public abstract class MapStructType<S, B, K, VS, VB> {
    protected abstract S create(ImmutableMap<K, VS> map);
    protected abstract B createBuilder(ImmutableSalvagingMap<K, VB> map);
    protected abstract VS toStruct(VB vb);
    protected abstract VB toBuilder(VS vs);

    public B builder() {
        return createBuilder(ImmutableSalvagingMap.<K, VB>of());
    }
}
