package dev.timkloepper.rendering.mesh.exception;

public class InvalidIndicesException extends RuntimeException {

    public InvalidIndicesException(String message) {
        super(message);
    }
    public InvalidIndicesException() {
        super("The specified indices are not valid!");
    }

}