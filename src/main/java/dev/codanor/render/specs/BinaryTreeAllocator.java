package dev.codanor.render.specs;

import dev.codanor.render.specs.allocation.I_Allocator;

public class BinaryTreeAllocator implements I_Allocator {

    private class BinaryAllocationNode {

        public BinaryAllocationNode(int position, int size) {
            this.position = position;
            this.size = size;
        }

        public int position, size;

        public BinaryAllocationNode lower, higher;

        public void add(BinaryAllocationNode node) {
            if (node == null || node == this) return;

            if (node.size < size) {
                if (lower == null) lower = node;
                else lower.add(node);
            }
            else {
                if (higher == null) higher = node;
                else higher.add(node);
            }
        }
        public void rmv(BinaryAllocationNode node) {
            if (node == null) return;

            if (lower == node) lower = null;
            else if (higher == node) higher = null;

            add(node.lower);
            add(node.higher);
        }

        public BinaryAllocationNode allocate(int size) {
            if (size > this.size) {
                if (higher == null) return null;

                return higher.allocate(size);
            }
            if (size < this.size) {
                BinaryAllocationNode result;

                if (lower == null) return this;

                result = lower.allocate(size);

                if (result != null) return result;
            }

            return this;
        }

    }

    @Override
    public void init(int size) {
        kill();

        _size = size;

        _root = new BinaryAllocationNode(0, _size);
    }
    @Override
    public void kill() {
        _root = null;
    }

    private int _size;

    private BinaryAllocationNode _root;

    @Override
    public int allocate(int size) {
        BinaryAllocationNode node;
        int index;
        BinaryAllocationNode lower, higher;

        if (_root == null) return -1;

        node = _root.allocate(size);

        if (node == null) return -1;

        if (node == _root) {
            if (node.lower != null) {
                _root = node.lower;
                _root.add(node.higher);
            }
            else {
                _root = node.higher;
            }
        }
        else {
            _root.rmv(node);
        }

        index = node.position;
        node.position += size;
        node.size -= size;

        if (node.size > 0) _root.add(node); // Automatically stops, if the root is this node.

        return index;
    }
    @Override
    public void free(int position, int amount) {
        BinaryAllocationNode node;

        node = new BinaryAllocationNode(position, amount);

        if (_root == null) _root = node;
        else _root.add(node);
    }

    @Override
    public void clear() {
        _root = null;
    }

}