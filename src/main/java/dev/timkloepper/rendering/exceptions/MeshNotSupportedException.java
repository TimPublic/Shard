package dev.timkloepper.rendering.exceptions;

public class MeshNotSupportedException extends RuntimeException {

    public MeshNotSupportedException(String message) {
        super(message);
    }
    public MeshNotSupportedException() {
        super("This mesh is not supported!");
    }

}