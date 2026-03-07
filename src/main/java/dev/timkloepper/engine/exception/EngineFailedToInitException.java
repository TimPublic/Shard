package dev.timkloepper.engine.exception;


public class EngineFailedToInitException extends RuntimeException {

    public EngineFailedToInitException(String message) {
        super(message);
    }
    public EngineFailedToInitException() {
        super("The engine has failed to initialize!");
    }

}