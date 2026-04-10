package dev.codanor.render.specs;

import dev.codanor.engine.Worker;

import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.*;

public class GL_Quad {

    private static int s_vao, s_vbo;

    private static boolean _initialized;

    public static void init() {
        Worker.GLFW.instruct(() -> {
            float[] vertices;

            if (_initialized) return;
            _initialized = true;

            vertices = new float[]{
                    -1f, 1f,
                    -1f, -1f,
                    1f, 1f,
                    1f, -1f,
            };

            s_vao = glGenVertexArrays();
            s_vbo = glGenBuffers();

            glBindVertexArray(s_vao);

            glBindBuffer(GL_ARRAY_BUFFER, s_vbo);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);

            glBindVertexArray(0);
        });
    }

    public static void draw() {
        Worker.GLFW.instruct(() -> {
            glBindVertexArray(s_vao);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            glBindVertexArray(0);
        });
    }

}