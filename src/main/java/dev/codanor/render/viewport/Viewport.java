package dev.codanor.render.viewport;

import dev.codanor.render.objects.frame_buffer.I_FrameBuffer;
import dev.codanor.render.render_object.RenderObject;
import dev.codanor.render.specs.buffers.RenderBuffers;
import dev.codanor.util.Indexer;

import java.util.Collection;

public class Viewport {

    public Viewport() {
        _PASS_ID_INDEXER = new Indexer();

        _BATCH_CONTAINER = new BatchContainer(this);
        _CAMERA = new Camera2D();

        _MAIN_FRAME_BUFFER = RenderBuffers.createFrame(null);
    }
    public Viewport(Camera2D camera) {
        _PASS_ID_INDEXER = new Indexer();

        _BATCH_CONTAINER = new BatchContainer(this);
        _CAMERA = camera;

        _MAIN_FRAME_BUFFER = RenderBuffers.createFrame(null);
    }

    private final Indexer _PASS_ID_INDEXER;

    private final BatchContainer _BATCH_CONTAINER;
    private final Camera2D _CAMERA;

    private final I_FrameBuffer _MAIN_FRAME_BUFFER;

    protected BatchContainer p_getBatchContainer() {
        return _BATCH_CONTAINER;
    }

    protected Camera2D p_getCamera() {
        return _CAMERA;
    }

    public boolean addLayer(String layer) {
        return _BATCH_CONTAINER.addLayer(layer);
    }

    public boolean rmvLayer(String layer) {
        return _BATCH_CONTAINER.rmvLayer(layer);
    }

    public boolean addObj(RenderObject obj, Collection<String> layers) {
        return _BATCH_CONTAINER.addObj(obj, layers);
    }

    public boolean addObj(RenderObject obj) {
        return _BATCH_CONTAINER.addObj(obj);
    }

    public boolean rmvObj(RenderObject obj, Collection<String> layers) {
        return _BATCH_CONTAINER.rmvObj(obj, layers);
    }

    public boolean rmvObj(RenderObject obj) {
        return _BATCH_CONTAINER.rmvObj(obj);
    }

    public boolean contains(RenderObject obj) {
        return _BATCH_CONTAINER.contains(obj);
    }

    public Collection<String> getObjectLayers(RenderObject obj) {
        return _BATCH_CONTAINER.getObjectLayers(obj);
    }

    public void render() {}

}