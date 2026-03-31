package dev.timkloepper.visual_container;

import dev.timkloepper.engine.Shard;
import dev.timkloepper.engine.Worker;
import dev.timkloepper.visual_container.exception.WindowFailedToInitException;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window extends A_VisualContainer {

    // <editor-fold desc="-+- CREATION -+-">

    private Window(int width, int height, String title) {
        super(width, height, true);

        _closed = false;

        _title = title;

        Worker.GLFW.instruct(this::h_createGLFW);
    }

    public static Window create(int width, int height, String title) {
        Window window;

        System.out.println(Shard.addWindowUpdate((window = createIndependent(width, height, title))));

        return window;
    }
    public static Window createIndependent(int width, int height, String title) {
        Window window;

        window = new Window(width, height, title);

        window._glfwLoopTaskId = Worker.GLFW.instructLooped(() -> {
            if (window._glfwId == NULL) return;

            if (glfwWindowShouldClose(window._glfwId)) {
                window.close();

                return;
            }

            window.setRenderFocus(true);
            glClear(GL_COLOR_BUFFER_BIT);
            glfwSwapBuffers(window._glfwId);
            window.setRenderFocus(false);
        });

        return window;
    }

    /**
     * Creates the window on the GLFW side. <br>
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

        setRenderFocus(true);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(_glfwId);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        setRenderFocus(false);
    }

    /**
     * Sets up the GLFW window hints.
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
     * Creates the actual GLFW window and adds it to the static queue.
     *
     * @author Tim Kloepper
     */
    private void h_glfwCreateWindow() {
        Window referenceWindow;

        referenceWindow = s_WINDOWS.peek();

        if (referenceWindow == null) _glfwId = glfwCreateWindow(getWidth(), getHeight(), _title, NULL, NULL);
        else _glfwId = glfwCreateWindow(getWidth(), getHeight(), _title, NULL, referenceWindow._glfwId);

        if (_glfwId == NULL) throw new WindowFailedToInitException("Failed to create a window with GLFW!");

        s_WINDOWS.add(this);
    }
    /**
     * Pushes a stack frame for the GLFW window in order for GLFW references.
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
            glfwGetWindowSize(_glfwId, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    _glfwId,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private long _glfwId;
    private boolean _renderFocused;

    private int _glfwLoopTaskId;

    private boolean _closed;

    private String _title;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private static final ConcurrentLinkedQueue<Window> s_WINDOWS = new ConcurrentLinkedQueue<>();

    // </editor-fold>

    // </editor-fold>

    public void close() {
        if (_closed) return;

        Worker.GLFW.rmvLooped(_glfwLoopTaskId);

        Shard.rmvWindowUpdate(this);

        Worker.GLFW.instruct(() -> {
            glfwFreeCallbacks(_glfwId);
            glfwDestroyWindow(_glfwId);
        });

        _closed = true;
    }

    public void setRenderFocus(boolean state) {
        if (_renderFocused == state) return;

        if (state) glfwMakeContextCurrent(_glfwId);
        else glfwMakeContextCurrent(NULL);

        _renderFocused = state;
    }

    @Override
    public void update(double delta) {}

}