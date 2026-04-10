package dev.codanor.render.pipe;

import dev.codanor.render.specs.buffers.I_Image;
import dev.codanor.render.viewport.BatchContainer;
import dev.codanor.render.viewport.Camera2D;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputPass extends A_RenderPass {

    protected InputPass(int id) {
        super(id);

        p_init();
    }

    private Camera2D _camera;
    private BatchContainer _container;
    private Collection<String> _layers;

    @Override
    public Collection<String> getInputs() {
        return List.of();
    }
    @Override
    public Map<String, Integer> getOutputs() {
        return Map.of("test", 0);
    }

    public void prepare(Camera2D camera, BatchContainer container, Collection<String> layers) {
        _camera = camera;
        _container = container;
        _layers = layers;
    }
    @Override
    public void render(HashMap<String, I_Image> inputs) {
        for (String layer : _layers) _container.render(layer, _camera, p_buffer);
    }

}