package dev.codanor.input.mouse;

import dev.codanor.event_system.I_Event;

public class MouseScrolledEvent implements I_Event {

    public MouseScrolledEvent(double xOffset, double yOffset) {
        X_OFFSET = xOffset;
        Y_OFFSET = yOffset;
    }

    public final double X_OFFSET, Y_OFFSET;

}