package misc1.commons.options;

public class HelpRequestedException extends OptionsException {
    private static final long serialVersionUID = 1L;

    public HelpRequestedException() {
        super(1, "(help requested)");
    }
}
