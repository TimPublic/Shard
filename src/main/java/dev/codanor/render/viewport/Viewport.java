package dev.codanor.render.viewport;

import dev.codanor.render.pipe.RenderPipe;
import dev.codanor.render.render_object.RenderObject;
import dev.codanor.render.specs.buffers.I_FrameBuffer;
import dev.codanor.render.specs.buffers.RenderBuffers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Viewport {

    public Viewport() {
        _BATCH_CONTAINER = new BatchContainer(this);
        _CAMERA = new Camera2D();

        _LAYERS = new HashSet<>(List.of("STD"));

        _MAIN_FRAME_BUFFER = RenderBuffers.createFrame(Map.of(
                "fragmentOutput", 0
        ));

        _PIPES = new HashSet<>();
    }
    public Viewport(Camera2D camera) {
        _BATCH_CONTAINER = new BatchContainer(this);
        _CAMERA = camera;

        _LAYERS = new HashSet<>(List.of("STD"));

        _MAIN_FRAME_BUFFER = RenderBuffers.createFrame(Map.of(
                "fragmentOutput", 0
        ));

        _PIPES = new HashSet<>();
    }

    private final BatchContainer _BATCH_CONTAINER;
    private final Camera2D _CAMERA;

    private final HashSet<String> _LAYERS;

    private final I_FrameBuffer _MAIN_FRAME_BUFFER;

    private final HashSet<RenderPipe> _PIPES;

    protected BatchContainer p_getBatchContainer() {
        return _BATCH_CONTAINER;
    }

    protected Camera2D p_getCamera() {
        return _CAMERA;
    }

    public boolean addPipe(RenderPipe pipe) {
        return _PIPES.add(pipe);
    }
    public boolean rmvPipe(RenderPipe pipe) {
        return _PIPES.remove(pipe);
    }

    public boolean addLayer(String layer) {
        if (!_BATCH_CONTAINER.addLayer(layer)) return false;

        _LAYERS.add(layer);

        return true;
    }
    public boolean rmvLayer(String layer) {
        if (!_BATCH_CONTAINER.rmvLayer(layer)) return false;

        _LAYERS.remove(layer);

        return true;
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

    public void render() {
        RenderPipe.RenderData data;

        data = new RenderPipe.RenderData(
                _CAMERA,
                _BATCH_CONTAINER,
                _LAYERS
        );

        for (RenderPipe pipe : _PIPES) pipe.render(data);
    }

}