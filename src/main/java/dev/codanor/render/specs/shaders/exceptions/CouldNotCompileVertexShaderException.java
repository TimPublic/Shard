package dev.codanor.render.specs.shaders.exceptions;

public class CouldNotCompileVertexShaderException extends RuntimeException {

    public CouldNotCompileVertexShaderException(String message) {
        super(message);
    }
    public CouldNotCompileVertexShaderException() {
        super("Could not compile this vertex shader!");
    }

}
