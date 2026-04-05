package dev.timkloepper.render.render_object;

/**
 * Combines a {@link Mesh}, a {@link Material} and a {@link RenderTransform} into one object,
 * in order to be rendered easily. <br>
 * Changes in any of the components are registered automatically, handled by callbacks to avoid backdoor access. <br>
 * Be aware, that material or mesh changes always need to conform to each other's supportability, which
 * is why there is a dedicated {@link RenderObject#setMeshAndMaterial(Mesh, Material)} method, to change both at one time.
 * This enables changes of the apply object to a completely different material and mesh set.
 * The apply object works with version control, in order to support multiple managers, who can then check the apply object's
 * current version against their locally cached one.
 *
 * @version 0.1
 *
 * @author Tim Kloepper
 */
public class RenderObject {

    public RenderObject(RenderTransform transform, Material material, Mesh mesh) {
        _version = -1;

        _CHANGE_CALLBACK = () -> {if (_version != -1) _version++;};

        if (!setTransform(transform)) throw new RuntimeException();
        if (!setMeshAndMaterial(mesh, material)) throw new RuntimeException();

        _version = 0;
    }

    private int _version;

    private RenderTransform _transform;
    private Runnable _transformRemoveCallback;

    private Material _material;
    private Runnable _materialRemoveCallback;

    private Mesh _mesh;
    private Runnable _meshRemoveCallback;

    private final Runnable _CHANGE_CALLBACK;

    /**
     * Checks the apply object's version against the provided one. <br>
     * Be aware, that if the provided version exceeds the actual version,
     * {@code true} is returned, just as if the provided version is below the actual one.
     *
     * @param version Version to check against the actual apply object's version.
     *
     * @return {@code true} if the version do not match up, {@code false} if they match up.
     */
    public boolean hasChanged(int version) {
        return version != _version;
    }
    /**
     * Retrieves the current version of this apply object.
     *
     * @return Current version.
     */
    public int getVersion() {
        return _version;
    }

    /**
     * Changes the {@link RenderTransform} of this apply object. <br>
     * Also increments this apply object's version. <br>
     * If the provided transform is {@code null}, {@code false} is returned.
     *
     * @param transform The new apply transform for this apply object.
     *
     * @return The success of this setter method.
     */
    public boolean setTransform(RenderTransform transform) {
        if (transform == null) return false;

        if (_transformRemoveCallback != null) _transformRemoveCallback.run();

        _transform = transform;
        _transformRemoveCallback = _transform.p_addedToRenderObject(_CHANGE_CALLBACK);

        _version++;

        return true;
    }
    /**
     * Retrieves the current {@link RenderTransform} of this apply object. <br>
     * Be aware, that changes on this apply transform are notices and registered in this apply object
     * by incrementing the version.
     *
     * @return The current apply transform of this apply object.
     */
    public RenderTransform getTransform() {
        return _transform;
    }

    /**
     * Sets the {@link Material} for this apply object. <br>
     * This material must be not null and must support this apply object's current
     * {@link Mesh}, otherwise {@code false} is returned. <br>
     * If you want to change the material and the mesh to currently not supported instances,
     * please use {@link RenderObject#setMeshAndMaterial(Mesh, Material)}, which sets
     * both and only checks the supportability between those two instances.
     *
     * @param material The material that should be added to this apply object.
     *
     * @return The success of this setter method.
     */
    public boolean setMaterial(Material material) {
        if (material == null) return false;
        if (!material.supports(_mesh)) return false;

        if (_materialRemoveCallback != null) _materialRemoveCallback.run();

        _material = material;
        _materialRemoveCallback = _material.addChangeCallback(_CHANGE_CALLBACK);

        _version++;

        return true;
    }
    /**
     * Returns this apply object's current {@link Material}. <br>
     * Be aware, that changes to this material are recognized by this apply object automatically
     * and will result in an increment of this apply object's version.
     *
     * @return This apply object's current material.
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Changes this apply object's current {@link Mesh} to the provided one. <br>
     * This new instance has to be not null and be supported by this apply object's current
     * {@link Material}. Otherwise, {@code false} is returned.
     * If you want to bypass the support restriction by setting both a new mesh and material at the same time,
     * please use {@link RenderObject#setMeshAndMaterial(Mesh, Material)}. This method sets both instances simultaneously
     * and only checks the supportability between those two instances.
     *
     * @param mesh The new mesh for this apply object.
     *
     * @return The success of this setter method.
     */
    public boolean setMesh(Mesh mesh) {
        if (mesh == null) return false;
        if (!_material.supports(mesh)) return false;

        if (_meshRemoveCallback != null) _meshRemoveCallback.run();

        _mesh = mesh;
        _meshRemoveCallback = _mesh.p_onAddedToRenderObject(_CHANGE_CALLBACK);

        _version++;

        return true;
    }
    /**
     * Returns this apply object's current {@link Mesh}. <br>
     * Be aware that changes to this mesh are automatically identified by this apply object and will therefore
     * result in an incremention of this apply object's version.
     *
     * @return This apply object's current mesh.
     */
    public Mesh getMesh() {
        return _mesh;
    }

    /**
     * Sets both the current {@link Mesh} and {@link Material} of this apply object to the provided instances. <br>
     * These instances must be not null and the material must support the provided mesh. <br>
     * If these constraints are not met, the method will fail and return {@code false} without setting either the mesh or the material
     * to the new values.
     *
     * @param mesh The new mesh for this apply object.
     * @param material The new material for this apply object.
     *
     * @return Whether this setter method succeeded or not.
     */
    public boolean setMeshAndMaterial(Mesh mesh, Material material) {
        if (mesh == null || material == null) return false;

        if (!material.supports(mesh)) return false;

        // --- //

        if (_meshRemoveCallback != null) _meshRemoveCallback.run();

        _mesh = mesh;
        _meshRemoveCallback = _mesh.p_onAddedToRenderObject(_CHANGE_CALLBACK);

        // --- //

        if (_materialRemoveCallback != null) _materialRemoveCallback.run();

        _material = material;
        _materialRemoveCallback = _material.addChangeCallback(_CHANGE_CALLBACK);

        // --- //

        _version++;

        return true;
    }

}