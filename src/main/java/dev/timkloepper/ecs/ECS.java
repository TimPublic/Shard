package dev.timkloepper.ecs;

import dev.timkloepper.util.Indexer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

public class ECS {

    public ECS() {
        _PROCESSORS = new HashMap<>();

        _IDS = new HashSet<>();
        _ID_INDEXER = new Indexer();
    }

    private static final HashMap<Class<? extends A_EntityComponent>, Supplier<A_ComponentProcessor<? extends A_EntityComponent>>> _STD_BLUEPRINTS = new HashMap<>();

    private final HashMap<Class<? extends A_EntityComponent>, A_ComponentProcessor<A_EntityComponent>> _PROCESSORS;

    private final HashSet<Integer> _IDS;
    private final Indexer _ID_INDEXER;

    public static void setStdBlueprint(Class<? extends A_EntityComponent> componentClass, Supplier<A_ComponentProcessor<? extends A_EntityComponent>> blueprint) {
        _STD_BLUEPRINTS.put(componentClass, blueprint);
    }
    public static Supplier<A_ComponentProcessor<? extends A_EntityComponent>> getStdBlueprint(Class<? extends A_EntityComponent> componentClass) {
        return _STD_BLUEPRINTS.get(componentClass);
    }

    public <T extends A_EntityComponent> void setProcessor(A_ComponentProcessor<T> processor) {
        A_ComponentProcessor<T> prevProcessor;

        prevProcessor = (A_ComponentProcessor<T>) _PROCESSORS.get(processor.p_getProcessedClass());

        if (prevProcessor != null) {
            processor.p_add(prevProcessor.p_getComponents());
            prevProcessor.p_clear();
        }

        _PROCESSORS.put(processor.p_getProcessedClass(), (A_ComponentProcessor<A_EntityComponent>) processor);
    }
    public A_ComponentProcessor<? extends A_EntityComponent> getProcessor(Class<? extends A_EntityComponent> componentClass) {
        return _PROCESSORS.get(componentClass);
    }

    public <T extends A_EntityComponent> boolean add(T component, int id) {
        Class<T> componentClass;
        A_ComponentProcessor<T> processor;

        if (!_IDS.contains(id)) return false;

        componentClass = (Class<T>) component.getClass();
        processor = (A_ComponentProcessor<T>) _PROCESSORS.get(componentClass);

        if (processor == null) {
            Supplier<A_ComponentProcessor<? extends A_EntityComponent>> blueprint;

            blueprint = _STD_BLUEPRINTS.get(componentClass);

            if (blueprint == null) return false;

            processor = (A_ComponentProcessor<T>) blueprint.get();
        }

        if (!component.p_init(id)) return false;
        processor.p_add(component);

        return true;
    }
    public void add(Collection<? extends A_EntityComponent> components, int id) {
        if (!_IDS.contains(id)) return;

        components.forEach((component) -> add(component, id));
    }

    public boolean rmv(Class<? extends A_EntityComponent> componentClass, int id) {
        A_ComponentProcessor<? extends A_EntityComponent> processor;

        if (!_IDS.contains(id)) return false;

        processor = _PROCESSORS.get(componentClass);

        if (processor == null) return false;

        return processor.p_rmv(id);
    }
    public void rmv(Collection<Class<? extends A_EntityComponent>> componentClasses, int id) {
        if (!_IDS.contains(id)) return;

        componentClasses.forEach((componentClass) -> rmv(componentClass, id));
    }

    public <T extends A_EntityComponent> boolean rmv(T component) {
        int id;
        A_ComponentProcessor<T> processor;

        id = component.p_ownerId;

        if (!_IDS.contains(id)) return false;

        processor = (A_ComponentProcessor<T>) _PROCESSORS.get(component.getClass());

        if (processor == null) return false;

        return processor.p_rmv(id);
    }
    public void rmv(Collection<? extends A_EntityComponent> components) {
        components.forEach(this::rmv);
    }

    public int register() {
        int id;

        _IDS.add((id = _ID_INDEXER.get()));

        return id;
    }
    public boolean deregister(int id) {
        if (!_IDS.remove(id)) return false;

        _ID_INDEXER.free(id);

        _PROCESSORS.values().forEach((processor) -> processor.p_rmv(id));

        return true;
    }

    public boolean supports(Class<? extends A_EntityComponent> componentClass) {
        return _PROCESSORS.containsKey(componentClass) || _STD_BLUEPRINTS.containsKey(componentClass);
    }

    public void update(double delta) {
        _PROCESSORS.values().forEach((processor) -> processor.p_update(delta));
    }

}