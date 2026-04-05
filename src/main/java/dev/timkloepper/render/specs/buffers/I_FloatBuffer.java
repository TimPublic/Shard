package dev.timkloepper.render.specs.buffers;

public interface I_FloatBuffer {

    void bind();
    void unbind();

    void add(float[] data);
    void add(int index, float[] data);

    void rmv(int from, int toExcluded);

    void clear(boolean bind);

    int getSize();

}