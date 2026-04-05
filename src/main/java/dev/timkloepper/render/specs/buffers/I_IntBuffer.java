package dev.timkloepper.render.specs.buffers;

public interface I_IntBuffer {

    void bind();
    void unbind();

    void add(int[] data);
    void add(int index, int[] data);

    void rmv(int from, int toExcluded);

    void clear(boolean bind);

    int getSize();

}