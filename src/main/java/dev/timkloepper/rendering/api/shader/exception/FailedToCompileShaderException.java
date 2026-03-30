package dev.timkloepper.rendering.api.shader.exception;


public class FailedToCompileShaderException extends RuntimeException {

    public FailedToCompileShaderException(String message) {
        super(message);
    }
    public FailedToCompileShaderException() {
        super("Could not compile the shader!");
    }

}