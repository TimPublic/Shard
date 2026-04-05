package dev.codanor.render.specs.shaders;

import dev.codanor.render.render_object.Mesh;

import java.util.HashMap;

public interface I_GeometryShader {

    void bind();
    void unbind();

    HashMap<String, Integer> getLocationNames();
    boolean supportsMesh(Mesh mesh);

}