package dev.timkloepper.rendering.mesh.exception;

public class InvalidVerticesException extends RuntimeException {

    public InvalidVerticesException(String message) {
        super(message);
    }
    public InvalidVerticesException() {
        super("The specified vertices are not valid!");
    }

}