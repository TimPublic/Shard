package dev.timkloepper.rendering.gpu.batch.allocation.specifications;

import java.util.HashMap;

public class FlatLookupAllocator extends FlatAllocator {

    public FlatLookupAllocator() {
        _FREE_INDICES = new HashMap<>();
    }

    @Override
    public void kill() {
        super.kill();

        _FREE_INDICES.clear();
    }

    private int _size;
    private int _nextFreeIndex;

    private final HashMap<Integer, Integer> _FREE_INDICES;

    @Override
    public int allocate(int size) {
        int index;

        index = super.allocate(size);

        if (index >= 0) return index;

        for (Integer currentIndex : _FREE_INDICES.keySet()) {
            if (_FREE_INDICES.get(currentIndex) < size) continue;

            _FREE_INDICES.remove(currentIndex);

            return currentIndex;
        }

        return index;
    }
    @Override
    public void free(int position, int size) {
        _FREE_INDICES.put(position, size);
    }

    @Override
    public void clear() {
        super.clear();

        _FREE_INDICES.clear();
    }

}