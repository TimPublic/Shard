package dev.codanor.render.specs.shaders;

import dev.codanor.render.specs.RenderSpecs;

public class Shaders {

    public static I_GeometryShader createGeometry(String path) {
        return RenderSpecs.SHADER.get().geometryShaderFactory().apply(path);
    }

}