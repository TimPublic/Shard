package dev.timkloepper.render.shader;

import dev.timkloepper.render.mesh.Mesh;

import java.util.HashMap;

public interface I_Shader {

    void bind();
    void unbind();

    HashMap<String, Integer> getLocationNames();
    boolean supportsMesh(Mesh mesh);

}