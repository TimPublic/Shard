package dev.timkloepper.rendering.mesh;


import dev.timkloepper.rendering.mesh.exception.InvalidIndicesException;
import dev.timkloepper.rendering.mesh.exception.InvalidVerticesException;


public abstract class A_Mesh {


    public A_Mesh(float[] vertices, int[] indices, int zIndex) {
        if (!_areValidVertices(vertices)) throw new InvalidVerticesException("[MESH ERROR] : The size of the vertex array is not valid!\n"
        + "|-> Vertices Amount : " + vertices.length + "\n"
        + "|-> Required Vertex Size : " + getVertexSize() + "(or multiple)");
        if (!_areValidIndices(indices)) throw new InvalidIndicesException("[MESH ERROR] : The size of the index array is not valid!\n"
        + "|-> Indices Amount : " + indices.length + "\n"
        + "|-> Required Index Size : " + REQUIRED_INDEX_SIZE + " (or multiple)");

        this.vertices = vertices;
        this.indices = indices;

        this.zIndex = zIndex;
    }


    public float[] vertices;
    public int[] indices;

    public int zIndex;


    public final int REQUIRED_INDEX_SIZE = 3;


    private boolean _areValidIndices(int[] indices) {
        return (indices.length % 3 == 0);
    }
    private boolean _areValidVertices(float[] vertices) {
        return (vertices.length % getVertexSize() == 0);
    }


    public abstract int getVertexSize();


}