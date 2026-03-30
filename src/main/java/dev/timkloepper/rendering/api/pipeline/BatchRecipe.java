package dev.timkloepper.rendering.api.pipeline;

import dev.timkloepper.rendering.gpu.batch.allocation.I_Allocator;
import dev.timkloepper.rendering.api.shader.Shader;

import java.util.function.Supplier;

public class BatchRecipe {

    public BatchRecipe(Shader shader, Supplier<I_Allocator> vertexAllocatorSupplier, Supplier<I_Allocator> indexAllocatorSupplier) {
        this.shader = shader;

        this.vertexAllocatorSupplier = vertexAllocatorSupplier;
        this.indexAllocatorSupplier = indexAllocatorSupplier;
    }

    public Shader shader;

    public final Supplier<I_Allocator> vertexAllocatorSupplier, indexAllocatorSupplier;

}