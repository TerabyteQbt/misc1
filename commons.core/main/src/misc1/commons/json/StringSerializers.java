package misc1.commons.json;

public final class StringSerializers {
    private StringSerializers() {
        // no
    }

    public static final StringSerializer<String> STRING = new StringSerializer<String>() {
        @Override
        public String toString(String t) {
            return t;
        }

        @Override
        public String fromString(String s) {
            return s;
        }
    };

    public static final StringSerializer<Boolean> BOOLEAN = new StringSerializer<Boolean>() {
        @Override
        public String toString(Boolean t) {
            return t ? "true" : "false";
        }

        @Override
        public Boolean fromString(String s) {
            if(s.equals("true")) {
                return true;
            }
            if(s.equals("false")) {
                return false;
            }
            throw new IllegalArgumentException("Illegal value for boolean: " + s);
        }
    };
}
