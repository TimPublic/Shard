package dev.timkloepper.render.mesh.exceptions;

public class NoSuchAttributeException extends RuntimeException {

    public NoSuchAttributeException(String message) {
        super(message);
    }
    public NoSuchAttributeException() {
        super("There is no attribute equal to your defined one!");
    }

}