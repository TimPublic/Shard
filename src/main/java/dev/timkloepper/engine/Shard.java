package dev.timkloepper.engine;

import dev.timkloepper.visual_container.Window;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Shard {

    // <editor-fold desc="-+- CREATION -+-">

    private Shard() {
        _running = false;
        _hasStopped = false;
        _isAsync = false;

        _WINDOW_REFERENCES = new ConcurrentLinkedQueue<>();
        _WINDOW_REMOVE_QUEUE = new ConcurrentLinkedQueue<>();
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private volatile boolean _running;
    private volatile boolean _hasStopped;
    private boolean _isAsync;

    private Thread _runningThread;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private static final Shard s_INSTANCE = new Shard();

    private final ConcurrentLinkedQueue<WeakReference<Window>> _WINDOW_REFERENCES, _WINDOW_REMOVE_QUEUE;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- LIFE CYCLE MANAGEMENT -+-">

    public static boolean run(boolean async) {
        if (isRunning()) return false;

        s_INSTANCE._isAsync = async;
        s_INSTANCE._running = true;

        if (async) (s_INSTANCE._runningThread = new Thread(s_INSTANCE::h_run)).start();
        else s_INSTANCE.h_run();

        return true;
    }
    public static boolean stop() {
        Iterator<WeakReference<Window>> iterator;
        ArrayDeque<Window> windowsCopy; // Necessary, as Window::close manipulates _WINDOW_REFERENCES.

        if (!isRunning()) return false;

        s_INSTANCE._running = false;
        if (s_INSTANCE._isAsync) while (!s_INSTANCE._hasStopped); // Wait until the thread has stopped.
        s_INSTANCE._isAsync = false;

        iterator = s_INSTANCE._WINDOW_REFERENCES.iterator();
        windowsCopy = new ArrayDeque<>();

        while (iterator.hasNext()) {
            Window currentWindow;

            currentWindow = iterator.next().get();

            if (currentWindow == null) iterator.remove();
            else windowsCopy.add(currentWindow);
        }

        for (Window window : windowsCopy) window.close();

        Worker.p_kill();

        return false;
    }

    public static boolean isRunning() {
        return s_INSTANCE._running;
    }

    private void h_run() {
        while (_running) _update(1);

        _hasStopped = true;
    }
    private void _update(double delta) {
        Iterator<WeakReference<Window>> iterator;

        h_workRemoveQueue();

        iterator = _WINDOW_REFERENCES.iterator();

        while (iterator.hasNext()) {
            Window currentWindow;

            currentWindow = iterator.next().get();

            if (currentWindow == null) iterator.remove();
            else currentWindow.update(delta);
        }
    }

    // </editor-fold>
    // <editor-fold desc="-+- WINDOW MANAGEMENT -+-">

    public static boolean addWindowUpdate(Window window) {
        for (WeakReference<Window> ref : s_INSTANCE._WINDOW_REFERENCES) {
            Window currentWindow;

            currentWindow = ref.get();

            if (currentWindow == null) s_INSTANCE._WINDOW_REMOVE_QUEUE.add(ref);
            if (currentWindow == window) return false;
        }

        return s_INSTANCE._WINDOW_REFERENCES.add(new WeakReference<>(window));
    }
    public static boolean rmvWindowUpdate(Window window) {
        for (WeakReference<Window> ref : s_INSTANCE._WINDOW_REFERENCES) {
            Window currentWindow;

            currentWindow = ref.get();

            if (currentWindow == null || currentWindow == window) {
                s_INSTANCE._WINDOW_REMOVE_QUEUE.add(ref);

                return true;
            }
        }

        return false;
    }

    private static void h_workRemoveQueue() {
        while (!s_INSTANCE._WINDOW_REMOVE_QUEUE.isEmpty()) {
            s_INSTANCE._WINDOW_REFERENCES.remove(s_INSTANCE._WINDOW_REMOVE_QUEUE.poll());

            if (!s_INSTANCE._WINDOW_REFERENCES.isEmpty()) continue;

            stop();

            return;
        }
    }

    // </editor-fold>

}