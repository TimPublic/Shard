package dev.timkloepper.rendering.shader.exception;


public class CouldNotOpenShaderFileException extends RuntimeException {

    public CouldNotOpenShaderFileException(String message) {
        super(message);
    }
    public CouldNotOpenShaderFileException() {
        super("Could not open the specified shader file!");
    }

}