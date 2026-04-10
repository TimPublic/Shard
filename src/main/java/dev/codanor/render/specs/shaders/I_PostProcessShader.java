package dev.codanor.render.specs.shaders;

import java.util.Map;

public interface I_PostProcessShader {

    void bind();
    void unbind();

    Map<String, Integer> getImageUniforms();
    Map<String, Integer> getImageOutputs();

}