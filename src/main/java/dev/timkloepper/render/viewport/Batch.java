package dev.timkloepper.render.viewport;

import dev.timkloepper.render.render_object.Material;
import dev.timkloepper.render.render_object.RenderObject;
import dev.timkloepper.render.specs.RenderSpecs;
import dev.timkloepper.render.specs.batches.Batches;
import dev.timkloepper.render.specs.buffers.I_FloatBuffer;
import dev.timkloepper.render.specs.allocation.I_Allocator;
import dev.timkloepper.render.specs.buffers.RenderBuffers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class Batch {

    public Batch(Batch.BatchRecipe recipe) {
        RenderSpecs.BatchSpecs specs;

        specs = RenderSpecs.BATCH.get();

        _killed = false;

        _INDEX_BUFFER_SIZE = specs.indexBufferSize();
        _INDEX_LAYER_MANAGER = new BatchIndexLayerManager();

        recipe.layers.forEach((layer) -> _INDEX_LAYER_MANAGER.addLayer(layer, _INDEX_BUFFER_SIZE));

        _VERTEX_BUFFER = RenderBuffers.createFloat();

        _VERTEX_ALLOCATOR = Batches.createVertexAllocatorInitialized();

        p_MATERIAL = recipe.MATERIAL;
        p_VIEWPORT = recipe.VIEWPORT;

        _ON_MATERIAL_REMOVED_CALLBACK = p_MATERIAL.addChangeCallback(this::_onMaterialChanged);

        _OBJECTS = new HashMap<>();
    }

    public void kill(boolean reroute) {
        _killed = true;

        _ON_MATERIAL_REMOVED_CALLBACK.run();

        if (reroute) _OBJECTS.keySet().forEach(p_VIEWPORT::addObj);
        _OBJECTS.clear();
    }

    public record BatchRecipe(
            Material MATERIAL,
            Viewport VIEWPORT,
            Collection<String> layers
    ) {}
    public record BatchRenderData(
            Camera2D camera2D
    ) {}

    private static class ObjInfo {

        public ObjInfo(int version, int verticesIndex, int verticesSize) {
            this.version = version;

            INFO = new Batch.MeshInfo(verticesIndex, verticesSize);
        }

        public int version;

        public final Batch.MeshInfo INFO;

    }
    private static class MeshInfo {

        public MeshInfo(int verticesPosition, int verticesSize) {
            this.verticesPosition = verticesPosition;
            this.verticesSize = verticesSize;
        }

        public int verticesPosition, verticesSize;

    }

    private boolean _killed;

    private final int _INDEX_BUFFER_SIZE;
    private final BatchIndexLayerManager _INDEX_LAYER_MANAGER;

    private final I_Allocator _VERTEX_ALLOCATOR;

    private final I_FloatBuffer _VERTEX_BUFFER;

    protected final Material p_MATERIAL;
    protected final Viewport p_VIEWPORT;

    private final Runnable _ON_MATERIAL_REMOVED_CALLBACK;

    private final HashMap<RenderObject, Batch.ObjInfo> _OBJECTS;

    protected void p_render(String layer, Camera2D camera) {
        _updateObjects();

        _INDEX_LAYER_MANAGER.bind(layer);

        RenderSpecs.BATCH.get().drawCall().accept(new BatchRenderData(
                camera
        ));
    }

    protected boolean p_bindIndexBuffer(String layer) {
        return _INDEX_LAYER_MANAGER.bind(layer);
    }
    protected void p_bindVertexBuffer() {
        _VERTEX_BUFFER.bind();
    }

    public boolean addLayer(String layer) {
        return _INDEX_LAYER_MANAGER.addLayer(layer, _INDEX_BUFFER_SIZE);
    }
    public boolean rmvLayer(String layer) {
        return _INDEX_LAYER_MANAGER.rmvLayer(layer);
    }

    public boolean addObj(RenderObject obj, Collection<String> layers) {
        float[] vertices;

        int verticesIndex;

        if (_killed) return false;

        vertices = obj.getMesh().getData();
        if ((verticesIndex = _allocateVertices(vertices, true)) == -1) return false;

        if (!_INDEX_LAYER_MANAGER.add(obj.getMesh(), verticesIndex, layers)) {
            _VERTEX_ALLOCATOR.free(verticesIndex, vertices.length);

            _VERTEX_BUFFER.bind();
            _VERTEX_BUFFER.rmv(verticesIndex, vertices.length);
            _VERTEX_BUFFER.unbind();

            return false;
        }

        // --- //

        Batch.ObjInfo info;

        info = new Batch.ObjInfo(
                obj.getVersion(),
                verticesIndex, vertices.length
        );

        _OBJECTS.put(obj, info);

        return true;
    }
    public boolean addObj(RenderObject obj) {
        float[] vertices;

        int verticesIndex;

        if (_killed) return false;

        vertices = obj.getMesh().getData();
        if ((verticesIndex = _allocateVertices(vertices, true)) == -1) return false;

        if (!_INDEX_LAYER_MANAGER.add(obj.getMesh(), verticesIndex)) {
            _VERTEX_ALLOCATOR.free(verticesIndex, vertices.length);

            _VERTEX_BUFFER.bind();
            _VERTEX_BUFFER.rmv(verticesIndex, vertices.length);
            _VERTEX_BUFFER.unbind();

            return false;
        }

        // --- //

        Batch.ObjInfo info;

        info = new Batch.ObjInfo(
                obj.getVersion(),
                verticesIndex, vertices.length
        );

        _OBJECTS.put(obj, info);

        return true;
    }

    public boolean rmvObj(RenderObject obj, Collection<String> layers) {
        Batch.ObjInfo info;
        int verticesIndex, verticesSize;

        info = _OBJECTS.get(obj);

        if (info == null) return false;

        _INDEX_LAYER_MANAGER.rmv(obj.getMesh(), layers);

        if (!_INDEX_LAYER_MANAGER.contains(obj.getMesh())) {
            _VERTEX_ALLOCATOR.free(info.INFO.verticesPosition, info.INFO.verticesSize);

            _VERTEX_BUFFER.bind();
            _VERTEX_BUFFER.rmv(info.INFO.verticesPosition, info.INFO.verticesSize);
            _VERTEX_BUFFER.unbind();

            _OBJECTS.remove(obj);
        }

        return true;
    }
    public boolean rmvObj(RenderObject obj) {
        Batch.ObjInfo info;
        int verticesIndex, verticesSize;

        info = _OBJECTS.remove(obj);

        if (info == null) return false;

        _INDEX_LAYER_MANAGER.rmv(obj.getMesh());

        _VERTEX_ALLOCATOR.free((verticesIndex = info.INFO.verticesPosition), (verticesSize = info.INFO.verticesSize));

        _VERTEX_BUFFER.bind();
        _VERTEX_BUFFER.rmv(verticesIndex, verticesSize);
        _VERTEX_BUFFER.unbind();

        return true;
    }

    public boolean contains(RenderObject obj) {
        return _OBJECTS.containsKey(obj);
    }
    public Collection<String> getObjectLayers(RenderObject obj) {
        return _INDEX_LAYER_MANAGER.getMeshLayers(obj.getMesh());
    }

    private int _allocateVertices(float[] vertices, boolean rebuild) {
        int index;

        if ((index = _VERTEX_ALLOCATOR.allocate(vertices.length)) == -1) {
            if (!rebuild) return -1;

            p_rebuild();

            if ((index = _VERTEX_ALLOCATOR.allocate(vertices.length)) == -1) return -1;
        }

        // --- //

        _VERTEX_BUFFER.bind();
        _VERTEX_BUFFER.add(index, vertices);
        _VERTEX_BUFFER.unbind();

        return index;
    }

    protected void p_rebuild() {
        ArrayList<RenderObject> objCopy;

        objCopy = new ArrayList<>(_OBJECTS.keySet());

        objCopy.forEach(this::rmvObj);
        objCopy.forEach(this::addObj);
    }

    private void _onMaterialChanged() {
        ArrayList<RenderObject> rerouteObjects;
        ArrayList<String> layers;

        rerouteObjects = new ArrayList<>();

        for (RenderObject obj : _OBJECTS.keySet()) {
            if (!p_MATERIAL.supports(obj.getMesh())) {
                rerouteObjects.add(obj);
            }
        }

        layers = new ArrayList<>();

        for (RenderObject obj : rerouteObjects) {
            layers.clear();
            layers.addAll(getObjectLayers(obj));

            rmvObj(obj);
            p_VIEWPORT.addObj(obj, layers);
        }
    }
    private void _updateObjects() {
        Iterator<RenderObject> iterator;
        ArrayList<RenderObject> reinsertObjects, removeObjects, rerouteObjects;

        reinsertObjects = new ArrayList<>();
        removeObjects = new ArrayList<>();
        rerouteObjects = new ArrayList<>();

        for (RenderObject obj : _OBJECTS.keySet()) {
            if (!obj.hasChanged(_OBJECTS.get(obj).version)) continue;

            _OBJECTS.get(obj).version = obj.getVersion();

            if (!p_MATERIAL.supports(obj.getMesh())) {
                rerouteObjects.add(obj);

                continue;
            }

            _updateObjectMesh(obj, reinsertObjects, removeObjects);
        }

        removeObjects.forEach(this::rmvObj);

        reinsertObjects.forEach(this::rmvObj);
        for (RenderObject obj : reinsertObjects) if (!addObj(obj)) throw new OutOfMemoryError("[BATCH ERROR] : Batch could not fit this mesh!");

        // --- //

        ArrayList<String> layers;

        layers = new ArrayList<>();

        for (RenderObject obj : rerouteObjects) {
            layers.clear();
            layers.addAll(getObjectLayers(obj));

            rmvObj(obj);
            p_VIEWPORT.addObj(obj, layers);
        }
    }

    private void _updateObjectMesh(RenderObject obj, ArrayList<RenderObject> reinsertObjects, ArrayList<RenderObject> removeObjects) {
        float[] vertices;
        Batch.MeshInfo meshInfo;
        boolean verticesSizeChanged;

        vertices = obj.getMesh().getData();

        meshInfo = _OBJECTS.get(obj).INFO;

        verticesSizeChanged = vertices.length != meshInfo.verticesSize;

        if (verticesSizeChanged) {
            reinsertObjects.add(obj);

            return;
        }

        if (!_INDEX_LAYER_MANAGER.update(obj.getMesh(), meshInfo.verticesPosition)) {
            removeObjects.add(obj);

            throw new OutOfMemoryError("[BATCH ERROR] : Batch could not fit this mesh!");
        }

        _VERTEX_BUFFER.bind();
        _VERTEX_BUFFER.add(meshInfo.verticesPosition, vertices);
        _VERTEX_BUFFER.unbind();
    }

}