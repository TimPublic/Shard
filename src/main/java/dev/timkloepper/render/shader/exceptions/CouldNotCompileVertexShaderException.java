package dev.timkloepper.render.shader.exceptions;

public class CouldNotCompileVertexShaderException extends RuntimeException {

    public CouldNotCompileVertexShaderException(String message) {
        super(message);
    }
    public CouldNotCompileVertexShaderException() {
        super("Could not compile this vertex shader!");
    }

}
