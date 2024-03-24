package utc.k61.cntt2.class_management.exception;

public class SomethingNotFoundException extends RuntimeException {
    public SomethingNotFoundException(String message) {
        super(message);
    }

    public SomethingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
