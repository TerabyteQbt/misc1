package misc1.commons.resources;

public interface Resource<R extends Resource<R>> {
    public R copyInto(FreeScope scope);
}
