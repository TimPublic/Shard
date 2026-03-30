package dev.timkloepper.visual_container;


import dev.timkloepper.engine.Shard;
import dev.timkloepper.entity_component_system.entity_system.EntitySystem;
import dev.timkloepper.event_system.A_Port;
import dev.timkloepper.event_system.EventSystem;
import dev.timkloepper.event_system.I_EventSystemHolder;
import dev.timkloepper.rendering.api.pipeline.Viewport;

import java.util.ArrayList;


public abstract class A_Scene extends A_VisualContainer implements I_EventSystemHolder {

    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public A_Scene(int width, int height) {
        super(width, height, true);

        _parentScene = null;

        p_SYSTEMS = new p_Systems();
        _PRE_PORTS = new p_PrePorts();

        _LAYERED_SCENES = new ArrayList<>();

        Shard.getEventSystem().addPort(_PRE_PORTS.enginePort);
    }

    // </editor-fold>


    // <editor-fold desc="-+- CLASSES -+-">

    protected class p_Systems {

        public p_Systems() {
            ECS = new EntitySystem();
            EVENT_SYSTEM = new EventSystem();
        }

        public Viewport renderViewport;

        public final EntitySystem ECS;
        public final EventSystem EVENT_SYSTEM;

        public void update(double delta) {
            ECS.update(delta);
        }

    }
    protected class p_PrePorts {


        public p_PrePorts() {
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

    protected final p_Systems p_SYSTEMS;
    private final p_PrePorts _PRE_PORTS;

    private final ArrayList<A_Scene> _LAYERED_SCENES;

    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- WINDOW INTERACTION -+-">

    protected abstract void p_onAddedToWindow(Window window);
    protected abstract void p_onRemovedFromWindow(Window window);

    // </editor-fold>
    // <editor-fold desc="-+- SCENE INTERACTION -+-">

    // TODO : Do this. Think about scene movement, scene transfer through windows/surfaces and scene removal and addition.

    // </editor-fold>
    // <editor-fold desc="-+- VIEWPORT INTERACTION -+-">

    public Viewport getViewport() {
        return p_SYSTEMS.renderViewport;
    }

    // </editor-fold>

    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        for (int index = _LAYERED_SCENES.size() - 1; index >= 0; index--) {
            _LAYERED_SCENES.get(index).update(delta);
        }

        p_SYSTEMS.update(delta);
    }

    // </editor-fold>

    // <editor-fold desc="-+- EVENT MANAGEMENT -+-">

    public EventSystem getEventSystem() {
        return p_SYSTEMS.EVENT_SYSTEM;
    }

    // </editor-fold>

}