package dev.timkloepper.rendering.gpu.objects.buffer.specifications.open_gl;

import dev.timkloepper.rendering.gpu.objects.buffer.I_IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class GL_ElementBuffer implements I_IntBuffer {

    public GL_ElementBuffer(int size) {
        _pointer = 0;

        _ID = glGenBuffers();
        _SIZE = size;

        bind();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[size], GL_DYNAMIC_DRAW);
        unbind();
    }

    private int _pointer;

    private final int _ID;
    private final int _SIZE;

    @Override
    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, _ID);
    }
    @Override
    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void add(int[] data) {
        add(_pointer, data);
    }
    @Override
    public void add(int index, int[] data) {
        if (index + data.length >= _SIZE) return;
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, (long) index * Integer.BYTES, data);
    }

    @Override
    public void rmv(int from, int toExcluded) {
        if (toExcluded <= from) return;

        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, (long) from * Integer.BYTES, new int[toExcluded - from]);
    }

    @Override
    public void clear(boolean bind) {
        if (bind) bind();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[_SIZE], GL_DYNAMIC_DRAW);
        if (bind) unbind();

        _pointer = 0;
    }

    @Override
    public int getSize() {
        return _SIZE;
    }

}