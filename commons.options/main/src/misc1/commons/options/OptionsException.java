package misc1.commons.options;

public class OptionsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int priority;

    public OptionsException(String message) {
        this(0, message);
    }

    public OptionsException(int priority, String message) {
        super(message);

        this.priority = priority;
    }

    public OptionsException join(OptionsException other) {
        if(other.priority > priority) {
            return other;
        }
        return this;
    }
}
