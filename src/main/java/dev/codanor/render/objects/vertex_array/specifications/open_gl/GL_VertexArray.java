package dev.codanor.render.objects.vertex_array.specifications.open_gl;

import dev.codanor.render.objects.vertex_array.I_VertexArray;

import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class GL_VertexArray implements I_VertexArray {

    public GL_VertexArray() {
        _ID = glGenVertexArrays();
    }

    private final int _ID;

    @Override
    public void bind() {
        glBindVertexArray(_ID);
    }
    @Override
    public void unbind() {
        glBindVertexArray(0);
    }

}