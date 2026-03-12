package dev.timkloepper.rendering.batch;


import dev.timkloepper.rendering.RenderSystem;


public class BatchSystemInteractor {


    // <editor-fold desc="-+- PARAMETERS -+-">

    // <editor-fold desc="NON FINALS">

    private int _currentSceneId;
    private RenderSystem _currentRenderSystem;

    // </editor-fold>
    // <editor-fold desc="FINALS">



    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- INTERACTION LOGIC -+-">

    private BatchSystem h_getSystem() {
        return _currentRenderSystem.getBatchSystem(_currentSceneId);
    }

    // </editor-fold>


}