package dev.timkloepper.render.specs.shaders.exceptions;

public class CouldNotParseFragmentShaderException extends RuntimeException {

    public CouldNotParseFragmentShaderException(String message) {
        super(message);
    }
    public CouldNotParseFragmentShaderException() {
        super("Could not parse the fragment shader!");
    }

}