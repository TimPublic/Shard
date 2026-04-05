package dev.codanor.entity_component_system.component.exception;


public class ComponentCopyIsNullException extends RuntimeException {

    public ComponentCopyIsNullException(String message) {
        super(message);
    }
    public ComponentCopyIsNullException() {
        super("The copy of this component is null!");
    }

}