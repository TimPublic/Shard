package dev.codanor.input.mouse;

import dev.codanor.event_system.EventSystem;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {

    public MouseListener(EventSystem eventSystem) {
        _EVENT_SYSTEM = eventSystem;

        _BUTTON_MAP = new HashMap<>();

        _CURSOR_POS = new Vector2d();
        _PREV_CURSOR_POS = new Vector2d();

        _BUFFER = new ConcurrentLinkedQueue<>();
        _PREV_BUFFER = new ArrayList<>();
    }

    private double xOffset, yOffset;

    private final EventSystem _EVENT_SYSTEM;

    private final HashMap<Integer, String> _BUTTON_MAP;

    private final Vector2d _CURSOR_POS, _PREV_CURSOR_POS;

    private final ConcurrentLinkedQueue<Integer> _BUFFER;
    private final ArrayList<Integer> _PREV_BUFFER;

    public void cursorPositionCallback(long window, double x, double y) {
        _CURSOR_POS.set(x, y);
    }
    public void mouseButtonCallback(long window, int button, int action, int mods) {
        switch (action) {
            case GLFW_PRESS -> _BUFFER.add(action);
            case GLFW_RELEASE -> _BUFFER.remove(action);
        }
    }
    public void scrollCallback(long window, double xOffset, double yOffset) {
        this.xOffset += xOffset;
        this.yOffset += yOffset;
    }

    public void update() {
        h_manageCursorPosition();
        h_manageMouseButtons();
        h_manageScroll();
    }

    private void h_manageCursorPosition() {
        if (_CURSOR_POS.x != _PREV_CURSOR_POS.x || _CURSOR_POS.y != _PREV_CURSOR_POS.y) _EVENT_SYSTEM.push(new CursorMovedEvent(_CURSOR_POS));
        _PREV_CURSOR_POS.set(_CURSOR_POS);
    }
    private void h_manageMouseButtons() {
        for (Integer action : _BUFFER) {
            if (_PREV_BUFFER.remove(action)) h_pushRepeat(action);
            else h_pushPress(action);
        }

        _PREV_BUFFER.forEach(this::h_pushRelease);
        _PREV_BUFFER.clear();

        _PREV_BUFFER.addAll(_BUFFER);
        _BUFFER.clear();
    }
    private void h_manageScroll() {
        if (xOffset != 0 || yOffset != 0) _EVENT_SYSTEM.push(new MouseScrolledEvent(xOffset, yOffset));
        xOffset = 0;
        yOffset = 0;
    }

    private void h_pushPress(Integer action) {
        MouseButtonEvent event;

        event = new MouseButtonEvent(
                MouseButtonEvent.MOUSE_BUTTON_EVENT_TYPE.PRESS,
                _BUTTON_MAP.get(action)
        );

        _EVENT_SYSTEM.push(event);
    }
    private void h_pushRelease(Integer action) {
        MouseButtonEvent event;

        event = new MouseButtonEvent(
                MouseButtonEvent.MOUSE_BUTTON_EVENT_TYPE.RELEASE,
                _BUTTON_MAP.get(action)
        );

        _EVENT_SYSTEM.push(event);
    }
    private void h_pushRepeat(Integer action) {
        MouseButtonEvent event;

        event = new MouseButtonEvent(
                MouseButtonEvent.MOUSE_BUTTON_EVENT_TYPE.REPEAT,
                _BUTTON_MAP.get(action)
        );

        _EVENT_SYSTEM.push(event);
    }

}