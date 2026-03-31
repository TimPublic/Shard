package dev.timkloepper.rendering.api.pipeline;

import dev.timkloepper.rendering.api.camera.Camera2D;
import dev.timkloepper.rendering.gpu.batch.A_Batch;
import dev.timkloepper.rendering.exceptions.MeshNotSupportedException;
import dev.timkloepper.rendering.exceptions.NoCameraException;
import dev.timkloepper.rendering.mesh.A_Mesh;

import java.util.HashMap;
import java.util.Optional;

/**
 * <p>
 *     Represents one collection of {@link A_Batch}, one for every mesh. <br>
 *     This means, it also holds only one {@link BatchRecipe} for every mesh. <br>
 *     It can only be created by a {@link Viewport}.
 * </p>
 * <p>
 *     Is rendered with one draw call, initialized by the containing viewport. <br>
 *     It is important to know, that this render pass does not represent one frame, but only one draw set.
 * </p>
 * <p>
 *     In standard, render pass uses the camera of its viewport, but it can also use a custom camera. <br>
 *     Also supports switches to other viewports, which automatically changes the standard camera.
 * </p>
 * <p>
 *     Can be moved through the viewport's render queue, to be rendered at a different time, relative to its siblings.
 * </p>
 *
 * @version 1.0
 *
 * @author Tim Kloepper
 */
public class RenderPass {

    // <editor-fold desc="-+- CREATION -+-">

    protected RenderPass(Camera2D camera) {
        this.camera = new CameraHolder(null, camera);

        _MESH_CLASS_DATA = new HashMap<>();
    }

    // </editor-fold>

    // <editor-fold desc="-+- CLASSES -+-">

    public class CameraHolder {

        private CameraHolder(Camera2D std, Camera2D camera) {
            _stdCamera = std;
            _camera = camera;
        }

        private Camera2D _stdCamera;
        private Camera2D _camera;

        public void set(Camera2D camera) {
            if (camera == null) throw new NoCameraException("[RENDER PASS ERROR] : RenderPass' camera cannot be null!");

            _camera = camera;
        }
        public Camera2D get() {
            if (_camera == null) reset();

            return _camera;
        }

        public void reset() {
            _camera = _stdCamera;
        }

        public boolean has() {
            return _camera != null;
        }
        public boolean isStd() {
            return _camera == _stdCamera;
        }

    }

    private class MeshClassData<T extends A_Mesh> {

        public MeshClassData(Class<T> managedClass) {
            _managedClass = managedClass;

            _customFactory = s_STD_FACTORIES.get(_managedClass);
            _customRecipe = s_STD_RECIPES.get(_managedClass);
        }

        private static final HashMap<Class<? extends A_Mesh>, BatchFactory> s_STD_FACTORIES = new HashMap<>();
        private static final HashMap<Class<? extends A_Mesh>, BatchRecipe> s_STD_RECIPES = new HashMap<>();

        private Class<T> _managedClass;
        private BatchFactory _customFactory;
        private BatchRecipe _customRecipe;
        private A_Batch<T> _batch;

        public void setRecipe(BatchRecipe recipe) {
            _customRecipe = recipe;
        }
        public BatchRecipe getRecipe() {
            return _customRecipe;
        }

        public void resetShader() {
            _customRecipe = s_STD_RECIPES.get(_managedClass);
        }

        public static boolean setRecipeStd(Class<? extends A_Mesh> forMesh, BatchRecipe recipe) {
            s_STD_RECIPES.put(forMesh, recipe);

            return true;
        }
        public static BatchRecipe getRecipeStd(Class<? extends A_Mesh> forMesh) {
            return s_STD_RECIPES.get(forMesh);
        }

        public void setFactory(BatchFactory blueprint) {
            _customFactory = blueprint;
        }
        public BatchFactory getFactory() {
            return _customFactory;
        }

        public static boolean setFactoryStd(Class<? extends A_Mesh> forMesh, BatchFactory blueprint) {
            s_STD_FACTORIES.put(forMesh, blueprint);

            return true;
        }
        public static BatchFactory getFactoryStd(Class<? extends A_Mesh> forMesh) {
            return s_STD_FACTORIES.get(forMesh);
        }

        public A_Batch<T> getBatch() throws MeshNotSupportedException {
            if (_batch == null) {
                if (_customFactory == null || _customRecipe == null) throw new MeshNotSupportedException("[RENDER PASS ERROR] : This mesh class is not supported!");

                _batch = (A_Batch<T>) _customFactory.factor(_customRecipe);
            }

            return _batch;
        }

        public boolean isSupported() {
            return _customFactory != null && _customRecipe != null;
        }

    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private Viewport _viewport;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    public final CameraHolder camera;

    private final HashMap<Class<? extends A_Mesh>, MeshClassData<? extends A_Mesh>> _MESH_CLASS_DATA;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- BATCH RECIPE MANAGEMENT -+-">

    public void setRecipe(Class<? extends A_Mesh> forMesh, BatchRecipe recipe) {
        h_getMeshData(forMesh).setRecipe(recipe);
    }
    public Optional<BatchRecipe> getRecipe(Class<? extends A_Mesh> forMesh) {
        return Optional.ofNullable(h_getMeshData(forMesh).getRecipe());
    }

    public static void setRecipeStd(Class<? extends A_Mesh> forMesh, BatchRecipe recipe) {
        MeshClassData.setRecipeStd(forMesh, recipe);
    }
    public static BatchRecipe getRecipeStd(Class<? extends A_Mesh> forMesh) {
        return MeshClassData.getRecipeStd(forMesh);
    }

    public void resetRecipe(Class<? extends A_Mesh> forMesh) {
        h_getMeshData(forMesh).resetShader();
    }

    // </editor-fold>
    // <editor-fold desc="-+- BATCH MANAGEMENT -+-">

    public void setFactory(Class<? extends A_Mesh> forMesh, BatchFactory blueprint) {
        MeshClassData<? extends A_Mesh> data;

        data = h_getMeshData(forMesh);

        data.setFactory(blueprint);
    }
    public BatchFactory getFactory(Class<? extends A_Mesh> forMesh) {
        return h_getMeshData(forMesh).getFactory();
    }

    public static void setFactoryStd(Class<? extends A_Mesh> forMesh, BatchFactory blueprint) {
        MeshClassData.setFactoryStd(forMesh, blueprint);
    }
    public static BatchFactory getFactoryStd(Class<? extends A_Mesh> forMesh) {
        return MeshClassData.getFactoryStd(forMesh);
    }

    // </editor-fold>
    // <editor-fold desc="-+- MESH MANAGEMENT -+-">

    public <T extends A_Mesh> boolean addMesh(T mesh) {
        MeshClassData<? extends A_Mesh> data;

        data = h_getMeshData(mesh.getClass());

        try {
            return ((A_Batch<T>) data.getBatch()).addMesh(mesh);
        }
        catch (MeshNotSupportedException e) {
            return false;
        }
    }
    public <T extends A_Mesh> boolean rmvMesh(T mesh) {
        MeshClassData<? extends A_Mesh> data;

        data = h_getMeshData(mesh.getClass());

        try {
            return ((A_Batch<T>) data.getBatch()).rmvMesh(mesh);
        }
        catch (MeshNotSupportedException e) {
            return false;
        }
    }

    public <T extends A_Mesh> boolean containsMesh(T mesh) {
        MeshClassData<? extends A_Mesh> data;

        data = h_getMeshData(mesh.getClass());

        try {
            return ((A_Batch<T>) data.getBatch()).contains(mesh);
        }
        catch (MeshNotSupportedException e) {
            return false;
        }
    }
    public boolean isMeshSupported(Class<? extends A_Mesh> meshClass) {
        return h_getMeshData(meshClass).isSupported();
    }

    private <T extends A_Mesh> MeshClassData<T> h_getMeshData(Class<T> meshClass) {
        MeshClassData<T> data;

        data = (MeshClassData<T>) _MESH_CLASS_DATA.get(meshClass);
        if (data == null) _MESH_CLASS_DATA.put(meshClass, (data = new MeshClassData<>(meshClass)));

        return data;
    }

    // </editor-fold>
    // <editor-fold desc="-+- VIEWPORT MANAGEMENT -+-">

    public void move(int toIndex) {
        _viewport.movePass(this, toIndex);
    }
    public void moveFirst() {
        _viewport.movePassFirst(this);
    }
    public void moveLast() {
        _viewport.movePassLast(this);
    }

    protected void p_setViewport(Viewport viewport) {
        if (_viewport != null) {// This relies on the assumption, that Viewport::transferPassTo() removes the pass first, before calling RenderPass::p_setViewport().
            if (_viewport.containsPass(this))
                throw new IllegalStateException("[RENDER PASS ERROR] : Render pass cannot be owned by two viewports at once!");
            if (camera._camera == _viewport.getCamera()) camera._camera = viewport.getCamera();
        }

        camera._stdCamera = viewport.getCamera();

        this._viewport = viewport;
    }
    protected void p_setViewportCamera(Camera2D camera) {
        if (this.camera.isStd()) this.camera._stdCamera = camera;
        this.camera.set(camera);
    }

    // </editor-fold>

    // <editor-fold desc="-+- RENDER LOGIC-+-">

    protected void p_render() {
        if (!camera.has()) throw new NoCameraException("[RENDER PASS ERROR] : The camera is null!");

        for (MeshClassData<? extends A_Mesh> data : _MESH_CLASS_DATA.values()) {
            A_Batch<? extends A_Mesh> batch;

            try {
                batch = data.getBatch();

                batch.render(camera.get());
            } catch (MeshNotSupportedException e) {
                continue;
            }
        }
    }

    // </editor-fold>

}