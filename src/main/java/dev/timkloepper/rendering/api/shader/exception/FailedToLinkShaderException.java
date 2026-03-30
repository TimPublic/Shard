package dev.timkloepper.rendering.api.shader.exception;


public class FailedToLinkShaderException extends RuntimeException {

    public FailedToLinkShaderException(String message) {
        super(message);
    }
    public FailedToLinkShaderException() {
        super("Could not link the shaders!");
    }

}