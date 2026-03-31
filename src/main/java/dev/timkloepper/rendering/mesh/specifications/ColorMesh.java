package dev.timkloepper.rendering.mesh.specifications;

import dev.timkloepper.rendering.mesh.A_Mesh;
import org.joml.Vector2d;

import java.awt.*;
import java.util.ArrayList;

public class ColorMesh extends A_Mesh {

    /*
    x, y, r, g, b, a | ...
     */

    public ColorMesh(float[] vertices, int[] indices) {
        super(vertices, indices);
    }
    public ColorMesh(Color color, ArrayList<Vector2d> points, int[] indices) {
        super(new float[points.size() * 5], indices);

        int pointsIndex;

        pointsIndex = 0;
        for (int index = 0; index < points.size();) {
            p_vertices[index] = (float) points.get(pointsIndex).x;
            p_vertices[index + 1] = (float) points.get(pointsIndex).y;

            p_vertices[index + 2] = color.getRed();
            p_vertices[index + 3] = color.getGreen();
            p_vertices[index + 4] = color.getBlue();
            p_vertices[index + 5] = color.getAlpha();

            index += 6;
            pointsIndex++;
        }
    }

    @Override
    public int getVertexSize() {
        return 6;
    }

}