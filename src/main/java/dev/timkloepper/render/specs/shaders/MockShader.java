package dev.timkloepper.render.specs.shaders;

import dev.timkloepper.render.render_object.Mesh;

import java.util.HashMap;

public class MockShader implements I_GeometryShader {

    @Override
    public void bind() {
        System.out.println("Bound mock shader!");
    }
    @Override
    public void unbind() {
        System.out.println("Unbound mock shader!");
    }

    @Override
    public HashMap<String, Integer> getLocationNames() {
        return new HashMap<>();
    }
    @Override
    public boolean supportsMesh(Mesh mesh) {
        return false;
    }

}