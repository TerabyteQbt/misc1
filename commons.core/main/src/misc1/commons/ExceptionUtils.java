package misc1.commons;

public final class ExceptionUtils {
    private ExceptionUtils() {
        // no
    }

    public static RuntimeException commute(Throwable t) {
        if(t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        return new RuntimeException(t);
    }
}
