package dev.codanor.render.specs.buffers;

import dev.codanor.render.specs.RenderSpecs;
import dev.codanor.render.objects.frame_buffer.I_FrameBuffer;
import org.joml.Vector2i;

public class RenderBuffers {

    public static I_IntBuffer createInt() {
        return RenderSpecs.BUFFER.get().intBufferFactory().apply(RenderSpecs.BATCH.get().vertexBufferSize());
    }
    public static I_FloatBuffer createFloat() {
        return RenderSpecs.BUFFER.get().floatBufferFactory().apply(RenderSpecs.BATCH.get().vertexBufferSize());
    }

    public static I_FrameBuffer createFrame(Vector2i layout) {
        return RenderSpecs.BUFFER.get().frameBufferFactory().apply(layout);
    }

}