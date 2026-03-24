package dev.timkloepper.rendering;


import dev.timkloepper.rendering.batch.BatchSystem;
import dev.timkloepper.rendering.mesh.A_Mesh;
import dev.timkloepper.rendering.shader.Shader;


public class BatchSystemInteractor {

    protected BatchSystemInteractor(BatchSystem system) {
        _SYSTEM = system;
    }

    private final BatchSystem _SYSTEM;

    public boolean add(A_Mesh mesh, Shader shader) {
        return _SYSTEM.add(mesh, shader);
    }
    public boolean add(A_Mesh mesh) {
        return _SYSTEM.add(mesh);
    }

    public boolean rmv(A_Mesh mesh) {
        return _SYSTEM.rmv(mesh);
    }

    public boolean update(A_Mesh mesh) {
        return _SYSTEM.update(mesh);
    }
    public boolean update(A_Mesh mesh, Shader shader) {
        return _SYSTEM.update(mesh, shader);
    }

}