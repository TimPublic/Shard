package dev.codanor.event_system;

public abstract class A_Port {


    // <editor-fold desc="-+- CREATION -+-">

    public A_Port(EventFilter filter, Runnable destroyCallback) {
        _FILTER = filter;

        EventSystem.p_getCleaner().register(this, destroyCallback);
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">



    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final EventFilter _FILTER;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- FILTER MANAGEMENT -+-">

    public EventFilter getFilter() {
        return _FILTER;
    }

    // </editor-fold>

    // <editor-fold desc="-+- EVENT MANAGEMENT -+-">

    protected void p_accept(I_Event event) {
        if (!_FILTER.filter(event)) return;

        p_acceptFilteredEvent(event);
    }

    protected abstract void p_acceptFilteredEvent(I_Event event);

    // </editor-fold>


}