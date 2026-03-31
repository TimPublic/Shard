package dev.timkloepper.ecs;

import java.util.Collection;
import java.util.HashMap;

public abstract class A_ComponentProcessor<T extends A_EntityComponent> {

    public A_ComponentProcessor() {
        _COMPONENTS = new HashMap<>();
    }

    private final HashMap<Integer, T> _COMPONENTS;

    protected void p_add(T component) {
        _COMPONENTS.put(component.p_ownerId, component);
    }
    protected void p_add(Collection<T> components) {
        for (T component : components) p_add(component);
    }

    protected boolean p_rmv(int id) {
        return _COMPONENTS.remove(id) != null;
    }
    protected void p_rmv(Collection<Integer> ids) {
        ids.forEach(this::p_rmv);
    }

    protected Collection<T> p_getComponents() {
        return _COMPONENTS.values();
    }

    protected void p_clear() {
        _COMPONENTS.clear();
    }

    protected abstract Class<T> p_getProcessedClass();

    protected abstract void p_update(double delta);

}