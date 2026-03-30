package dev.timkloepper.rendering.gpu.batch;

import dev.timkloepper.rendering.api.camera.Camera2D;
import dev.timkloepper.rendering.gpu.batch.allocation.I_Allocator;
import dev.timkloepper.rendering.gpu.objects.buffer.I_FloatBuffer;
import dev.timkloepper.rendering.gpu.objects.buffer.I_IntBuffer;
import dev.timkloepper.rendering.gpu.objects.vertex_array.I_VertexArray;
import dev.timkloepper.rendering.mesh.A_Mesh;
import dev.timkloepper.rendering.api.shader.Shader;

import java.util.*;

public abstract class A_Batch<T extends A_Mesh> {

    // <editor-fold desc="-+- CREATION -+-">

    public A_Batch(I_Allocator vertexAllocator, I_Allocator indexAllocator, Shader shader) {
        int size;

        size = p_getSize();

        _vertexAllocator = vertexAllocator;
        _indexAllocator = indexAllocator;

        _SHADER = shader;

        p_VERTEX_BUFFER = p_createVertexBuffer(size);
        p_INDEX_BUFFER = p_createIndexBuffer(size);
        p_VERTEX_ARRAY = p_createVertexArray(size);

        _INFOS = new HashMap<>();

        _vertexAllocator.init(size);
        _indexAllocator.init(size);
    }

    protected abstract I_FloatBuffer p_createVertexBuffer(int size);
    protected abstract I_IntBuffer p_createIndexBuffer(int size);
    protected abstract I_VertexArray p_createVertexArray(int size);

    protected abstract int p_getSize();

    // </editor-fold>

    // <editor-fold desc="-+- CLASSES -+-">

    private class MeshInfo {

        public MeshInfo(int verticesPos, int verticesSize, int indicesPos, int indicesSize) {
            this.verticesPos = verticesPos;
            this.verticesSize = verticesSize;
            this.indicesPos = indicesPos;
            this.indicesSize = indicesSize;
        }

        public int verticesPos, indicesPos;
        public int verticesSize, indicesSize;

    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private I_Allocator _vertexAllocator, _indexAllocator;

    private int _indexCount;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final Shader _SHADER;

    protected final I_FloatBuffer p_VERTEX_BUFFER;
    protected final I_IntBuffer p_INDEX_BUFFER;

    protected final I_VertexArray p_VERTEX_ARRAY;

    private final HashMap<T, MeshInfo> _INFOS;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- MESH MANAGEMENT -+-">

    public boolean addMesh(T mesh) {
        float[] vertices;
        int verticesIndex;
        int[] indices;
        int indicesIndex;
        MeshInfo info;

        if (mesh == null) return false;
        if (_INFOS.containsKey(mesh)) return false;

        verticesIndex = h_allocateVertices((vertices = mesh.getVertices()));
        if (verticesIndex == -1) return false;

        indicesIndex = h_allocateIndices((indices = mesh.getIndices()), verticesIndex, mesh.getVertexSize());
        if (indicesIndex == -1) return false;

        info = new MeshInfo(verticesIndex, vertices.length, indicesIndex, indices.length);
        _INFOS.put(mesh, info);

        _indexCount += info.indicesSize;

        return true;
    }
    public boolean rmvMesh(T mesh) {
        MeshInfo info;

        info = _INFOS.remove(mesh);

        if (info == null) return false;

        p_VERTEX_BUFFER.bind();
        p_VERTEX_BUFFER.rmv(info.verticesPos, info.verticesPos + info.verticesSize);
        p_VERTEX_BUFFER.unbind();

        p_INDEX_BUFFER.bind();
        p_INDEX_BUFFER.rmv(info.indicesPos, info.indicesPos + info.indicesSize);
        p_INDEX_BUFFER.unbind();

        _vertexAllocator.free(info.verticesPos, info.verticesSize);
        _indexAllocator.free(info.indicesPos, info.indicesSize);

        _indexCount -= info.indicesSize;

        return true;
    }

    private boolean _tryUpdateMesh(T mesh) {
        MeshInfo info;

        if (!mesh.hasChanged()) return true;

        info = _INFOS.get(mesh);

        if (info == null) return false;

        // Relocation of vertices requires a full rebuild.
        if (info.verticesSize != mesh.getVertices().length) {
            rmvMesh(mesh);

            return addMesh(mesh);
        }

        p_VERTEX_BUFFER.bind();
        p_INDEX_BUFFER.bind();

        // Just relocating the indices;
        if (info.indicesSize != mesh.getIndices().length) {
            int index;

            _indexCount -= mesh.getIndices().length;

            p_INDEX_BUFFER.rmv(info.indicesPos, info.indicesPos + info.indicesSize);
            _indexAllocator.free(info.indicesPos, info.indicesSize);

            index = h_allocateIndices(mesh.getIndices(), info.verticesPos, mesh.getVertexSize());

            // Also deleting the vertices.
            if (index == -1) {
                p_VERTEX_BUFFER.rmv(info.verticesPos, info.verticesPos + info.verticesSize);
                _vertexAllocator.free(info.verticesPos, info.verticesSize);

                _INFOS.remove(mesh);

                return false;
            }

            p_INDEX_BUFFER.add(index, mesh.getIndices());

            info.indicesPos = index;
            info.indicesSize = mesh.getIndices().length;

            _indexCount = info.indicesSize;

            return true;
        }

        // Just changing data of the same size - no relocation needed.
        p_VERTEX_BUFFER.add(info.verticesPos, mesh.getVertices());
        p_INDEX_BUFFER.add(info.indicesPos, mesh.getIndices());

        p_VERTEX_BUFFER.unbind();
        p_INDEX_BUFFER.unbind();

        return true;
    }

    public boolean contains(T mesh) {
        return _INFOS.containsKey(mesh);
    }

    private int h_allocateVertices(float[] vertices) {
        int index;

        index = _vertexAllocator.allocate(vertices.length);

        if (index < 0) {
            rebuild();

            index = _vertexAllocator.allocate(vertices.length);
        }
        if (index < 0) return -1;

        p_VERTEX_BUFFER.bind();
        p_VERTEX_BUFFER.add(index, vertices);
        p_VERTEX_BUFFER.unbind();

        return index;
    }
    private int h_allocateIndices(int[] indices, int offset, int vertexSize) {
        int[] relativeIndices;
        int index, counter;

        relativeIndices = Arrays.copyOf(indices, indices.length);
        for (int currentIndex = 0; currentIndex < indices.length; currentIndex++) {
            relativeIndices[currentIndex] = indices[currentIndex] + (offset / vertexSize);
        }

        index = _indexAllocator.allocate(indices.length);

        if (index < 0) {
            rebuildIndices();

            index = _indexAllocator.allocate(indices.length);
        }
        if (index < 0) return -1;

        p_INDEX_BUFFER.bind();
        p_INDEX_BUFFER.add(index, relativeIndices);
        p_INDEX_BUFFER.unbind();

        return index;
    }

    // </editor-fold>
    // <editor-fold desc="-+- MEMORY MANAGEMENT -+-">

    public void rebuild() {
        for (T mesh : new HashSet<>(_INFOS.keySet())) {
            rmvMesh(mesh);
            addMesh(mesh);
        }
    }
    public void rebuildIndices() {
        int index;

        p_INDEX_BUFFER.clear(true);
        _indexAllocator.clear();

        for (T mesh : _INFOS.keySet()) {
            index = h_allocateIndices(mesh.getIndices(), _INFOS.get(mesh).verticesPos, mesh.getVertexSize());

            _INFOS.get(mesh).indicesPos = index;
        }
    }

    public void clear() {
        for (T mesh : new HashSet<>(_INFOS.keySet())) rmvMesh(mesh);
    }

    public void setVertexAllocator(I_Allocator allocator) {
        _vertexAllocator = allocator;

        rebuild();
    }
    public void setIndexAllocator(I_Allocator allocator) {
        _indexAllocator = allocator;

        rebuildIndices();
    }

    protected int p_getIndexCount() {
        return _indexCount;
    }

    // </editor-fold>
    // <editor-fold desc="-+- RENDER LOGIC -+-">

    public void render(Camera2D camera) {
        for (T mesh : _INFOS.keySet()) if (!_tryUpdateMesh(mesh)) throw new OutOfMemoryError("[BATCH ERROR] : Cannot fit updated mesh in the buffer!");

        _SHADER.use();
        p_VERTEX_ARRAY.bind();

        p_render(camera);

        p_VERTEX_ARRAY.unbind();
        _SHADER.unuse();
    }
    protected abstract void p_render(Camera2D camera);

    // </editor-fold>

}