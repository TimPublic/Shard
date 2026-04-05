package dev.codanor.render.viewport;

import dev.codanor.render.render_object.Mesh;
import dev.codanor.render.specs.allocation.I_Allocator;
import dev.codanor.render.specs.batches.Batches;
import dev.codanor.render.specs.buffers.I_IntBuffer;
import dev.codanor.render.specs.buffers.RenderBuffers;

import java.util.ArrayList;
import java.util.HashMap;

public class BatchIndexLayer {

    public BatchIndexLayer(String name) {
        _BUFFER = RenderBuffers.createInt();
        _ALLOCATOR = Batches.createIndexAllocatorInitialized();

        _NAME = name;

        _MESH_INFOS = new HashMap<>();
    }

    protected static class MeshLayerInfo {

        public MeshLayerInfo(int position, int size, int verticesPosition) {
            this.position = position;
            this.size = size;
        }

        public int position, size;
        public int verticesPosition;

    }

    private final I_IntBuffer _BUFFER;
    private final I_Allocator _ALLOCATOR;

    private final String _NAME;

    private final HashMap<Mesh, MeshLayerInfo> _MESH_INFOS;

    public boolean add(Mesh mesh, int verticesPosition, boolean rebuild) {
        int[] indices;
        int index;

        indices = mesh.getIndices();

        if ((index = _ALLOCATOR.allocate(indices.length)) == -1) {
            if (!rebuild) return false;

            _MESH_INFOS.remove(mesh);

            rebuild();

            if ((index = _ALLOCATOR.allocate(indices.length)) == -1) return false;
        }

        _MESH_INFOS.put(mesh, new MeshLayerInfo(
                index,
                indices.length,
                verticesPosition
        ));

        // --- //

        int[] relativeIndices;
        int vertexSize;

        relativeIndices = new int[indices.length];
        vertexSize = mesh.getVertexSize();

        for (int currentIndex = 0; currentIndex < indices.length; currentIndex++) {
            relativeIndices[currentIndex] = indices[currentIndex] + (verticesPosition / vertexSize);
        }

        _BUFFER.bind();
        _BUFFER.add(index, relativeIndices);
        _BUFFER.unbind();

        return true;
    }
    public void rmv(Mesh mesh) {
        MeshLayerInfo info;

        info = _MESH_INFOS.remove(mesh);

        if (info == null) return;

        _ALLOCATOR.free(info.position, info.size);

        _BUFFER.bind();
        _BUFFER.rmv(info.position, info.size);
        _BUFFER.unbind();
    }

    public boolean update(Mesh mesh, int verticesPosition, boolean rebuild) {
        MeshLayerInfo info;
        int[] indices;
        int[] relativeIndices;
        int vertexSize;

        info = _MESH_INFOS.get(mesh);
        indices = mesh.getIndices();

        relativeIndices = new int[indices.length];
        vertexSize = mesh.getVertexSize();

        info.verticesPosition = verticesPosition;

        for (int currentIndex = 0; currentIndex < indices.length; currentIndex++) {
            relativeIndices[currentIndex] = indices[currentIndex] + (verticesPosition / vertexSize);
        }

        _BUFFER.bind();

        // --- //

        if (indices.length == info.size) {
            _BUFFER.add(info.position, relativeIndices);
            _BUFFER.unbind();

            return true;
        }

        // --- //

        int index;

        _ALLOCATOR.free(info.position, info.size);
        _BUFFER.rmv(info.position, info.size);

        if ((index = _ALLOCATOR.allocate(info.size)) == -1) {
            if (!rebuild) return false;

            _MESH_INFOS.remove(mesh);

            rebuild();

            if ((index = _ALLOCATOR.allocate(info.size)) == -1) return false;
        }

        info.position = index;
        info.size = indices.length;

        _BUFFER.add(index, relativeIndices);
        _BUFFER.unbind();

        return true;
    }

    public void rebuild() {
        _ALLOCATOR.clear();
        _BUFFER.clear(true);

        for (Mesh mesh : new ArrayList<>(_MESH_INFOS.keySet())) {
            int[] indices;
            int index;
            int[] relativeIndices;
            int vertexSize;

            indices = mesh.getIndices();

            relativeIndices = new int[indices.length];
            vertexSize = mesh.getVertexSize();

            for (int currentIndex = 0; currentIndex < indices.length; currentIndex++) {
                relativeIndices[currentIndex] = indices[currentIndex] + (_MESH_INFOS.get(mesh).verticesPosition / vertexSize);
            }

            index = _ALLOCATOR.allocate(indices.length);

            _MESH_INFOS.get(mesh).position = index;

            _BUFFER.add(index, relativeIndices);
        }
    }

    public void bind() {
        _BUFFER.bind();
    }
    public void unbind() {
        _BUFFER.unbind();
    }

    public String getName() {
        return _NAME;
    }

}