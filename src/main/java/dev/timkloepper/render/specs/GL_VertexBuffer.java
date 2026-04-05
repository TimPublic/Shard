package dev.timkloepper.render.specs;

import dev.timkloepper.render.specs.buffers.I_FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

public class GL_VertexBuffer implements I_FloatBuffer {

    protected GL_VertexBuffer(int size) {
        _pointer = 0;

        _ID = glGenBuffers();
        _SIZE = size;

        bind();
        glBufferData(GL_ARRAY_BUFFER, new float[size], GL_DYNAMIC_DRAW);
        unbind();
    }

    private int _pointer;

    private final int _ID;
    private final int _SIZE;

    @Override
    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, _ID);
    }
    @Override
    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void add(float[] data) {
        add(_pointer, data);
    }
    @Override
    public void add(int index, float[] data) {
        if (index + data.length >= _SIZE) return;

        glBufferSubData(GL_ARRAY_BUFFER, (long) index * Float.BYTES, data);
    }

    @Override
    public void rmv(int from, int toExcluded) {
        if (toExcluded <= from) return;

        glBufferSubData(GL_ARRAY_BUFFER, (long) from * Float.BYTES, new float[toExcluded - from]);
    }

    @Override
    public void clear(boolean bind) {
        if (bind) bind();
        glBufferData(GL_ARRAY_BUFFER, new float[_SIZE], GL_DYNAMIC_DRAW);
        if (bind) unbind();

        _pointer = 0;
    }

    @Override
    public int getSize() {
        return _SIZE;
    }

}