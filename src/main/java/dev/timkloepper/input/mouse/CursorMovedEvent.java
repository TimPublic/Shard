package dev.timkloepper.input.mouse;

import dev.timkloepper.event_system.I_Event;
import org.joml.Vector2d;

public class CursorMovedEvent implements I_Event {

    public CursorMovedEvent(Vector2d pos) {
        POS = new Vector2d(pos);
    }

    public final Vector2d POS;

}