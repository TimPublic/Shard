package dev.codanor.render.render_object.exceptions;

public class NoSuchVertexException extends RuntimeException {

    public NoSuchVertexException(String message) {
        super(message);
    }
    public NoSuchVertexException() {
        super("There is no vertex equal to your defined one!");
    }

}