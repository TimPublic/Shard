package dev.timkloepper.rendering.mesh;

import dev.timkloepper.rendering.mesh.exception.InvalidIndicesException;
import dev.timkloepper.rendering.mesh.exception.InvalidVerticesException;

public abstract class A_Mesh {

    public A_Mesh(float[] vertices, int[] indices) {
        validate();

        _changed = false;

        _vertices = vertices;
        _indices = indices;

        validate();
    }

    private boolean _changed;

    private float[] _vertices;
    private int[] _indices;

    public float[] getVertices() {
        return _vertices;
    }
    public int[] getIndices() {
        return _indices;
    }

    public void setVertices(float[] vertices) {
        _changed = true;

        _vertices = vertices;
    }
    public void setIndices(int[] indices) {
        _changed = true;

        _indices = indices;
    }

    public boolean hasChanged() {
        return _changed;
    }
    public void resetChanged() {
        _changed = false;
    }

    public void validate() {
        if (_vertices.length % getVertexSize() != 0) throw new InvalidVerticesException("");
        if (_indices.length % 3 != 0) throw new InvalidIndicesException("");
    }

    public abstract int getVertexSize();

}