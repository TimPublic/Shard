package dev.codanor.render.specs.batches;

import dev.codanor.render.specs.RenderSpecs;
import dev.codanor.render.viewport.Batch;
import dev.codanor.render.specs.allocation.I_Allocator;

public class Batches {

    public static Batch createBatch(Batch.BatchRecipe recipe) {
        return new Batch(recipe);
    }

    public static I_Allocator createIndexAllocatorInitialized() {
        I_Allocator allocator;

        (allocator = RenderSpecs.BATCH.get().allocatorFactory().get()).init(RenderSpecs.BATCH.get().indexBufferSize());

        return allocator;
    }
    public static I_Allocator createVertexAllocatorInitialized() {
        I_Allocator allocator;

        (allocator = RenderSpecs.BATCH.get().allocatorFactory().get()).init(RenderSpecs.BATCH.get().vertexBufferSize());

        return allocator;
    }

    public static void render(Batch.BatchRenderData data) {
        RenderSpecs.BATCH.get().drawCall().accept(data);
    }

}