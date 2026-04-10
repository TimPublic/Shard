package dev.codanor.render.specs;

import dev.codanor.engine.Worker;
import dev.codanor.render.specs.buffers.I_IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class GL_ElementBuffer implements I_IntBuffer {

    protected GL_ElementBuffer(int size) {
        Worker.GLFW.instruct(() -> {
            _id = glGenBuffers();
        });
        _pointer = 0;

        _SIZE = size;

        bind();
        Worker.GLFW.instruct(() -> {
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[size], GL_DYNAMIC_DRAW);
        });
        unbind();
    }

    private int _pointer;
    private int _id;

    private final int _SIZE;

    @Override
    public void bind() {
        Worker.GLFW.instruct(() -> {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, _id);
        });
    }
    @Override
    public void unbind() {
        Worker.GLFW.instruct(() -> {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        });
    }

    @Override
    public void add(int[] data) {
        add(_pointer, data);
    }
    @Override
    public void add(int index, int[] data) {
        if (index + data.length >= _SIZE) return;

        Worker.GLFW.instruct(() -> {
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, (long) index * Integer.BYTES, data);
        });
    }

    @Override
    public void rmv(int from, int toExcluded) {
        if (toExcluded <= from) return;

        Worker.GLFW.instruct(() -> {
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, (long) from * Integer.BYTES, new int[toExcluded - from]);
        });
    }

    @Override
    public void clear(boolean bind) {
        if (bind) bind();
        Worker.GLFW.instruct(() -> {
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[_SIZE], GL_DYNAMIC_DRAW);
        });
        if (bind) unbind();

        _pointer = 0;
    }

    @Override
    public int getSize() {
        return _SIZE;
    }

}