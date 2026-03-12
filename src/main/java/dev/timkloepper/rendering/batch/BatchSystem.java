package dev.timkloepper.rendering.batch;


import dev.timkloepper.rendering.shader.Shader;

import java.util.HashMap;


public class BatchSystem {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public BatchSystem() {
        _BATCHES = new HashMap<>();
    }

    // </editor-fold>


    // <editor-fold desc="-+- PARAMETERS -+-">

    // <editor-fold desc="NON FINALS">



    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final HashMap<Shader, A_Batch> _BATCHES;

    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- RENDERING -+-">

    public void render() {
        _BATCHES.values().forEach((batch) -> batch.render(null));
    }

    // </editor-fold>


}