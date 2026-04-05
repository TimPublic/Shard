package dev.codanor.render.viewport;

import dev.codanor.render.objects.frame_buffer.I_FrameBuffer;
import dev.codanor.render.render_object.Material;
import dev.codanor.render.render_object.RenderObject;
import dev.codanor.render.specs.batches.Batches;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BatchContainer {

    public BatchContainer(Viewport viewport) {
        _VIEWPORT = viewport;

        _BATCHES = new HashMap<>();
        _LAYERS = new HashSet<>();
    }

    private final Viewport _VIEWPORT;

    private final HashMap<Material, Batch> _BATCHES;
    private final HashSet<String> _LAYERS;

    public boolean addObj(RenderObject obj, Collection<String> layers) {
        Material material;
        Batch batch;

        material = obj.getMaterial();
        batch = _BATCHES.get(material);

        if (batch == null) {
            Batch.BatchRecipe recipe;

            recipe = new Batch.BatchRecipe(material, _VIEWPORT, _LAYERS);

            _BATCHES.put(material, (batch = Batches.createBatch(recipe)));
        }

        return batch.addObj(obj, layers);
    }
    public boolean addObj(RenderObject obj) {
        Material material;
        Batch batch;

        material = obj.getMaterial();
        batch = _BATCHES.get(material);

        if (batch == null) {
            Batch.BatchRecipe recipe;

            recipe = new Batch.BatchRecipe(material, _VIEWPORT, List.of("STD"));

            _BATCHES.put(material, (batch = Batches.createBatch(recipe)));
        }

        return batch.addObj(obj);
    }

    public boolean rmvObj(RenderObject obj, Collection<String> layers) {
        Material material;
        Batch batch;

        material = obj.getMaterial();
        batch = _BATCHES.get(material);

        if (batch == null) return false;

        return batch.rmvObj(obj, layers);
    }
    public boolean rmvObj(RenderObject obj) {
        Material material;
        Batch batch;

        material = obj.getMaterial();
        batch = _BATCHES.get(material);

        if (batch == null) return false;

        return batch.rmvObj(obj);
    }

    public boolean addLayer(String layer) {
        for (Batch batch : _BATCHES.values()) if (!batch.addLayer(layer)) return false;

        _LAYERS.add(layer);

        return true;
    }
    public boolean rmvLayer(String layer) {
        for (Batch batch : _BATCHES.values()) if (!batch.rmvLayer(layer)) return false;

        _LAYERS.remove(layer);

        return true;
    }

    public boolean contains(RenderObject obj) {
        Material material;
        Batch batch;

        material = obj.getMaterial();
        batch = _BATCHES.get(material);

        if (batch == null) return false;

        return batch.contains(obj);
    }

    public Collection<String> getObjectLayers(RenderObject obj) {
        Material material;
        Batch batch;

        material = obj.getMaterial();
        batch = _BATCHES.get(material);

        if (batch == null) return null;

        return batch.getObjectLayers(obj);
    }

    public void render(String layer, Camera2D camera, I_FrameBuffer frameBuffer) {
        frameBuffer.bind();
        for (Batch batch : _BATCHES.values()) batch.p_render(layer, camera);
        frameBuffer.unbind();
    }

}