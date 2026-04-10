package dev.codanor.render.specs;

import dev.codanor.engine.Worker;
import dev.codanor.render.specs.buffers.I_FrameBuffer;
import dev.codanor.render.specs.buffers.I_Image;

import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.opengl.GL30.*;

public class GL_FrameBuffer implements I_FrameBuffer {

    public GL_FrameBuffer(I_FrameBuffer.FrameBufferRecipe recipe) {
        _images = new HashMap<>();

        Worker.GLFW.instruct(() -> _id = glGenFramebuffers());
    }

    private record ImageData(
            I_Image image,
            int slot,
            String name
    ) {}

    private int _id;

    private HashMap<Integer, ImageData> _images;

    @Override
    public void bind() {
        Worker.GLFW.instruct(() -> {
            glBindFramebuffer(GL_FRAMEBUFFER, _id);
        });
    }
    @Override
    public void unbind() {
        Worker.GLFW.instruct(() -> {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        });
    }

    @Override
    public void clear() {
        bind();

        Worker.GLFW.instruct(() -> {
            glClear(GL_COLOR_BUFFER_BIT);
        });
    }

    @Override
    public void attachImage(String name, I_Image image, int unit) {
        _images.remove(unit);
        Worker.GLFW.instruct(() -> {
            glBindFramebuffer(GL_FRAMEBUFFER, _id);
            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_COLOR_ATTACHMENT0 + unit,
                    GL_TEXTURE_2D,
                    image.getId(),
                    0
            );
        });

        _images.put(unit, new ImageData(
                image,
                unit,
                name
        ));
    }
    @Override
    public void attachDepth(String name, I_Image image) {
        _images.remove(-1);

        Worker.GLFW.instruct(() -> {
            glBindFramebuffer(GL_FRAMEBUFFER, _id);
            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_DEPTH_ATTACHMENT,
                    GL_TEXTURE_2D,
                    image.getId(),
                    0
            );
        });

        _images.put(-1, new ImageData(
                image,
                -1,
                name
        ));
    }

    @Override
    public I_Image getImage(String name) {
        for (ImageData data : _images.values()) if (Objects.equals(data.name, name)) return data.image;

        return null;
    }
    @Override
    public HashMap<String, I_Image> getImages() {
        HashMap<String, I_Image> imageMap;

        imageMap = new HashMap<>();

        for (ImageData data : _images.values()) imageMap.put(data.name, data.image);

        return imageMap;
    }

}