package dev.timkloepper.rendering.shader.exception;


public class FailedToLinkShadersException extends RuntimeException {

    public FailedToLinkShadersException(String message) {
        super(message);
    }
    public FailedToLinkShadersException() {
        super("Could not link the shaders!");
    }

}