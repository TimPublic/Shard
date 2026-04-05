package dev.timkloepper.main;

import dev.timkloepper.render.render_object.Material;
import dev.timkloepper.render.render_object.Mesh;
import dev.timkloepper.render.render_object.RenderObject;
import dev.timkloepper.render.render_object.RenderTransform;
import dev.timkloepper.render.specs.shaders.MockShader;
import org.joml.Vector2d;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        RenderObject ro;
        Mesh firstMesh;

        ro = new RenderObject(
                new RenderTransform(0, 0, 0),
                new Material(new MockShader()),
                (firstMesh = new Mesh(List.of(new Vector2d(0, 0))))
        );

        System.out.println(ro.getVersion());

        ro.getMesh().addAttrib("test", new float[]{0, 0});

        System.out.println(ro.getVersion());

        ro.setMesh(new Mesh(List.of(new Vector2d(0, 0))));

        System.out.println(ro.getVersion());

        firstMesh.addAttrib("another test", new float[]{1, 1, 1, 1});

        System.out.println(ro.getVersion());
    }

}