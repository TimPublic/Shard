package dev.codanor.render.render_object.exceptions;

public class NoSuchAttributeException extends RuntimeException {

    public NoSuchAttributeException(String message) {
        super(message);
    }
    public NoSuchAttributeException() {
        super("There is no attribute equal to your defined one!");
    }

}