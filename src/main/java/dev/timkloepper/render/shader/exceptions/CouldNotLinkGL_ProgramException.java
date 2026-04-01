package dev.timkloepper.render.shader.exceptions;

public class CouldNotLinkGL_ProgramException extends RuntimeException {

    public CouldNotLinkGL_ProgramException(String message) {
        super(message);
    }
    public CouldNotLinkGL_ProgramException() {
        super("Could not compile this GL program!");
    }

}