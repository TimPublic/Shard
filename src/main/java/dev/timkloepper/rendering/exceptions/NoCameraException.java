package dev.timkloepper.rendering.exceptions;

public class NoCameraException extends RuntimeException {

    public NoCameraException(String message) {
        super(message);
    }
    public NoCameraException() {
        super("No camera specified!");
    }

}