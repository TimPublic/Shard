package dev.timkloepper.visual_container.exception;


public class WindowFailedToInitException extends RuntimeException {

    public WindowFailedToInitException(String message) {
        super(message);
    }
    public WindowFailedToInitException() {
        super("Window has failed to initialize!");
    }

}