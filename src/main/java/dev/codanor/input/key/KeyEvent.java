package dev.codanor.input.key;

import dev.codanor.event_system.I_Event;

public class KeyEvent implements I_Event {

    public KeyEvent(KEY_EVENT_TYPE type, Character key, int scanCode) {
        TYPE = type;
        KEY = key;
        SCAN_CODE = scanCode;
    }

    public enum KEY_EVENT_TYPE {
        PRESS,
        RELEASE,
        REPEAT,
    }

    public final KEY_EVENT_TYPE TYPE;
    public final Character KEY;
    public final int SCAN_CODE;

}