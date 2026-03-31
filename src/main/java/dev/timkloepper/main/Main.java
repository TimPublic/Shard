package dev.timkloepper.main;

import dev.timkloepper.engine.Shard;
import dev.timkloepper.rendering.api.pipeline.BatchFactory;
import dev.timkloepper.rendering.api.pipeline.BatchRecipe;
import dev.timkloepper.rendering.api.pipeline.RenderPass;
import dev.timkloepper.rendering.api.shader.Shader;
import dev.timkloepper.rendering.gpu.batch.allocation.specifications.FlatAllocator;
import dev.timkloepper.rendering.gpu.batch.specifications.open_gl.GL_ColorBatch;
import dev.timkloepper.rendering.mesh.specifications.ColorMesh;
import dev.timkloepper.visual_container.Window;

public class Main {

    public static void main(String[] args) {
        Window.create(700, 400, "Hello World!");

        Shard.run(false);

        RenderPass.setFactoryStd(ColorMesh.class, new BatchFactory(
                batchRecipe -> new GL_ColorBatch(batchRecipe.vertexAllocatorSupplier.get(), batchRecipe.indexAllocatorSupplier.get(), batchRecipe.shader)
        ));
        RenderPass.setRecipeStd(ColorMesh.class, new BatchRecipe(
                new Shader("assets/shaders/basic_color_shader_no_camera.glsl"),
                FlatAllocator::new,
                FlatAllocator::new
        ));
    }

}