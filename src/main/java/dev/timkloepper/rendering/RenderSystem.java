package dev.timkloepper.rendering;


import dev.timkloepper.rendering.batch.BatchSystem;

import java.lang.ref.WeakReference;
import java.util.*;


public class RenderSystem {

    // <editor-fold desc="-+- CREATION -+-">

    public RenderSystem() {
        _nextFreeId = 0;

        _INFORMATION = new HashMap<>();
        _FREE_IDS = new ArrayDeque<>();

        _QUEUE = new ArrayDeque<>();
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private int _nextFreeId;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final HashMap<Integer, IdInformation> _INFORMATION;
    private final ArrayDeque<Integer> _FREE_IDS;

    private final ArrayDeque<Integer> _QUEUE;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- BATCH MANAGEMENT -+-">

    public int register() {
        int id;

        id = h_getFreeId();
        _INFORMATION.put(id, new IdInformation());

        return id;
    }
    public boolean delete(int id) {
        if (_INFORMATION.remove(id) == null) return false;

        return _FREE_IDS.add(id);
    }

    public int switchTo(int id, RenderSystem system) {
        IdInformation information;
        int newId;

        information = _INFORMATION.remove(id);
        if (information == null) return -1;

        newId = system.h_getFromOtherSystem(information);

        return newId;
    }
    public int switchFrom(int oldId, RenderSystem system) {
        IdInformation information;
        int newId;

        information = system._INFORMATION.remove(oldId);
        if (information == null) return -1; // TODO : Could possibly also do normal register but a silent fail would be alarming.

        newId = h_getFreeId();
        _INFORMATION.put(newId, information);

        return newId;
    }

    private int h_getFromOtherSystem(IdInformation information) {
        int id;

        id = h_getFreeId();
        _INFORMATION.put(id, information);

        return id;
    }

    public BatchSystemInteractor interactor(int id) {
        IdInformation information;
        BatchSystem system;

        information = _INFORMATION.get(id);
        if (information == null) return null;

        return new BatchSystemInteractor(information.BATCH_SYSTEM);
    }

    private int h_getFreeId() {
        Integer id;

        id = _FREE_IDS.poll();
        if (id == null) id = _nextFreeId++;

        return id;
    }

    // </editor-fold>
    // <editor-fold desc="-+- RENDER LOGIC -+-">

    public void queue(int id) {
        _QUEUE.add(id);
    }

    public void render() {
        while (!_QUEUE.isEmpty()) _INFORMATION.get(_QUEUE.poll()).BATCH_SYSTEM.render();
    }

    // </editor-fold>

}

class IdInformation {


    public IdInformation() {
        BATCH_SYSTEM = BatchSystem.create();
        INTERACTORS = new ArrayList<>();
    }


    public final BatchSystem BATCH_SYSTEM;
    public final ArrayList<WeakReference<BatchSystemInteractor>> INTERACTORS;


    public void updateInteractors() {
        Iterator<WeakReference<BatchSystemInteractor>> iterator;

        iterator = INTERACTORS.iterator();

        while (iterator.hasNext()) if (iterator.next().get() == null) iterator.remove();
    }


}