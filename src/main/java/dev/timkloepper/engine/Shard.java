package dev.timkloepper.engine;


import dev.timkloepper.engine.exception.EngineFailedToInitException;
import dev.timkloepper.engine.exception.EngineRunningOnDifferentThreadException;
import dev.timkloepper.event_system.EventSystem;
import dev.timkloepper.visual_container.Window;
import dev.timkloepper.util.Indexer;

import java.lang.ref.Cleaner;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.lwjgl.glfw.GLFW.*;


/**
 * <p>
 *     Is the top class in the hierarchy. <br>
 *     This class is the origin of the update loop of Shard.
 * </p>
 * <p>
 *     There can only ever exist one instance of {@link Shard},
 *     this is ensured by the class itself and the user has no option
 *     to bypass this restriction nor should they do it anyway.
 * </p>
 * <p>
 *     The engine supports running on another thread but please
 *     do not do this on your own and use the respective {@link Shard#runAsync()} method.
 * </p>
 * <p>
 *     All publicly exposed methods are static and the engine itself
 *     cannot be instantiated by the user. <br>
 *     Everything you need is accessible through those static methods: <br>
 *     <ul>
 *         <li>[Life cycle] : {@link Shard#reset()}, {@link Shard#rerun()}, {@link Shard#kill()},
 *                            {@link Shard#rerunAsync()}</li>
 *         <li>[Update loop] : {@link Shard#run()}, {@link Shard#pause()}, {@link Shard#stepFrames(int)},
 *                             {@link Shard#runAsync()}</li>
 *         <li>[Info] : {@link Shard#isRunning()}, {@link Shard#isInstantiated()}</li>
 *     </ul> <br>
 *     If no {@link Shard} instance exists, one is created upon any static call. <br>
 *     Nonetheless, a {@link Shard#tryCreate()} method exists, in order for the user
 *     to have a clear API access for a little bit more control.
 * </p>
 * <p>
 *     The engines main priority is the management of any {@link Window}. <br>
 *     It holds and updates them inside its own update loop. <br>
 *     This also means, pausing the engine, automatically pauses all windows which will then most likely pause
 *     their held objects.
 * </p>
 * <p>
 *     The {@link Shard} class is also thread safe, meaning that it would not be a problem
 *     to {@link Shard#run()} it on one thread and {@link Shard#kill()} it on another as they all share
 *     the same volatile instance.
 * </p>
 *
 * @version 0.1
 *
 * @author Tim Kloepper
 */
public class Shard {

    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    private Shard() {
        _runningState = _RUNNING_STATE.NONE;

        _WINDOW_ENGINE_ID_INDEXER = new Indexer();
        _WINDOWS_BY_INDEX = new ConcurrentHashMap<>();

        _WINDOW_REMOVE_QUEUE = new ConcurrentLinkedQueue<>();

        _TASK_QUEUE = new LinkedBlockingQueue<>();
        _LOOP_TASKS = new ConcurrentHashMap<>();
        _LOOP_TASK_INDEXER = new Indexer();

        _CLEANER = Cleaner.create();

        _EVENT_SYSTEM = new EventSystem();

        h_initGLFW();
    }

    /**
     * Initializes glfw.
     *
     * @author Tim Kloepper
     */
    private void h_initGLFW() {
        _shouldKillGlfwThread = false;

        _glfwThread = new Thread(() -> {
            HashSet<Thread> threadsCopy;
            Thread mainThread;

            try {
                if (!glfwInit()) throw new EngineFailedToInitException("Shard was not able to initialize GLFW!");

                mainThread = null;

                threadsCopy = new HashSet<>(Thread.getAllStackTraces().keySet());
                for (Thread thread : threadsCopy) {
                    if (!Objects.equals(thread.getName(), "main")) continue;

                    mainThread = thread;
                }

                while (!_shouldKillGlfwThread) {
                    HashSet<Runnable> tasksCopy, loopTasksCopy;

                    if (mainThread == null) break;
                    if (!mainThread.isAlive()) break;

                    while (!_TASK_QUEUE.isEmpty()) {
                        Runnable task;

                        task = _TASK_QUEUE.poll();

                        if (task == null) continue;

                        task.run();
                    }

                    loopTasksCopy = new HashSet<>(_LOOP_TASKS.values());

                    for (Runnable loopTask : loopTasksCopy) {
                        loopTask.run();
                    }

                    glfwPollEvents();
                }

                glfwTerminate();
            } catch (RuntimeException e) {
                System.out.println("ERROR : " + e);
            }
        });

        _glfwThread.start();
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
     * Is the current instance of {@link Shard} on which
     * the statically exposed methods work on. <br>
     * Is volatile to support multi threading.
     */
    private volatile static Shard _instance;

    /**
     * Indicates, if the engine is currently running or not. <br>
     * Is also used directly by the engine to determines, if it should
     * loop-call {@link Shard#_update()} or not, so please do not change
     * the value without keeping this in mind. <br>
     * Is volatile to support multi threading.
     */
    private volatile _RUNNING_STATE _runningState;

    private Thread _asyncThread;
    private Thread _glfwThread;
    private volatile boolean _shouldKillGlfwThread;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final Indexer _WINDOW_ENGINE_ID_INDEXER;
    private final ConcurrentHashMap<Integer, Window> _WINDOWS_BY_INDEX;

    private final ConcurrentLinkedQueue<Integer> _WINDOW_REMOVE_QUEUE;

    private final LinkedBlockingQueue<Runnable> _TASK_QUEUE;
    private final ConcurrentHashMap<Integer, Runnable> _LOOP_TASKS;
    private final Indexer _LOOP_TASK_INDEXER;

    private final Cleaner _CLEANER;

    private final EventSystem _EVENT_SYSTEM;

    // </editor-fold>


    // </editor-fold>

    // <editor-fold desc="-+- LIFE CYCLE -+-">

    /**
     * Checks, if an instance of {@link Shard} currently exists and if not,
     * creates one in order for other methods to avoid a {@link NullPointerException}.
     *
     * @author Tim Kloepper
     */
    public static void tryCreate() {
        if (_instance == null) _instance = new Shard();
    }
    /**
     * Kills the current {@link Shard} instance and does not create a new one,
     * which leads to the next method called, needing to create one again. <br>
     * This method is safe to call, even if no {@link Shard} instance is currently
     * existing. <br>
     * You can use this method from any thread to kill the engine on either async execution
     * or synced.
     *
     * @author Tim Kloepper
     */
    public static void kill() {
        boolean async;
        Iterator<Integer> iterator;

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

        iterator = _instance._WINDOWS_BY_INDEX.keySet().iterator();
        while (iterator.hasNext()) {
            Window window;

            window = _instance._WINDOWS_BY_INDEX.get(iterator.next());
            window.close();

            iterator.remove();
        }

        _instance._killGlfwThread();

        _instance = null;
    }

    /**
     * Kills the current {@link Shard} instance and creates a new one,
     * but does not {@link Shard#run()} it. <br>
     * If you want to instantly run after the reset, please use {@link Shard#rerun()}.
     *
     * @author Tim Kloepper
     */
    public static void reset() {
        kill();

        _instance = new Shard();
    }
    /**
     * Kills the current {@link Shard} instance and creates a new one,
     * instantly calling {@link Shard#run()} again. <br>
     * If you do not want an automatic restart, please use {@link Shard#reset()}.
     *
     * @author Tim Kloepper
     */
    public static void rerun() {
        reset();

        run();
    }
    /**
     * Kills the current {@link Shard} instance and creates a new one,
     * instantly calling {@link Shard#runAsync()} again. <br>
     * If you do not want an automatic restart, please use {@link Shard#reset()}.
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
     *     {@link Shard#_update()} in an infinite loop
     *     until {@link Shard#pause()}, {@link Shard#reset()}
     *     or {@link Shard#kill()} is called. <br>
     *     Be aware, that {@link Shard#reset()} will call {@link Shard#kill()}
     *     on the current {@link Shard} instance.
     * </p>
     * <p>
     *     If this static method is called as the first method
     *     called on {@link Shard} in an execution,
     *     it also creates an instance of the engine, possibly
     *     slowing the method's execution down slightly.
     *     This is also true, if the last {@link Shard}
     *     method called was {@link Shard#kill()}.
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
     *     {@link Shard#_update()} in an infinite loop
     *     until {@link Shard#pause()}, {@link Shard#reset()}
     *     or {@link Shard#kill()} is called. <br>
     *     Be aware, that {@link Shard#reset()} will call {@link Shard#kill()}
     *     on the current {@link Shard} instance.
     * </p>
     * <p>
     *     This method activates the update loop on another thread, enabling you to use
     *     the main thread for other things. <br>
     *     You can still use all other methods as normal.
     * </p>
     * <p>
     *     If this static method is called as the first method
     *     called on {@link Shard} in an execution,
     *     it also creates an instance of the engine, possibly
     *     slowing the method's execution down slightly.
     *     This is also true, if the last {@link Shard}
     *     method called was {@link Shard#kill()}.
     * </p>
     *
     * @author Tim Kloepper
     */
    public static void runAsync() {
        tryCreate();

        if (_instance._runningState == _RUNNING_STATE.SYNC) throw new EngineRunningOnDifferentThreadException("Shard is already running on the main thread!");
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
     *     Pauses the update loop by not calling {@link Shard#_update()} anymore,
     *     until you call {@link Shard#run()} or {@link Shard#rerun()}, where
     *     {@link Shard#rerun()} first calls {@link Shard#kill()} on the current
     *     {@link Shard} instance.
     * </p>
     * <p>
     *     If this static method is called as the first method called on {@link Shard} in an execution,
     *     it also creates an instance of the engine, possibly slowing the method's execution down slightly.
     *     This is also true, if the last {@link Shard} method called was {@link Shard#kill()}.
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
     * Calls {@link Shard#_update(double)} exactly {@code amount} time with a delta
     * of {@code 1} for the first {@link Shard#_update()} call.
     * This essentially means stepping one frame forwards
     * with a delta that does not manipulate systems such as movement systems,
     * which typically multiply their speed with the delta time.
     *
     * @param amount Amount of calls to {@link Shard#_update()}.
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
        h_removeWindows();

        for (Window window : _WINDOWS_BY_INDEX.values()) if (window.isInitialized()) window.update(delta);
    }

    // </editor-fold>

    // <editor-fold desc="-+- GLFW THREAD MANAGEMENT -+-">

    public static void addTask(Runnable task) {
        tryCreate();

        _instance._TASK_QUEUE.add(task);
    }

    public static int addLoopTask(Runnable task) {
        int id;

        tryCreate();

        id = _instance._LOOP_TASK_INDEXER.get();

        _instance._LOOP_TASKS.put(id, task);

        return id;
    }
    public static void rmvLoopTask(int id) {
        _instance._LOOP_TASKS.remove(id);
        _instance._LOOP_TASK_INDEXER.free(id);
    }

    private void _killGlfwThread() {
        _shouldKillGlfwThread = true;

        try {
            _glfwThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        _shouldKillGlfwThread = false;
    }

    // </editor-fold>

    // <editor-fold desc="-+- WINDOW MANAGEMENT -+-">

    public static int addWindow(Window window) {
        int index;

        if (_instance._WINDOWS_BY_INDEX.containsValue(window)) return -1;

        index = _instance._WINDOW_ENGINE_ID_INDEXER.get();
        _instance._WINDOWS_BY_INDEX.put(index, window);

        return index;
    }
    public static boolean rmvWindow(int index) {
        if (!_instance._WINDOWS_BY_INDEX.containsKey(index)) return false;

        // Avoid concurrent modification exceptions when the engine is running.
        if (_instance._runningState != _RUNNING_STATE.NONE) _instance._WINDOW_REMOVE_QUEUE.add(index);
        else {
            Window window;

            window = _instance._WINDOWS_BY_INDEX.remove(index);
            if (!window.isClosed()) window.close();

            _instance._WINDOW_ENGINE_ID_INDEXER.free(index);
        }

        return true;
    }

    private void h_removeWindows() {
        while (!_WINDOW_REMOVE_QUEUE.isEmpty()) {
            int id;
            Window window;

            id = _WINDOW_REMOVE_QUEUE.poll();

            window = _WINDOWS_BY_INDEX.remove(id);
            if (!window.isClosed()) window.close();

            _WINDOW_ENGINE_ID_INDEXER.free(id);
        }
    }

    // </editor-fold>

    // <editor-fold desc="-+- CLEANER MANAGEMENT -+-">

    public static Cleaner getCleaner() {
        tryCreate();

        return _instance._CLEANER;
    }

    // </editor-fold>

    // <editor-fold desc="-+- EVENT MANAGEMENT -+-">

    public static EventSystem getEventSystem() {
        tryCreate();

        return _instance._EVENT_SYSTEM;
    }

    // </editor-fold>

    // <editor-fold desc="-+- CHECKERS -+-">

    /**
     * Shows, whether the engine is currently running,
     * meaning it calls {@link Shard#_update()} in a loop,
     * or not.
     *
     * @return Whether the engine is running, or not.
     */
    public static boolean isRunning() {
        tryCreate();

        return _instance._runningState != _RUNNING_STATE.NONE;
    }
    /**
     * Checks, whether there currently exists an instance of {@link Shard}.
     *
     * @return Whether there is an instance of {@link Shard} existing, or not.
     */
    public static boolean isInstantiated() {
        return _instance != null;
    }

    // </editor-fold>

}