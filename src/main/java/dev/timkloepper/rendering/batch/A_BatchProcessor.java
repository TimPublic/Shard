package dev.timkloepper.rendering.batch;


import dev.timkloepper.rendering.camera.Camera2D;
import dev.timkloepper.rendering.mesh.A_Mesh;
import dev.timkloepper.rendering.shader.Shader;

import java.util.HashMap;


public abstract class A_BatchProcessor<T extends A_Mesh> {

    // <editor-fold desc="-+- CREATION -+-">

    public A_BatchProcessor() {
        _BATCHES = new HashMap<>();
        _MESHES = new HashMap<>();

        _STD_SHADER = p_provideStandardShader();
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">
    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final HashMap<Shader, A_Batch<T>> _BATCHES;
    private final HashMap<T, Shader> _MESHES;

    private final Shader _STD_SHADER;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- MESH MANAGEMENT -+-">

    public boolean add(T mesh, Shader shader) {
        A_Batch<T> batch;

        if (_MESHES.containsKey(mesh)) return false;

        batch = _BATCHES.get(shader);
        if (batch == null) _BATCHES.put(shader, (batch = p_createBatch(shader)));

        _MESHES.put(mesh, shader);

        return batch.addMesh(mesh);
    }
    public boolean add(T mesh) {
        return add(mesh, _STD_SHADER);
    }

    public boolean rmv(T mesh) {
        Shader shader;

        shader = _MESHES.remove(mesh);
        if (shader == null) return false;

        return _BATCHES.get(shader).rmvMesh(mesh);
    }

    public boolean update(T mesh, Shader shader) {
        A_Batch<T> batch;

        batch = _BATCHES.get(shader);
        if (batch == null) return false;

        _MESHES.put(mesh, shader);

        batch.updateMesh(mesh);

        return batch.contains(mesh);
    }
    public boolean update(T mesh) {
        Shader shader;

        shader = _MESHES.get(mesh);
        if (shader == null) return false;

        return update(mesh, shader);
    }

    public boolean contains(T mesh) {
        return _MESHES.containsKey(mesh);
    }

    // </editor-fold>
    // <editor-fold desc="-+- ABSTRACT LOGIC -+-">

    protected abstract A_Batch<T> p_createBatch(Shader shader);
    protected abstract Shader p_provideStandardShader();
    protected abstract Class<T> p_getProcessedMeshClass();

    // </editor-fold>
    // <editor-fold desc="-+- RENDER LOGIC -+-">

    protected void p_render() {
        for (A_Batch<? extends A_Mesh> batch : _BATCHES.values()) batch.render(new Camera2D());
    }

    // </editor-fold>

}