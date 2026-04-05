package dev.timkloepper.engine;

import dev.timkloepper.engine.exceptions.EngineFailedToInitException;
import dev.timkloepper.util.Indexer;
import dev.timkloepper.visual_container.Window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;

public class Worker {

    private Worker() {
        _TASK_QUEUE = new ConcurrentLinkedQueue<>();
        _THREADS = new ArrayList<>();
    }

    private static class WorkerThread {

        private WorkerThread(ConcurrentLinkedQueue<Runnable> taskQueue) {
            _TASK_QUEUE = taskQueue;
        }

        private volatile boolean _isRunning;

        private final ConcurrentLinkedQueue<Runnable> _TASK_QUEUE;

        public void startUp() {
            _isRunning = true;

            new Thread(this::p_work).start();
        }
        public void shutDown() {
            _isRunning = false;
        }

        protected void p_work() {
            while (_isRunning) p_workFrame();
        }
        protected void p_workFrame() {
            Runnable task;

            task = _TASK_QUEUE.poll();

            if (task == null) return;

            task.run();
        }

    }
    public static class GlfwWorkerThread extends WorkerThread {

        private GlfwWorkerThread(ConcurrentLinkedQueue<Runnable> taskQueue) {
            super(taskQueue);

            _GLFW_TASKS = new ConcurrentLinkedQueue<>();

            _LOOP_TASKS = new ConcurrentHashMap<>();
            _LOOP_TASK_ID_INDEXER = new Indexer();

            startUp();
        }

        private final ConcurrentLinkedQueue<Runnable> _GLFW_TASKS;

        private final ConcurrentHashMap<Integer, Runnable> _LOOP_TASKS;
        private final Indexer _LOOP_TASK_ID_INDEXER;

        @Override
        protected void p_work() {
            if (!glfwInit()) throw new EngineFailedToInitException();

            super.p_work();

            glfwTerminate();
        }
        @Override
        protected void p_workFrame() {
            ArrayList<Runnable> loopTasksCopy;

            glfwPollEvents();

            while (!_GLFW_TASKS.isEmpty()) _GLFW_TASKS.poll().run();

            loopTasksCopy = new ArrayList<>(_LOOP_TASKS.values());
            for (Runnable loopTask : loopTasksCopy) loopTask.run();

            super.p_workFrame();
        }

        public void instruct(Runnable task) {
            if (task == null) return;

            _GLFW_TASKS.add(task);
        }
        public void instruct(Collection<Runnable> tasks) {
            tasks.forEach(this::instruct);
        }

        public int instructLooped(Runnable task) {
            int id;

            _LOOP_TASKS.put((id = _LOOP_TASK_ID_INDEXER.get()), task);

            return id;
        }
        public boolean rmvLooped(int id) {
            if (_LOOP_TASKS.remove(id) == null) return false;

            _LOOP_TASK_ID_INDEXER.free(id);

            return true;
        }

        public void contextFocus() {
            Window.glfwFocus();
        }

    }

    private final int _INIT_WORKER_AMOUNT = 1;

    private static final Worker s_INSTANCE = new Worker();

    private final ConcurrentLinkedQueue<Runnable> _TASK_QUEUE;
    private final ArrayList<WorkerThread> _THREADS;

    public static final GlfwWorkerThread GLFW = new GlfwWorkerThread(s_INSTANCE._TASK_QUEUE);

    public static void instruct(Runnable task) {
        if (task == null) return;

        s_INSTANCE._TASK_QUEUE.add(task);
    }
    public static void instruct(Collection<Runnable> tasks) {
        tasks.forEach(Worker::instruct);
    }

    private static void h_newThread() {
        WorkerThread thread;

        s_INSTANCE._THREADS.add((thread = new WorkerThread(s_INSTANCE._TASK_QUEUE)));
        thread.startUp();
    }
    private static void h_killThread() {
        WorkerThread thread;

        thread = s_INSTANCE._THREADS.removeFirst();

        if (thread == null) return;

        thread.shutDown();
    }

    protected static void p_kill() {
        while (!s_INSTANCE._THREADS.isEmpty()) h_killThread();

        GLFW.shutDown();
    }

}