package misc1.commons.resources;

import com.google.common.base.Function;

public interface ResourceType<RR extends RawResource, R extends Resource<R>> {
    public R wrap(RR raw, Function<FreeScope, R> onCopyInto);
}
