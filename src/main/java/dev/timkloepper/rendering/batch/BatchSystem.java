package dev.timkloepper.rendering.batch;


import dev.timkloepper.rendering.mesh.A_Mesh;
import dev.timkloepper.rendering.shader.Shader;

import java.util.HashMap;
import java.util.HashSet;


public class BatchSystem {

    // <editor-fold desc="-+- CREATION -+-">

    private BatchSystem() {
        _PROCESSORS = new HashMap<>();
    }

    public static BatchSystem create() {
        BatchSystem system;

        _SYSTEMS.add((system = new BatchSystem()));

        return system;
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">
    // </editor-fold>
    // <editor-fold desc="FINALS">

    private static final HashSet<BatchSystem> _SYSTEMS = new HashSet<>();

    private final HashMap<Class<? extends A_Mesh>, A_BatchProcessor<?>> _PROCESSORS;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- PROCESSOR MANAGEMENT -+-">

    public boolean addProcessor(A_BatchProcessor<? extends A_Mesh> processor, boolean overwrite) {
        Class<? extends A_Mesh> meshClass;

        meshClass = processor.p_getProcessedMeshClass();

        if (_PROCESSORS.containsKey(meshClass) && !overwrite) return false;

        _PROCESSORS.put(meshClass, processor);

        return true;
    }

    public boolean rmvProcessor(A_BatchProcessor<? extends A_Mesh> processor) {
        return rmvProcessor(processor.p_getProcessedMeshClass());
    }
    public boolean rmvProcessor(Class<? extends A_Mesh> meshClass) {
        return _PROCESSORS.remove(meshClass) != null;
    }

    public static void addProcessorStd(A_BatchProcessor<? extends A_Mesh> processor) {
        for (BatchSystem system : _SYSTEMS) system.addProcessor(processor, false);
    }
    public static void addProcessor(A_BatchProcessor<? extends A_Mesh> processor) {
        for (BatchSystem system : _SYSTEMS) system.addProcessor(processor, true);
    }

    // </editor-fold>
    // <editor-fold desc="-+- MESH MANAGEMENT -+-">

    public <T extends A_Mesh> boolean add(T mesh) {
        A_BatchProcessor<T> processor;

        // If this fails, something is seriously wrong with the batch system or processor system!
        processor = h_getProcessor(mesh.getClass());
        if (processor == null) return false;

        return processor.add(mesh);
    }
    public <T extends A_Mesh> boolean add(T mesh, Shader shader) {
        A_BatchProcessor<T> processor;

        // If this fails, something is seriously wrong with the batch system or processor system!
        processor = h_getProcessor(mesh.getClass());
        if (processor == null) return false;

        return processor.add(mesh, shader);
    }

    public <T extends A_Mesh> boolean rmv(T mesh) {
        A_BatchProcessor<T> processor;

        // If this fails, something is seriously wrong with the batch system or processor system!
        processor = h_getProcessor(mesh.getClass());
        if (processor == null) return false;

        return processor.rmv(mesh);
    }

    public <T extends A_Mesh> boolean update(T mesh) {
        A_BatchProcessor<T> processor;

        processor = h_getProcessor(mesh.getClass());
        if (processor == null) return false;

        return processor.update(mesh);
    }
    public <T extends A_Mesh> boolean update(T mesh, Shader shader) {
        A_BatchProcessor<T> processor;

        processor = h_getProcessor(mesh.getClass());
        if (processor == null) return false;

        return processor.update(mesh, shader);
    }

    private A_BatchProcessor h_getProcessor(Class<? extends A_Mesh> meshClass) {
        return _PROCESSORS.get(meshClass);
    }

    // </editor-fold>
    // <editor-fold desc="-+- RENDER LOGIC -+-">

    public void render() {
        for (A_BatchProcessor<? extends A_Mesh> processor : _PROCESSORS.values()) processor.p_render();
    }

    // </editor-fold>

}