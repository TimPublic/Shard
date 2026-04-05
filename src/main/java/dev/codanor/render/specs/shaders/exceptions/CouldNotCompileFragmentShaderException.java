package dev.codanor.render.specs.shaders.exceptions;

public class CouldNotCompileFragmentShaderException extends RuntimeException {

    public CouldNotCompileFragmentShaderException(String message) {
        super(message);
    }
    public CouldNotCompileFragmentShaderException() {
        super("Could not compile this fragment shader!");
    }

}
