package dev.timkloepper.render.specs.shaders.exceptions;

public class CouldNotParseVertexShaderException extends RuntimeException {

    public CouldNotParseVertexShaderException(String message) {
        super(message);
    }
    public CouldNotParseVertexShaderException() {
        super("Could not parse the vertex shader!");
    }

}