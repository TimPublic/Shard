package dev.timkloepper.rendering.api.pipeline;

import dev.timkloepper.rendering.gpu.batch.A_Batch;
import dev.timkloepper.rendering.mesh.A_Mesh;

import java.util.function.Function;

public class BatchFactory {

    public BatchFactory(Function<BatchRecipe, A_Batch<? extends A_Mesh>> callback) {
        this._callback = callback;
    }

    private Function<BatchRecipe, A_Batch<? extends A_Mesh>> _callback;

    public boolean setCallback(Function<BatchRecipe, A_Batch<? extends A_Mesh>> callback) {
        if (_callback == null) return false;

        _callback = callback;

        return true;
    }

    public A_Batch<? extends A_Mesh> factor(BatchRecipe recipe) {
        return _callback.apply(recipe);
    }

}