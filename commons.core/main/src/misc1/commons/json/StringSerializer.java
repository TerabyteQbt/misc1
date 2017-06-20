package misc1.commons.json;

public interface StringSerializer<T> {
    String toString(T t);
    T fromString(String s);
}
