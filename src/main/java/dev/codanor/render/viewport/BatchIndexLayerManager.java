package dev.codanor.render.viewport;

import dev.codanor.render.render_object.Mesh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class BatchIndexLayerManager {

    protected BatchIndexLayerManager() {
        _LAYERS = new HashMap<>();
        _MESHES = new HashMap<>();

        _LAYERS.put("STD", new BatchIndexLayer("STD"));
    }

    private final HashMap<String, BatchIndexLayer> _LAYERS;
    private final HashMap<Mesh, ArrayList<BatchIndexLayer>> _MESHES;

    public boolean bind(String layerName) {
        BatchIndexLayer layer;

        layer = _LAYERS.get(layerName);

        if (layer == null) return false;

        layer.bind();

        return true;
    }

    public boolean addLayer(String name, int size) {
        if (Objects.equals(name, "STD")) return false;
        if (_LAYERS.containsKey(name)) return false;

        _LAYERS.put(name, new BatchIndexLayer(name));

        return true;
    }
    public boolean rmvLayer(String name) {
        if (Objects.equals(name, "STD")) return false;

        return _LAYERS.remove(name) != null;
    }

    public boolean add(Mesh mesh, int verticesPosition, Collection<String> layerNames) {
        ArrayList<BatchIndexLayer> layers;

        layers = _MESHES.computeIfAbsent(mesh, k -> new ArrayList<>());

        if (layers.isEmpty()) if (!_LAYERS.get("std").add(mesh, verticesPosition, true)) {
            _MESHES.remove(mesh);

            return false;
        }

        for (String layerName : layerNames) {
            BatchIndexLayer layer;

            layer = _LAYERS.get(layerName);

            if (layer == null) continue;

            if (layers.contains(layer)) continue;

            if (!layer.add(mesh, verticesPosition, true)) {
                for (BatchIndexLayer meshLayer : layers) meshLayer.rmv(mesh);

                _MESHES.remove(mesh);

                return false;
            }

            layers.add(layer);
        }

        return true;
    }
    public boolean add(Mesh mesh, int verticesPosition) {
        ArrayList<BatchIndexLayer> layers;

        layers = _MESHES.computeIfAbsent(mesh, k -> new ArrayList<>());

        if (layers.isEmpty()) if (!_LAYERS.get("std").add(mesh, verticesPosition, true)) {
            _MESHES.remove(mesh);

            return false;
        }

        for (BatchIndexLayer layer : _LAYERS.values()) {
            if (layers.contains(layer)) continue;

            if (!layer.add(mesh, verticesPosition, true)) {
                for (BatchIndexLayer meshLayer : layers) meshLayer.rmv(mesh);

                _MESHES.remove(mesh);

                return false;
            }

            layers.add(layer);
        }

        return true;
    }

    public boolean rmv(Mesh mesh, Collection<String> layerNames) {
        ArrayList<BatchIndexLayer> layers, meshLayers;

        layers = new ArrayList<>();
        meshLayers = _MESHES.get(mesh);

        if (meshLayers == null) return false;

        for (String layerName : layerNames) {
            BatchIndexLayer layer;

            layer = _LAYERS.get(layerName);

            if (layer == null) continue;
            if (!meshLayers.contains(layer)) continue;

            layers.add(layer);
        }

        meshLayers.removeAll(layers);

        for (BatchIndexLayer layer : layers) layer.rmv(mesh);

        if (meshLayers.isEmpty()) {
            _MESHES.remove(mesh);

            _LAYERS.get("sts").rmv(mesh);
        }

        return true;
    }
    public boolean rmv(Mesh mesh) {
        ArrayList<BatchIndexLayer> layers;

        layers = _MESHES.remove(mesh);

        if (layers == null) return false;

        for (BatchIndexLayer layer : layers) layer.rmv(mesh);

        _LAYERS.get("std").rmv(mesh);

        return true;
    }

    public boolean contains(Mesh mesh) {
        return _MESHES.containsKey(mesh);
    }
    public Collection<String> getMeshLayers(Mesh mesh) {
        ArrayList<BatchIndexLayer> layers;
        ArrayList<String> names;

        layers = _MESHES.get(mesh);

        if (layers == null) return null;

        names = new ArrayList<>();

        for (BatchIndexLayer layer : layers) names.add(layer.getName());

        return names;
    }

    public boolean update(Mesh mesh, int verticesPosition) {
        ArrayList<BatchIndexLayer> layers;

        layers = _MESHES.get(mesh);

        if (layers == null) return false;

        if (!_LAYERS.get("std").update(mesh, verticesPosition, true)) {
            for (BatchIndexLayer rmvLayer : layers) rmvLayer.rmv(mesh);

            return false;
        }

        for (BatchIndexLayer layer : layers) {
            if (layer.update(mesh, verticesPosition, true)) continue;

            for (BatchIndexLayer rmvLayer : layers) rmvLayer.rmv(mesh);

            return false;
        }

        return true;
    }

}