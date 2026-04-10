package dev.codanor.render.pipe;

import dev.codanor.render.specs.buffers.I_Image;
import dev.codanor.render.specs.pipes.Pipes;
import dev.codanor.render.specs.shaders.I_PostProcessShader;
import dev.codanor.render.specs.shaders.Shaders;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PostProcessPass extends A_RenderPass {

    public PostProcessPass(int id, String shaderPath) {
        super(id);

        _SHADER = Shaders.createPostProcess(shaderPath);
        p_init();
    }

    private final I_PostProcessShader _SHADER;

    @Override
    public Collection<String> getInputs() {
        return _SHADER.getImageUniforms().keySet();
    }
    @Override
    public Map<String, Integer> getOutputs() {
        return _SHADER.getImageOutputs();
    }

    @Override
    public void render(HashMap<String, I_Image> inputs) {
        HashMap<I_Image, Integer> imageUnits;
        int unit;

        imageUnits = new HashMap<>();
        unit = 0;

        p_buffer.bind();
        p_buffer.clear();
        _SHADER.bind();

        for (String inputName : inputs.keySet()) {
            I_Image image;

            (image = inputs.get(inputName)).bindForReading(unit);

            imageUnits.put(image, unit);

            unit++;
        }

        Pipes.renderQuad();

        for (I_Image image : imageUnits.keySet()) {
            image.unbindFromReading(imageUnits.get(image));
        }

        _SHADER.unbind();
        p_buffer.unbind();
    }


}