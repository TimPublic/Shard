package dev.timkloepper.visual_container;


import dev.timkloepper.entity_component_system.entity_system.EntitySystem;

import java.util.ArrayList;


public abstract class A_Scene extends A_VisualContainer {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public A_Scene(int width, int height) {
        super(width, height, true);

        _SYSTEMS = new _Systems();

        _LAYERED_SCENES = new ArrayList<>();
    }

    // </editor-fold>


    // <editor-fold desc="-+- CLASSES -+-">

    private class _Systems {

        public _Systems() {
            ECS = new EntitySystem();
        }

        public final EntitySystem ECS;

        public void update(double delta) {
            ECS.update(delta);
        }

    }

    // </editor-fold>
    // <editor-fold desc="-+- PARAMETERS -+-">

    // <editor-fold desc="NON FINALS">



    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final _Systems _SYSTEMS;

    private final ArrayList<A_Scene> _LAYERED_SCENES;

    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- WINDOW INTERACTION -+-">

    protected abstract void p_addedToWindow(Window window);
    protected abstract void p_removedFromWindow(Window window);

    // </editor-fold>
    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        for (int index = _LAYERED_SCENES.size() - 1; index >= 0; index--) {
            _LAYERED_SCENES.get(index).update(delta);
        }

        _SYSTEMS.update(delta);
    }

    // </editor-fold>
    // <editor-fold desc="-+- SCENE MANAGEMENT -+-">

    public void addSceneFront(A_Scene scene) {
        _LAYERED_SCENES.addFirst(scene);
    }
    public void addSceneBack(A_Scene scene) {
        _LAYERED_SCENES.addLast(scene);
    }
    public void addScene(A_Scene scene, int zIndex) {
        _LAYERED_SCENES.add(zIndex, scene);
    }

    // </editor-fold>


}