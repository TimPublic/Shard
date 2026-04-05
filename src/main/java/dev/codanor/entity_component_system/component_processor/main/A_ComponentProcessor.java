package dev.codanor.entity_component_system.component_processor.main;


import dev.codanor.entity_component_system.component.A_EntityComponent;
import dev.codanor.entity_component_system.component_processor.post.I_ComponentPostProcessor;
import dev.codanor.update.I_UpdateLoop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;


public abstract class A_ComponentProcessor<T extends A_EntityComponent> implements I_UpdateLoop {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public A_ComponentProcessor() {
        _COMPONENTS = new HashMap<>();

        _POST_PROCESSORS = new ArrayList<>();
    }

    // </editor-fold>


    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">



    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final HashMap<Integer, T> _COMPONENTS;

    private final ArrayList<I_ComponentPostProcessor<T>> _POST_PROCESSORS;

    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- COMPONENT MANAGEMENT -+-">

    public boolean add(T component, boolean overwrite) {
        int entityId;

        entityId = component.getEntityId();

        if (_COMPONENTS.containsKey(entityId) && !overwrite) return false;

        _COMPONENTS.put(entityId, component);

        return true;
    }

    public boolean rmv(int entityId) {
        return _COMPONENTS.remove(entityId) != null;
    }
    public boolean rmv(T component) {
        int entityId;

        entityId = component.getEntityId();

        if (_COMPONENTS.get(entityId) != component) return false;

        _COMPONENTS.remove(entityId);

        return true;
    }

    public Optional<T> get(int entityId) {
        if (entityId <= -1) return Optional.empty();

        return Optional.ofNullable(_COMPONENTS.get(entityId));
    }

    public boolean has(int entityId) {
        return _COMPONENTS.containsKey(entityId);
    }
    public boolean has(int entityId, T component) {
        return _COMPONENTS.containsKey(entityId) && _COMPONENTS.get(entityId) == component;
    }

    // </editor-fold>
    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        p_updateInternal(delta);

        h_updatePostProcessors(delta);
    }

    protected abstract void p_updateInternal(double delta);

    // </editor-fold>
    // <editor-fold desc="-+- POST PROCESSOR MANAGEMENT -+-">

    public void addPostProcessor(I_ComponentPostProcessor<T> postProcessor) {
        _POST_PROCESSORS.add(postProcessor);
    }
    public boolean rmvPostProcessor(I_ComponentPostProcessor<T> postProcessor) {
        return _POST_PROCESSORS.remove(postProcessor);
    }

    private void h_updatePostProcessors(double delta) {
        ArrayList<T> componentCopies;

        componentCopies = new ArrayList<>();
        _COMPONENTS.values().forEach((component) -> componentCopies.add(component.copy()));

        for (I_ComponentPostProcessor<T> postProcessor : _POST_PROCESSORS) {
            componentCopies.clear();
            if (postProcessor.needsDetail()) _COMPONENTS.values().forEach((component) -> componentCopies.add(component.copy()));

            postProcessor.update(delta, componentCopies);
        }
    }

    // </editor-fold>


    // <editor-fold desc="-+- REQUIREMENTS SPECIFICATION -+-">

    public abstract Class<T> getProcessedComponentClass();
    public abstract Collection<Class<A_EntityComponent>> getRequiredComponentClasses();

    // </editor-fold>


    // <editor-fold desc="-+- GETTERS -+-">

    public Collection<I_ComponentPostProcessor<T>> getPostProcessors() {
        return new ArrayList<>(_POST_PROCESSORS);
    }
    public Collection<T> getComponents() {
        return new ArrayList<>(_COMPONENTS.values());
    }

    // </editor-fold>


}