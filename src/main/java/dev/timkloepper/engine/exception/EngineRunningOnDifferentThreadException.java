package dev.timkloepper.engine.exception;

public class EngineRunningOnDifferentThreadException extends RuntimeException {

    public EngineRunningOnDifferentThreadException(String message) {
        super(message);
    }
    public EngineRunningOnDifferentThreadException() {
        super("Engine is already running on a different thread!");
    }

}