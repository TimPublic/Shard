package dev.codanor.render.specs.allocation;

public interface I_Allocator {

    void init(int size);
    void kill();

    int allocate(int size);
    void free(int position, int size);

    void clear();

}