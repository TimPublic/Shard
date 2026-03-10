package dev.timkloepper.entity_component_system.component_processor.post;


import dev.timkloepper.entity_component_system.component.A_EntityComponent;

import java.util.Collection;


public interface I_ComponentPostProcessor<T extends A_EntityComponent> {


    // <editor-fold desc="-+- UPDATE LOOP -+-">

    void update(double delta, Collection<T> componentCopies);

    // </editor-fold>


    // <editor-fold desc="-+- REQUIREMENTS SPECIFICATION -+-">

    Class<T> getProcessedComponentClass();

    boolean needsDetail();

    // </editor-fold>


}