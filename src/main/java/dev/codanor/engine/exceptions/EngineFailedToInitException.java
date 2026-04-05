package dev.codanor.engine.exceptions;


public class EngineFailedToInitException extends RuntimeException {

    public EngineFailedToInitException(String message) {
        super(message);
    }
    public EngineFailedToInitException() {
        super("The engine has failed to initialize!");
    }

}