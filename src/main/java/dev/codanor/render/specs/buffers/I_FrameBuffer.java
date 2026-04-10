package dev.codanor.render.specs.buffers;

import org.joml.Vector2i;

import java.util.HashMap;

public interface I_FrameBuffer {

    record FrameBufferRecipe(
            Vector2i layout
    ) {}

    void bind();
    void unbind();

    void clear();

    void attachImage(String name, I_Image image, int unit);
    void attachDepth(String name, I_Image image);

    I_Image getImage(String name);
    HashMap<String, I_Image> getImages();

}