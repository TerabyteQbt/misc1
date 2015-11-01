package misc1.commons.resources;

import com.google.common.base.Function;

public interface VoidResource extends Resource<VoidResource> {
    public static final ResourceType<RawResource, VoidResource> TYPE = new ResourceType<RawResource, VoidResource>() {
        public VoidResource wrap(RawResource raw, final Function<FreeScope, VoidResource> onCopyInto) {
            return new VoidResource() {
                @Override
                public VoidResource copyInto(FreeScope scope) {
                    return onCopyInto.apply(scope);
                }
            };
        }
    };
}
