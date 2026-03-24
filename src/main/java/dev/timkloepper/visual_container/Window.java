package dev.timkloepper.visual_container;


import dev.timkloepper.engine.Shard;
import dev.timkloepper.event_system.EventSystem;
import dev.timkloepper.event_system.I_EventSystemHolder;
import dev.timkloepper.rendering.RenderSystem;
import dev.timkloepper.visual_container.exception.WindowFailedToInitException;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;


/**
 * <p>
 *     A normal window, created with glfw. <br>
 *     Is used to display any kind of rendering and is therefore a core piece of Shard.
 * </p>
 * <p>
 *     In order to make sure, to render into the correct window, please call {@link Window#makeCurrent()}
 *     when doing custom rendering outside the normal render loop.
 * </p>
 * <p>
 *     Windows support resizing and changing of the title, which is displayed in the top left corner of the window bar.
 * </p>
 * <p>
 *     If you want to dispose a window, please call {@link Window#close()}. <br>
 *     Closing the window in the window bar, has the same effect.
 * </p>
 *
 * @version 0.1
 *
 * @author Tim Kloepper
 */
public class Window extends A_VisualContainer implements I_EventSystemHolder {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    private Window(int width, int height, String title) {
        super(width, height, true);

        _initialized = false;

        _shouldClose = false;
        _closed = false;

        _title = title;

        Shard.addTask(() -> {
            h_createGLFW();

            _initialized = true;
        });
        _closeTaskId = Shard.addLoopTask(() -> {
            if (_closed) return;

            _shouldClose = glfwWindowShouldClose(_glfwPointer);

            if (_shouldClose) closeAndRemove();
        });

        _RENDER_SYSTEM = new RenderSystem();
        _EVENT_SYSTEM = new EventSystem();
    }

    public static Window create(int width, int height, String title) {
        Window window;

        window = new Window(width, height, title);
        window._engineWindowId = Shard.addWindow(window);

        return window;
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    /**
     * Pointer or id for glfw operations. <br>
     * Should not change but due to the set-up of this class,
     * it needs to be non-final.
     */
    private long _glfwPointer;
    private int _engineWindowId;

    private volatile boolean _initialized;
    private volatile boolean _shouldClose;
    private boolean _closed;

    private A_Scene _rootAScene;

    /**
     * The title of the window, which gets displayed in the top left corner. <br>
     * This title can possibly change and due to multi threading concerns, is this property
     * {@code volatile}.
     */
    private volatile String _title;

    // </editor-folds>
    // <editor-fold desc="FINALS">

    /**
     * Keeps track of all existing windows, which is also why it is concurrent. <br>
     * This queue is used to always have a valid reference for window context sharing.
     */
    private static final ConcurrentLinkedQueue<Window> s_WINDOWS = new ConcurrentLinkedQueue<>();

    private final int _closeTaskId;

    private final RenderSystem _RENDER_SYSTEM;
    private final EventSystem _EVENT_SYSTEM;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- LIFE CYCLE -+-">

    /**
     * Closes the window which clears the callbacks and destroys the window
     * on the glfw side and removes it from the {@link Shard}.
     *
     * @author Tim Kloepper
     */
    public void closeAndRemove() {
        close();

        Shard.rmvWindow(_engineWindowId);
    }
    /**
     * Closes the window which clears the callbacks and destroys the window
     * on the glfw side without removing it from the {@link Shard}.
     *
     * @author Tim Kloepper
     */
    public void close() {
        Shard.addTask(() -> {
            glfwFreeCallbacks(_glfwPointer);
            glfwDestroyWindow(_glfwPointer);
        });

        _closed = true;

        Shard.rmvLoopTask(_closeTaskId);
    }

    // </editor-fold>
    // <editor-fold desc="-+- SET UP -+-">

    /**
     * Creates the window on the glfw side. <br>
     * Sets up windows hints, creates the window and tries to use an already existing
     * window as a reference in order to share the context. <br>
     * Also adds the window into the static queue for later references for future windows.
     *
     * @author Tim Kloepper
     */
    private void h_createGLFW() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        h_glfwWindowHints();
        h_glfwCreateWindow();
        h_pushStackFrame();

        // Make the OpenGL context current
        glfwMakeContextCurrent(_glfwPointer);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(_glfwPointer);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        makeNotCurrent();
    }

    /**
     * Sets up the glfw window hints.
     *
     * @author Tim Kloepper
     */
    private void h_glfwWindowHints() {
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);
    }
    /**
     * Creates the actual glfw window and adds it to the static queue.
     *
     * @author Tim Kloepper
     */
    private void h_glfwCreateWindow() {
        Window referenceWindow;

        referenceWindow = s_WINDOWS.peek();

        if (referenceWindow == null) _glfwPointer = glfwCreateWindow(getWidth(), getHeight(), _title, NULL, NULL);
        else _glfwPointer = glfwCreateWindow(getWidth(), getHeight(), _title, NULL, referenceWindow._glfwPointer);

        if (_glfwPointer == NULL) throw new WindowFailedToInitException("Failed to create a window with GLFW!");
    }
    /**
     * Pushes a stack frame for the glfw window in order for glfw references.
     * TODO : Check, if this is even true lol.
     *
     * @author Tim Kloepper
     */
    private void h_pushStackFrame() {
        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(_glfwPointer, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    _glfwPointer,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically
    }

    // </editor-fold>

    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        if (_closed) return;

        if (_shouldClose) {
            closeAndRemove();

            return;
        }

        makeCurrent();

        if (_rootAScene != null) _rootAScene.update(delta);

        makeNotCurrent();
    }

    // </editor-fold>

    // <editor-fold desc="-+- RENDERING -+-">

    /**
     * Makes the glfw context this window, which applies all further rendering applications
     * to this window.
     */
    public void makeCurrent() {
        glfwMakeContextCurrent(_glfwPointer);
    }
    public void makeNotCurrent() {
        glfwMakeContextCurrent(NULL);
    }

    // </editor-fold>
    // <editor-fold desc="-+- SCENE MANAGEMENT -+-">

    public boolean setScene(A_Scene AScene, boolean overwrite) {
        if (_rootAScene != null && !overwrite) return false;

        // Remove old scene, if existing.
        if (_rootAScene != null) {
            _rootAScene.p_removeRenderSystem();
            _rootAScene.p_onRemovedFromWindow(this);
        }

        // Set up new scene.
        _rootAScene = AScene;
        _rootAScene.p_getNewRenderSystem(_RENDER_SYSTEM);
        _rootAScene.p_onAddedToWindow(this);

        return true;
    }
    public Optional<A_Scene> getScene() {
        return Optional.ofNullable(_rootAScene);
    }

    // </editor-fold>

    // <editor-fold desc="-+- EVENT MANAGEMENT -+-">

    public EventSystem getEventSystem() {
        return _EVENT_SYSTEM;
    }

    // </editor-fold>

    // <editor-fold desc="-+- GETTERS -+-">

    /**
     * Gets the current title of this window. <br>
     * Be aware, that this title can change during the execution process.
     *
     * @return The current title of this window.
     */
    public String getTitle() {
        return _title;
    }
    public int getEngineWindowId() {
        return _engineWindowId;
    }

    // </editor-fold>
    // <editor-fold desc="-+- CHECKERS -+-">

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Window)) return false;

        return ((Window) obj)._glfwPointer == _glfwPointer;
    }

    public boolean isInitialized() {
        return _initialized;
    }
    public boolean isClosed() {
        return _closed;
    }

    // </editor-fold>


}