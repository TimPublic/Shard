package dev.timkloepper.render.specs.shaders;

import dev.timkloepper.render.render_object.Mesh;

import java.util.HashMap;

public interface I_GeometryShader {

    void bind();
    void unbind();

    HashMap<String, Integer> getLocationNames();
    boolean supportsMesh(Mesh mesh);

}