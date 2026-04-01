package dev.timkloepper.render.mesh.exceptions;

public class NoSuchVertexException extends RuntimeException {

    public NoSuchVertexException(String message) {
        super(message);
    }
    public NoSuchVertexException() {
        super("There is no vertex equal to your defined one!");
    }

}