package dev.codanor.render.pipe;

import dev.codanor.render.specs.buffers.I_FrameBuffer;
import dev.codanor.render.specs.buffers.I_Image;
import dev.codanor.render.specs.buffers.RenderBuffers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class A_RenderPass {

    protected A_RenderPass(int id) {
        _ID = id;
    }

    protected void p_init() {
        p_buffer = RenderBuffers.createFrame(getOutputs());
    }

    protected I_FrameBuffer p_buffer;

    private final int _ID;

    public abstract Collection<String> getInputs();
    public abstract Map<String, Integer> getOutputs();

    public HashMap<String, I_Image> getImages() {
        return p_buffer.getImages();
    }

    public abstract void render(HashMap<String, I_Image> inputs);

    public int getId() {
        return _ID;
    }

}