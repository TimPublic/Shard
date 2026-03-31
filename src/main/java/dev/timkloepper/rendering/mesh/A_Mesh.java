package dev.timkloepper.rendering.mesh;

import dev.timkloepper.rendering.mesh.exception.InvalidIndicesException;
import dev.timkloepper.rendering.mesh.exception.InvalidVerticesException;

public abstract class A_Mesh {

    public A_Mesh(float[] vertices, int[] indices) {
        _changed = false;

        p_vertices = vertices;
        p_indices = indices;

        validate();
    }

    private boolean _changed;

    protected float[] p_vertices;
    protected int[] p_indices;

    public float[] getVertices() {
        return p_vertices;
    }
    public int[] getIndices() {
        return p_indices;
    }

    public void setVertices(float[] vertices) {
        _changed = true;

        p_vertices = vertices;
    }
    public void setIndices(int[] indices) {
        _changed = true;

        p_indices = indices;
    }

    public boolean hasChanged() {
        return _changed;
    }
    public void resetChanged() {
        _changed = false;
    }

    public void validate() {
        if (p_vertices.length % getVertexSize() != 0) throw new InvalidVerticesException("");
        if (p_indices.length % 3 != 0) throw new InvalidIndicesException("");
    }

    public abstract int getVertexSize();

}