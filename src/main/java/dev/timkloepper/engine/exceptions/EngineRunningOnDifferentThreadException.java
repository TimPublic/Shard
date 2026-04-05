package dev.timkloepper.engine.exceptions;

public class EngineRunningOnDifferentThreadException extends RuntimeException {

    public EngineRunningOnDifferentThreadException(String message) {
        super(message);
    }
    public EngineRunningOnDifferentThreadException() {
        super("Shard is already running on a different thread!");
    }

}