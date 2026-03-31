package dev.timkloepper.input.key;

import dev.timkloepper.event_system.EventSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {

    public KeyListener(EventSystem eventSystem) {
        _EVENT_SYSTEM = eventSystem;

        _CHAR_MAP = new HashMap<>();

        _BUFFER = new ConcurrentLinkedQueue<>();
        _PREV_BUFFER = new ArrayList<>();
    }

    private final EventSystem _EVENT_SYSTEM;

    private final HashMap<Integer, Character> _CHAR_MAP;

    private final ConcurrentLinkedQueue<Integer> _BUFFER;
    ArrayList<Integer> _PREV_BUFFER;

    public void keyCallback(long windowPointer, int key, int scanCode, int action, int mods) {
        switch (action) {
            case GLFW_PRESS -> _BUFFER.add(scanCode);
            case GLFW_RELEASE -> _PREV_BUFFER.add(scanCode);
        }
    }

    public void update() {
        for (Integer scanCode : _BUFFER) {
            if (_PREV_BUFFER.remove(scanCode)) h_pushRepeat(scanCode);
            else h_pushPress(scanCode);
        }

        _PREV_BUFFER.forEach(this::h_pushRelease);
        _PREV_BUFFER.clear();

        _PREV_BUFFER.addAll(_BUFFER);
        _BUFFER.clear();
    }

    private void h_pushPress(int scanCode) {
        KeyEvent event;

        event = new KeyEvent(
                KeyEvent.KEY_EVENT_TYPE.PRESS,
                _CHAR_MAP.get(scanCode),
                scanCode
        );

        _EVENT_SYSTEM.push(event);
    }
    private void h_pushRelease(int scanCode) {
        KeyEvent event;

        event = new KeyEvent(
                KeyEvent.KEY_EVENT_TYPE.RELEASE,
                _CHAR_MAP.get(scanCode),
                scanCode
        );

        _EVENT_SYSTEM.push(event);
    }
    private void h_pushRepeat(int scanCode) {
        KeyEvent event;

        event = new KeyEvent(
                KeyEvent.KEY_EVENT_TYPE.REPEAT,
                _CHAR_MAP.get(scanCode),
                scanCode
        );

        _EVENT_SYSTEM.push(event);
    }

}