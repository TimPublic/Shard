package dev.timkloepper.entity_component_system.entity_system;


import dev.timkloepper.entity_component_system.component.A_EntityComponent;
import dev.timkloepper.entity_component_system.component_processor.main.A_ComponentProcessor;
import dev.timkloepper.update.I_UpdateLoop;
import dev.timkloepper.util.Indexer;

import java.util.HashMap;
import java.util.HashSet;


public class EntitySystem implements I_UpdateLoop {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public EntitySystem() {
        _ENTITY_ID_INDEXER = new Indexer();
        _ENTITY_IDS = new HashSet<>();

        _PROCESSORS = new HashMap<>();
    }

    // </editor-fold>


    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">



    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final Indexer _ENTITY_ID_INDEXER;
    private final HashSet<Integer> _ENTITY_IDS;

    private final HashMap<Class<? extends A_EntityComponent>, A_ComponentProcessor<? extends A_EntityComponent>> _PROCESSORS;

    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="-+- ENTITY MANAGEMENT -+-">

    public int register() {
        int id;

        _ENTITY_IDS.add((id = _ENTITY_ID_INDEXER.get()));

        return id;
    }
    public boolean deregister(int id) {
        if (!_ENTITY_IDS.remove(id)) return false;

        _ENTITY_ID_INDEXER.free(id);

        return true;
    }

    // </editor-fold>
    // <editor-fold desc="-+- PROCESSOR MANAGEMENT -+-">

    public boolean addProcessor(A_ComponentProcessor<?> processor, boolean overwrite) {
        Class<? extends A_EntityComponent> componentClass;

        componentClass = processor.getProcessedComponentClass();

        if (_PROCESSORS.containsKey(componentClass) && !overwrite) return false;

        _PROCESSORS.put(componentClass, processor);

        return true;
    }

    public boolean rmvProcessor(Class<? extends A_EntityComponent> forComponentClass) {
        return _PROCESSORS.remove(forComponentClass) != null;
    }
    public boolean rmvProcessor(A_ComponentProcessor<? extends A_EntityComponent> processor) {
        Class<? extends A_EntityComponent> componentClass;

        componentClass = processor.getProcessedComponentClass();

        if (_PROCESSORS.get(componentClass) != processor) return false;

        _PROCESSORS.remove(componentClass);

        return true;
    }

    // </editor-fold>
    // <editor-fold desc="-+- COMPONENT MANAGEMENT -+-">

    public <T extends A_EntityComponent> boolean add(T component, boolean overwrite) {
        A_ComponentProcessor<T> processor;

        processor = (A_ComponentProcessor<T>) _PROCESSORS.get(component.getClass());
        if (processor == null) return false;

        return processor.add(component, overwrite);
    }

    public <T extends A_EntityComponent> boolean rmv(T component) {
        A_ComponentProcessor<T> processor;

        processor = (A_ComponentProcessor<T>) _PROCESSORS.get(component.getClass());
        if (processor == null) return false;

        return processor.rmv(component);
    }
    public boolean rmv(Class<? extends A_EntityComponent> componentClass, int entityId) {
        A_ComponentProcessor<? extends A_EntityComponent> processor;

        processor = _PROCESSORS.get(componentClass);
        if (processor == null) return false;

        return processor.rmv(entityId);
    }

    // </editor-fold>
    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        _PROCESSORS.values().forEach((processor) -> processor.update(delta));
    }

    // </editor-fold>


}