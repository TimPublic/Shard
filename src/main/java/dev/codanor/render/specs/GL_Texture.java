package dev.codanor.render.specs;

import dev.codanor.engine.Worker;
import dev.codanor.render.specs.buffers.I_Image;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_RGBA16F;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GL_Texture implements I_Image {

    public GL_Texture(I_Image.ImageRecipe recipe) {
        Worker.GLFW.instruct(() -> {
            _id = glGenTextures();

            _width = recipe.layout().x;
            _height = recipe.layout().y;

            glBindTexture(GL_TEXTURE_2D, _id);

            switch (recipe.format()) {
                case RGBA8 -> glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGBA8,
                        _width,
                        _height,
                        0,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        NULL
                );
                case RGBA16F -> glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGBA16F,
                        _width,
                        _height,
                        0,
                        GL_RGBA,
                        GL_FLOAT,
                        NULL
                );
                case DEPTH -> glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_DEPTH_COMPONENT,
                        _width,
                        _height,
                        0,
                        GL_DEPTH_COMPONENT,
                        GL_UNSIGNED_BYTE,
                        NULL
                );
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        });
    }

    private int _id;

    private int _width, _height;

    @Override
    public void bindForReading(int unit) {
        Worker.GLFW.instruct(() -> {
            glActiveTexture(GL_TEXTURE0 + unit);
            glBindTexture(GL_TEXTURE_2D, _id);
        });
    }
    @Override
    public void unbindFromReading(int unit) {
        Worker.GLFW.instruct(() -> {
            glActiveTexture(GL_TEXTURE0 + unit);
            glBindTexture(GL_TEXTURE_2D, 0);
        });
    }

    @Override
    public int getWidth() {
        return _width;
    }
    @Override
    public int getHeight() {
        return _height;
    }

    @Override
    public int getId() {
        return _id;
    }

}