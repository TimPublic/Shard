package dev.timkloepper.entity_component_system.component;


import dev.timkloepper.entity_component_system.component.exception.ComponentCopyIsNullException;
import dev.timkloepper.entity_component_system.component.exception.ComponentCopyNotOfSameClassException;


public abstract class A_EntityComponent {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public A_EntityComponent(int entityId) {
        _entityId = entityId;
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private int _entityId;

    // </editor-fold>
    // <editor-fold desc="FINALS">



    // </editor-fold>

    // </editor-fold

    // <editor-fold desc="-+- COPY LOGIC -+-">

    public <T extends A_EntityComponent> T copy() {
        T component;

        component = p_createCopy();

        if (component == null) throw new ComponentCopyIsNullException();
        if (component.getClass() != this.getClass()) throw new ComponentCopyNotOfSameClassException();

        return component;
    }

    protected abstract <T extends A_EntityComponent> T p_createCopy();

    // </editor-fold>

    // <editor-fold desc="-+- GETTERS -+-">

    public int getEntityId() {
        return _entityId;
    }

    // </editor-fold>


}