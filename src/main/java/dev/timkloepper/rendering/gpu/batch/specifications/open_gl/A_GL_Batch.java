package dev.timkloepper.rendering.gpu.batch.specifications.open_gl;

import dev.timkloepper.rendering.gpu.batch.A_Batch;
import dev.timkloepper.rendering.gpu.batch.allocation.I_Allocator;
import dev.timkloepper.rendering.gpu.objects.buffer.I_FloatBuffer;
import dev.timkloepper.rendering.gpu.objects.buffer.I_IntBuffer;
import dev.timkloepper.rendering.gpu.objects.buffer.specifications.open_gl.GL_ElementBuffer;
import dev.timkloepper.rendering.gpu.objects.buffer.specifications.open_gl.GL_VertexBuffer;
import dev.timkloepper.rendering.gpu.objects.vertex_array.I_VertexArray;
import dev.timkloepper.rendering.gpu.objects.vertex_array.specifications.open_gl.GL_VertexArray;
import dev.timkloepper.rendering.mesh.A_Mesh;
import dev.timkloepper.rendering.api.shader.Shader;

public abstract class A_GL_Batch<T extends A_Mesh> extends A_Batch<T> {

    public A_GL_Batch(I_Allocator vertexAllocator, I_Allocator indexAllocator, Shader shader) {
        super(vertexAllocator, indexAllocator, shader);
    }

    @Override
    protected I_FloatBuffer p_createVertexBuffer(int size) {
        return new GL_VertexBuffer(size);
    }
    @Override
    protected I_IntBuffer p_createIndexBuffer(int size) {
        return new GL_ElementBuffer(size);
    }
    @Override
    protected I_VertexArray p_createVertexArray(int size) {
        GL_VertexArray vertexArray;

        vertexArray = new GL_VertexArray();
        vertexArray.bind();

        p_INDEX_BUFFER.bind();
        p_VERTEX_BUFFER.bind();

        p_createVertexAttributes();

        vertexArray.unbind();

        p_VERTEX_BUFFER.unbind();
        p_INDEX_BUFFER.unbind();

        return vertexArray;
    }

    protected abstract void p_createVertexAttributes();

}