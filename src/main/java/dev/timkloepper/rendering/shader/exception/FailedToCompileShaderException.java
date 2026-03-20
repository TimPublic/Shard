package dev.timkloepper.rendering.shader.exception;


public class FailedToCompileShaderException extends RuntimeException {

    public FailedToCompileShaderException(String message) {
        super(message);
    }
    public FailedToCompileShaderException() {
        super("Could not compile the shader!");
    }

}