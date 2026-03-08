package dev.timkloepper.engine;


import dev.timkloepper.engine.exception.EngineFailedToInitException;
import dev.timkloepper.engine.exception.EngineRunningOnDifferentThreadException;
import dev.timkloepper.render_container.Window;
import dev.timkloepper.util.Indexer;

import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwTerminate;


/**
 * <p>
 *     Is the top class in the hierarchy. <br>
 *     This class is the origin of the update loop of Shard.
 * </p>
 * <p>
 *     There can only ever exist one instance of {@link Engine},
 *     this is ensured by the class itself and the user has no option
 *     to bypass this restriction nor should they do it anyway.
 * </p>
 * <p>
 *     The engine supports running on another thread but please
 *     do not do this on your own and use the respective {@link Engine#runAsync()} method.
 * </p>
 * <p>
 *     All publicly exposed methods are static and the engine itself
 *     cannot be instantiated by the user. <br>
 *     Everything you need is accessible through those static methods: <br>
 *     <ul>
 *         <li>[Life cycle] : {@link Engine#reset()}, {@link Engine#rerun()}, {@link Engine#kill()},
 *                            {@link Engine#rerunAsync()}</li>
 *         <li>[Update loop] : {@link Engine#run()}, {@link Engine#pause()}, {@link Engine#stepFrames(int)},
 *                             {@link Engine#runAsync()}</li>
 *         <li>[Info] : {@link Engine#isRunning()}, {@link Engine#isInstantiated()}</li>
 *     </ul> <br>
 *     If no {@link Engine} instance exists, one is created upon any static call. <br>
 *     Nonetheless, a {@link Engine#tryCreate()} method exists, in order for the user
 *     to have a clear API access for a little bit more control.
 * </p>
 * <p>
 *     The engines main priority is the management of any {@link Window}. <br>
 *     It holds and updates them inside its own update loop. <br>
 *     This also means, pausing the engine, automatically pauses all windows which will then most likely pause
 *     their held objects.
 * </p>
 * <p>
 *     The {@link Engine} class is also thread safe, meaning that it would not be a problem
 *     to {@link Engine#run()} it on one thread and {@link Engine#kill()} it on another as they all share
 *     the same volatile instance.
 * </p>
 *
 * @version 0.1
 *
 * @author Tim Kloepper
 */
public class Engine {

    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    private Engine() {
        _runningState = _RUNNING_STATE.NONE;

        _INDEXER = new Indexer();
        _WINDOWS_BY_INDEX = new ConcurrentHashMap<>();

        h_initGLFW();
    }

    /**
     * Initializes glfw.
     *
     * @author Tim Kloepper
     */
    private void h_initGLFW() {
        if (!glfwInit()) throw new EngineFailedToInitException("Shard was not able to initialize GLFW!");
    }

    // </editor-fold>

    // <editor-fold desc="-+- ENUMS -+-">

    private enum _RUNNING_STATE {
        SYNC,
        ASYNC,
        NONE,
    }

    // </editor-fold>
    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    /**
     * Is the current instance of {@link Engine} on which
     * the statically exposed methods work on. <br>
     * Is volatile to support multi threading.
     */
    private volatile static Engine _instance;
    private static boolean _glfwInitialized;

    /**
     * Indicates, if the engine is currently running or not. <br>
     * Is also used directly by the engine to determines, if it should
     * loop-call {@link Engine#_update()} or not, so please do not change
     * the value without keeping this in mind. <br>
     * Is volatile to support multi threading.
     */
    private volatile _RUNNING_STATE _runningState;
    private Thread _asyncThread;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final Indexer _INDEXER;
    private final ConcurrentHashMap<Integer, Window> _WINDOWS_BY_INDEX;

    // </editor-fold>


    // </editor-fold>

    // <editor-fold desc="-+- LIFE CYCLE -+-">

    /**
     * Checks, if an instance of {@link Engine} currently exists and if not,
     * creates one in order for other methods to avoid a {@link NullPointerException}.
     *
     * @author Tim Kloepper
     */
    public static void tryCreate() {
        if (_instance == null) _instance = new Engine();
    }
    /**
     * Kills the current {@link Engine} instance and does not create a new one,
     * which leads to the next method called, needing to create one again. <br>
     * This method is safe to call, even if no {@link Engine} instance is currently
     * existing. <br>
     * You can use this method from any thread to kill the engine on either async execution
     * or synced.
     *
     * @author Tim Kloepper
     */
    public static void kill() {
        boolean async;

        if (_instance == null) return;

        async = _instance._runningState == _RUNNING_STATE.ASYNC;

        _instance._runningState = _RUNNING_STATE.NONE;

        if (async) {
            try {
                _instance._asyncThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        _instance = null;

        // Needs to be called, after ending the update loop.
        glfwTerminate();
    }

    /**
     * Kills the current {@link Engine} instance and creates a new one,
     * but does not {@link Engine#run()} it. <br>
     * If you want to instantly run after the reset, please use {@link Engine#rerun()}.
     *
     * @author Tim Kloepper
     */
    public static void reset() {
        kill();

        _instance = new Engine();
    }
    /**
     * Kills the current {@link Engine} instance and creates a new one,
     * instantly calling {@link Engine#run()} again. <br>
     * If you do not want an automatic restart, please use {@link Engine#reset()}.
     *
     * @author Tim Kloepper
     */
    public static void rerun() {
        reset();

        run();
    }
    /**
     * Kills the current {@link Engine} instance and creates a new one,
     * instantly calling {@link Engine#runAsync()} again. <br>
     * If you do not want an automatic restart, please use {@link Engine#reset()}.
     *
     * @author Tim Kloepper
     */
    public static void rerunAsync() {
        reset();

        runAsync();
    }

    // </editor-fold>
    // <editor-fold desc="-+- UPDATE LOOP -+-">

    /**
     * <p>
     *     Activates the update loop, calling
     *     {@link Engine#_update()} in an infinite loop
     *     until {@link Engine#pause()}, {@link Engine#reset()}
     *     or {@link Engine#kill()} is called. <br>
     *     Be aware, that {@link Engine#reset()} will call {@link Engine#kill()}
     *     on the current {@link Engine} instance.
     * </p>
     * <p>
     *     If this static method is called as the first method
     *     called on {@link Engine} in an execution,
     *     it also creates an instance of the engine, possibly
     *     slowing the method's execution down slightly.
     *     This is also true, if the last {@link Engine}
     *     method called was {@link Engine#kill()}.
     * </p>
     *
     * @author Tim Kloepper
     */
    public static void run() {
        tryCreate();

        if (_instance._runningState == _RUNNING_STATE.ASYNC) throw new EngineRunningOnDifferentThreadException();
        if (_instance._runningState == _RUNNING_STATE.SYNC) return;
        _instance._runningState = _RUNNING_STATE.SYNC;

        while (_instance._runningState == _RUNNING_STATE.SYNC) {
            _instance._update();

            if (_instance == null) return;
        }
    }
    /**
     * <p>
     *     Activates the update loop, calling
     *     {@link Engine#_update()} in an infinite loop
     *     until {@link Engine#pause()}, {@link Engine#reset()}
     *     or {@link Engine#kill()} is called. <br>
     *     Be aware, that {@link Engine#reset()} will call {@link Engine#kill()}
     *     on the current {@link Engine} instance.
     * </p>
     * <p>
     *     This method activates the update loop on another thread, enabling you to use
     *     the main thread for other things. <br>
     *     You can still use all other methods as normal.
     * </p>
     * <p>
     *     If this static method is called as the first method
     *     called on {@link Engine} in an execution,
     *     it also creates an instance of the engine, possibly
     *     slowing the method's execution down slightly.
     *     This is also true, if the last {@link Engine}
     *     method called was {@link Engine#kill()}.
     * </p>
     *
     * @author Tim Kloepper
     */
    public static void runAsync() {
        tryCreate();

        if (_instance._runningState == _RUNNING_STATE.SYNC) throw new EngineRunningOnDifferentThreadException("Engine is already running on the main thread!");
        if (_instance._runningState == _RUNNING_STATE.ASYNC) return;
        _instance._runningState = _RUNNING_STATE.ASYNC;

        _instance._asyncThread = new Thread(() -> {
            while (_instance._runningState == _RUNNING_STATE.ASYNC) {
                _instance._update();

                if (_instance == null) return;
            }
        });
        _instance._asyncThread.start();
    }
    /**
     * <p>
     *     Pauses the update loop by not calling {@link Engine#_update()} anymore,
     *     until you call {@link Engine#run()} or {@link Engine#rerun()}, where
     *     {@link Engine#rerun()} first calls {@link Engine#kill()} on the current
     *     {@link Engine} instance.
     * </p>
     * <p>
     *     If this static method is called as the first method called on {@link Engine} in an execution,
     *     it also creates an instance of the engine, possibly slowing the method's execution down slightly.
     *     This is also true, if the last {@link Engine} method called was {@link Engine#kill()}.
     * </p>
     *
     * @author Tim Kloepper
     */
    public static void pause() {
        boolean async;

        tryCreate();

        async = _instance._runningState == _RUNNING_STATE.ASYNC;

        _instance._runningState = _RUNNING_STATE.NONE;

        if (async) {
            try {
                _instance._asyncThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Calls {@link Engine#_update(double)} exactly {@code amount} time with a delta
     * of {@code 1} for the first {@link Engine#_update()} call.
     * This essentially means stepping one frame forwards
     * with a delta that does not manipulate systems such as movement systems,
     * which typically multiply their speed with the delta time.
     *
     * @param amount Amount of calls to {@link Engine#_update()}.
     * 
     * @author Tim Kloepper
     */
    public static void stepFrames(int amount) {
        if (amount <= 0) return;

        tryCreate();

        _instance._update(1);
        for (int counter = 1; counter < amount; counter++) _instance._update();
    }

    /**
     * Contains the logic for updating the engine,
     * meaning the updates of primarily windows. <br>
     * This method overwrite calculates its own delta time.
     *
     * @author Tim Kloepper
     */
    private void _update() {
        _update(1); // TODO : Calculate actual delta time.
    }
    /**
     * Contains the logic for updating the engine,
     * meaning the updates of primarily windows. <br>
     * This method overwrite does not calculate its own delta
     * time and requires you to provide one.
     *
     * @param delta Time passed since the last frame or rather update call.
     *
     * @author Tim Kloepper
     */
    private void _update(double delta) {
        for (Window window : _WINDOWS_BY_INDEX.values()) window.update(delta);
    }

    // </editor-fold>

    // <editor-fold desc="-+- WINDOW MANAGEMENT -+-">

    public static int addWindow(Window window) {
        int index;

        if (_instance._WINDOWS_BY_INDEX.containsValue(window)) return -1;

        index = _instance._INDEXER.get();
        _instance._WINDOWS_BY_INDEX.put(index, window);

        return index;
    }
    public static boolean rmvWindow(int index) {
        Window window;

        window = _instance._WINDOWS_BY_INDEX.get(index);
        if (window == null) return false;

        _instance._WINDOWS_BY_INDEX.remove(index);
        if (!window.isClosed()) window.close();

        return true;
    }

    // </editor-fold>

    // <editor-fold desc="-+- CHECKERS -+-">

    /**
     * Shows, whether the engine is currently running,
     * meaning it calls {@link Engine#_update()} in a loop,
     * or not.
     *
     * @return Whether the engine is running, or not.
     */
    public static boolean isRunning() {
        tryCreate();

        return _instance._runningState != _RUNNING_STATE.NONE;
    }
    /**
     * Checks, whether there currently exists an instance of {@link Engine}.
     *
     * @return Whether there is an instance of {@link Engine} existing, or not.
     */
    public static boolean isInstantiated() {
        return _instance != null;
    }

    // </editor-fold>

}