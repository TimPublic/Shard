package dev.codanor.main;

import dev.codanor.engine.Shard;
import dev.codanor.render.pipe.RenderPipe;
import dev.codanor.render.render_object.Material;
import dev.codanor.render.render_object.Mesh;
import dev.codanor.render.render_object.RenderObject;
import dev.codanor.render.render_object.RenderTransform;
import dev.codanor.render.specs.shaders.Shaders;
import dev.codanor.render.viewport.Viewport;
import dev.codanor.visual_container.Window;
import org.joml.Vector2d;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Window.create(100, 100, "Test!");
        Viewport viewport;
        Mesh mesh;
        RenderObject obj;

        viewport = new Viewport();

        mesh = new Mesh(List.of(new Vector2d(0, 0), new Vector2d(100, 0), new Vector2d(50, 100)));
        mesh.addAttrib("aColor", new float[]{245f, 40f, 145f, 0.8f});
        mesh.addTriangle(new int[]{0, 1, 2});

        obj = new RenderObject(
                new RenderTransform(0, 0, 0),
                new Material(Shaders.createGeometry("assets/shaders/basic_color_shader_no_camera.glsl")),
                mesh
        );

        viewport.addObj(obj);

        // --- //

        RenderPipe pipe;

        pipe = new RenderPipe("assets/shaders/test.glsl");
        pipe.compositionReadsFrom(pipe.INPUT_PASS);
        pipe.compile();

        viewport.addPipe(pipe);

        Shard.run(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            viewport.render();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}