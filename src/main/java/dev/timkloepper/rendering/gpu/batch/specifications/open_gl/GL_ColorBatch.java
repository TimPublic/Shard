package dev.timkloepper.rendering.gpu.batch.specifications.open_gl;

import dev.timkloepper.rendering.api.camera.Camera2D;
import dev.timkloepper.rendering.api.shader.Shader;
import dev.timkloepper.rendering.gpu.batch.allocation.I_Allocator;
import dev.timkloepper.rendering.mesh.specifications.ColorMesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class GL_ColorBatch extends A_GL_Batch<ColorMesh> {

    public GL_ColorBatch(I_Allocator vertexAllocator, I_Allocator indexAllocator, Shader shader) {
        super(vertexAllocator, indexAllocator, shader);
    }

    @Override
    protected void p_createVertexAttributes() {
        /* Coordinates */ glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * Float.BYTES, 0);
        /* Color */       glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * Float.BYTES, 2 * Float.BYTES);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    }

    @Override
    protected int p_getSize() {
        return 1000;
    }

    @Override
    protected void p_render(Camera2D camera) {
        glDrawElements(GL_TRIANGLES, p_getIndexCount(), GL_UNSIGNED_INT, 0);
    }

}