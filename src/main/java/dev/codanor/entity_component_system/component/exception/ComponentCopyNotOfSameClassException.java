package dev.codanor.entity_component_system.component.exception;


public class ComponentCopyNotOfSameClassException extends RuntimeException {

    public ComponentCopyNotOfSameClassException(String message) {
        super(message);
    }
    public ComponentCopyNotOfSameClassException() {
        super("The created copy of this component is not of the same class!");
    }

}