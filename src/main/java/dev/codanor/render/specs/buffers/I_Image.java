package dev.codanor.render.specs.buffers;

import org.joml.Vector2i;

public interface I_Image {

    enum FORMAT {
        RGBA8,
        RGBA16F,
        DEPTH,
    }

    record ImageRecipe(
            Vector2i layout,
            FORMAT format
    ) {}

    void bindForReading(int unit);
    void unbindFromReading(int unit);

    int getWidth();
    int getHeight();

    int getId();

}