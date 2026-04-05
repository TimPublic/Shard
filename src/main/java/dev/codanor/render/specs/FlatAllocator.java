package dev.codanor.render.specs;

import dev.codanor.render.specs.allocation.I_Allocator;

public class FlatAllocator implements I_Allocator {

    @Override
    public void init(int size) {
        kill();

        _size = size;
        _nextFreeIndex = 0;
    }
    @Override
    public void kill() {
        _size = 0;
        _nextFreeIndex = 0;
    }

    private int _size;
    private int _nextFreeIndex;

    @Override
    public int allocate(int size) {
        int index;

        if (_nextFreeIndex + size >= _size) return -1;

        index = _nextFreeIndex;
        _nextFreeIndex += size;

        return index;
    }
    @Override
    public void free(int position, int size) {
        if (_nextFreeIndex - size != position) return;

        _nextFreeIndex -= size;
    }

    @Override
    public void clear() {
        _nextFreeIndex = 0;
    }

}