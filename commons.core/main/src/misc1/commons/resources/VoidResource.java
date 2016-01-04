package misc1.commons.resources;

public interface VoidResource extends Resource<VoidResource> {
    public static final ResourceType<RawResource, VoidResource> TYPE = (raw, onCopyInto) -> onCopyInto::apply;
}
