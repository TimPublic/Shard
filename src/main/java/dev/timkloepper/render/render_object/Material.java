package dev.timkloepper.render.render_object;

import dev.timkloepper.render.specs.shaders.I_GeometryShader;

import java.util.ArrayList;

public class Material {

    public Material(I_GeometryShader shader) {
        _ON_CHANGED_CALLBACKS = new ArrayList<>();

        if (!setShader(shader)) throw new RuntimeException();
    }

    private I_GeometryShader _shader;

    private final ArrayList<Runnable> _ON_CHANGED_CALLBACKS;

    public boolean setShader(I_GeometryShader shader) {
        if (shader == null) return false;

        _shader = shader;

        h_notifyChange();

        return true;
    }
    public I_GeometryShader getShader() {
        return _shader;
    }

    public boolean supports(Mesh mesh) {
        return _shader.supportsMesh(mesh);
    }

    public Runnable addChangeCallback(Runnable callback) {
        _ON_CHANGED_CALLBACKS.add(callback);

        return () -> _ON_CHANGED_CALLBACKS.remove(callback);
    }

    private void h_notifyChange() {
        for (Runnable callback : _ON_CHANGED_CALLBACKS) callback.run();
    }

}