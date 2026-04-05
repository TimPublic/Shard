package dev.codanor.event_system;


import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.*;


public class EventSystem {


    // <editor-fold desc="-+- CREATION -+-">

    public EventSystem() {
        _PORTS = new ArrayList<>();
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">



    // </editor-fold>
    // <editor-fold desc="FINALS">

    private static final Cleaner _CLEANER = Cleaner.create();

    private final ArrayList<WeakReference<A_Port>> _PORTS;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- PORT MANAGEMENT -+-">

    public boolean addPort(A_Port port) {
        if (getPorts().contains(port)) return false;

        return _PORTS.add(new WeakReference<>(port)); // Returns true always.
    }
    public void addPorts(Collection<A_Port> ports) {
        ports.forEach(this::addPort);
    }

    public boolean rmvPort(A_Port port) {
        Iterator<WeakReference<A_Port>> iterator;

        iterator = _PORTS.iterator();

        while (iterator.hasNext()) {
            A_Port currentPort;

            currentPort = iterator.next().get();

            if (currentPort == null) iterator.remove();
            else if (currentPort == port) {
                iterator.remove();

                return true;
            }
        }

        return false;
    }
    public void rmvPorts(Collection<A_Port> ports) {
        ports.forEach(this::rmvPort);
    }

    public Set<A_Port> getPorts() {
        HashSet<A_Port> ports;
        Iterator<WeakReference<A_Port>> iterator;

        ports = new HashSet<>();
        iterator = _PORTS.iterator();

        while (iterator.hasNext()) {
            A_Port port;

            port = iterator.next().get();

            if (port == null) iterator.remove();
            else ports.add(port);
        }

        return ports;
    }

    public boolean containsPort(A_Port port) {
        return getPorts().contains(port);
    }

    // </editor-fold>
    // <editor-fold desc="-+- EVENT MANAGEMENT -+-">

    public void push(I_Event event) {
        getPorts().forEach((port) -> port.p_accept(event));
    }
    public void push(Collection<I_Event> events) {
        HashSet<A_Port> ports;

        ports = new HashSet<>(getPorts());

        events.forEach((event) -> {
            ports.forEach((port) -> port.p_accept(event));
        });
    }

    // </editor-fold>
    // <editor-fold desc="-+- CLEANER MANAGEMENT -+-">

    protected static Cleaner p_getCleaner() {
        return _CLEANER;
    }

    // </editor-fold>


}