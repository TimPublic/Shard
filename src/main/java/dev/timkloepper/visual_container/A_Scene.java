package dev.timkloepper.visual_container;


import dev.timkloepper.engine.Shard;
import dev.timkloepper.entity_component_system.entity_system.EntitySystem;
import dev.timkloepper.event_system.A_Port;
import dev.timkloepper.event_system.EventSystem;
import dev.timkloepper.event_system.I_EventSystemHolder;
import dev.timkloepper.rendering.RenderSystem;
import dev.timkloepper.rendering.batch.BatchSystemInteractor;

import java.util.ArrayList;


public abstract class A_Scene extends A_VisualContainer implements I_EventSystemHolder {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public A_Scene(int width, int height) {
        super(width, height, true);

        _parentScene = null;

        _SYSTEMS = new _Systems();
        _PRE_PORTS = new _PrePorts();

        _LAYERED_SCENES = new ArrayList<>();

        Shard.getEventSystem().addPort(_PRE_PORTS.enginePort);
    }

    // </editor-fold>


    // <editor-fold desc="-+- CLASSES -+-">

    private class _Systems {

        public _Systems() {
            renderSystemId = -1;
            INTERACTOR = null;

            ECS = new EntitySystem();
            EVENT_SYSTEM = new EventSystem();
        }

        public int renderSystemId;
        public BatchSystemInteractor INTERACTOR;

        public final EntitySystem ECS;
        public final EventSystem EVENT_SYSTEM;

        public void update(double delta) {
            ECS.update(delta);
        }

    }
    private class _PrePorts {


        public _PrePorts() {
            parentScenePort = null;
            enginePort = null;
        }


        public A_Port parentScenePort, enginePort;


    }

    // </editor-fold>
    // <editor-fold desc="-+- PARAMETERS -+-">

    // <editor-fold desc="NON FINALS">

    private A_Scene _parentScene;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final _Systems _SYSTEMS;
    private final _PrePorts _PRE_PORTS;

    private final ArrayList<A_Scene> _LAYERED_SCENES;

    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- WINDOW INTERACTION -+-">

    protected final void p_changedRenderSystem(RenderSystem renderSystem) {
        _SYSTEMS.INTERACTOR = renderSystem.getInteractor((_SYSTEMS.renderSystemId = renderSystem.addScene(this)));
    }
    protected void p_removedFromRenderSystem(RenderSystem renderSystem) {
        renderSystem.rmvScene(_SYSTEMS.renderSystemId);
        _SYSTEMS.renderSystemId = -1;
        _SYSTEMS.INTERACTOR = null;
    }

    protected abstract void p_onAddedToWindow(Window window);
    protected abstract void p_onRemovedFromWindow(Window window);

    // </editor-fold>
    // <editor-fold desc="-+- SCENE INTERACTION -+-">

    public void addScene(A_Scene scene, int layer) {
        _LAYERED_SCENES.add(layer, scene);

        scene._onParentSceneChanged(this);
    }
    public void addSceneFront(A_Scene scene) {
        _LAYERED_SCENES.addFirst(scene);

        scene._onParentSceneChanged(this);
    }
    public void addSceneBack(A_Scene scene) {
        _LAYERED_SCENES.addLast(scene);

        scene._onParentSceneChanged(this);
    }

    public boolean rmvScene(A_Scene scene) {
        if (!_LAYERED_SCENES.remove(scene)) return false;

        scene._removeParentScene();
        return true;
    }

    private void _onParentSceneChanged(A_Scene newScene) {
        _removeParentScene();

        _parentScene = newScene;
        _parentScene.getEventSystem().addPort(_PRE_PORTS.parentScenePort);
    }
    private void _removeParentScene() {
        if (_parentScene == null) return;

        _parentScene.rmvScene(this);
        _parentScene.getEventSystem().rmvPort(_PRE_PORTS.parentScenePort);

        _parentScene = null;
    }

    // </editor-fold>

    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        for (int index = _LAYERED_SCENES.size() - 1; index >= 0; index--) {
            _LAYERED_SCENES.get(index).update(delta);
        }

        _SYSTEMS.update(delta);
        _SYSTEMS.INTERACTOR.queueRender();
    }

    // </editor-fold>

    // <editor-fold desc="-+- EVENT MANAGEMENT -+-">

    public EventSystem getEventSystem() {
        return _SYSTEMS.EVENT_SYSTEM;
    }

    // </editor-fold>


}