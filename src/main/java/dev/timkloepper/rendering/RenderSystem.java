package dev.timkloepper.rendering;


import dev.timkloepper.rendering.batch.BatchSystem;
import dev.timkloepper.rendering.batch.BatchSystemInteractor;
import dev.timkloepper.update.I_UpdateLoop;
import dev.timkloepper.util.Indexer;
import dev.timkloepper.visual_container.A_Scene;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RenderSystem implements I_UpdateLoop {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public RenderSystem() {
        _SCENE_ID_INDEXER = new Indexer();

        _SYSTEM_PER_ID = new HashMap<>();

        _RENDERING_QUEUE = new ConcurrentLinkedQueue<>();
    }

    // </editor-fold>


    // <editor-fold desc="-+- PARAMETERS -+-">

    // <editor-fold desc="NON FINALS">



    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final Indexer _SCENE_ID_INDEXER;

    private final HashMap<Integer, BatchSystem> _SYSTEM_PER_ID;

    private final ConcurrentLinkedQueue<Integer> _RENDERING_QUEUE;

    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- SCENE MANAGEMENT -+-">

    public int addScene(A_Scene scene) {
        int id;

        _SYSTEM_PER_ID.put((id = _SCENE_ID_INDEXER.get()), new BatchSystem());

        return id;
    }
    public boolean rmvScene(int id) {
        if (_SYSTEM_PER_ID.remove(id) == null) return false;

        _SCENE_ID_INDEXER.free(id);

        return true;
    }

    public BatchSystem getBatchSystem(int id) {
        return _SYSTEM_PER_ID.get(id);
    }

    // </editor-fold>
    // <editor-fold desc="-+- INTERACTOR MANAGEMENT -+-">

    public BatchSystemInteractor getInteractor(int sceneId) {
        return new BatchSystemInteractor();
    }

    // </editor-fold>
    // <editor-fold desc="-+- RENDERING -+-">

    public void addToQueue(int id) {
        _RENDERING_QUEUE.add(id);
    }

    // </editor-fold>
    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        while (!_RENDERING_QUEUE.isEmpty()) {
            _SYSTEM_PER_ID.get(_RENDERING_QUEUE.poll()).render();
        }
    }

    // </editor-fold>


}