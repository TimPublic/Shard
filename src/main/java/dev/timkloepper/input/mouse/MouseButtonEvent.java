package dev.timkloepper.input.mouse;

import dev.timkloepper.event_system.I_Event;

public class MouseButtonEvent implements I_Event {

    public MouseButtonEvent(MOUSE_BUTTON_EVENT_TYPE type, String button) {
        TYPE = type;
        BUTTON = button;
    }

    public enum MOUSE_BUTTON_EVENT_TYPE {
        PRESS,
        RELEASE,
        REPEAT,
    }

    public final MOUSE_BUTTON_EVENT_TYPE TYPE;
    public final String BUTTON;

}