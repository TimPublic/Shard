package dev.timkloepper.render.specs.shaders.exceptions;

public class CouldNotReadShaderFileException extends RuntimeException {

    public CouldNotReadShaderFileException(String message) {
        super(message);
    }
    public CouldNotReadShaderFileException() {
        super("Could not read the shader file!");
    }

}