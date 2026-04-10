package dev.codanor.render.specs.buffers;

import dev.codanor.render.specs.RenderSpecs;

import java.util.HashMap;
import java.util.Map;

public class RenderBuffers {

    public static I_IntBuffer createInt() {
        return RenderSpecs.BUFFER.get().intBufferFactory().apply(RenderSpecs.BATCH.get().indexBufferSize());
    }
    public static I_FloatBuffer createFloat() {
        return RenderSpecs.BUFFER.get().floatBufferFactory().apply(RenderSpecs.BATCH.get().vertexBufferSize());
    }

    public static I_FrameBuffer createFrame(Map<String, Integer> images) {
        HashMap<String, I_Image> imagesMap;

        imagesMap = new HashMap<>();

        for (String name : images.keySet()) {
            imagesMap.put(name, createImage());
        }

        // --- //

        I_FrameBuffer buffer;

        buffer = RenderSpecs.BUFFER.get().frameBufferFactory().apply(new I_FrameBuffer.FrameBufferRecipe(
                RenderSpecs.BUFFER.get().imageLayout()
        ));

        for (String imageName : imagesMap.keySet()) {
            buffer.attachImage(imageName, imagesMap.get(imageName), images.get(imageName));
        }

        return buffer;
    }
    public static I_Image createImage() {
        return RenderSpecs.BUFFER.get().imageFactory().apply(new I_Image.ImageRecipe(
                RenderSpecs.BUFFER.get().imageLayout(),
                RenderSpecs.BUFFER.get().imageFormat()
        ));
    }

    public static void bindMainFrame() {
        RenderSpecs.BUFFER.get().bindMainFrameCall().run();
    }
    public static void unbindMainFrame() {
        RenderSpecs.BUFFER.get().unbindMainFrameCall().run();
    }

    public static void clearMainFrame() {
        RenderSpecs.BUFFER.get().clearMainFrameCall().run();
    }

}