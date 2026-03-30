package dev.timkloepper.rendering.api.pipeline;

import dev.timkloepper.rendering.api.camera.Camera2D;
import dev.timkloepper.rendering.exceptions.NoCameraException;

import java.util.ArrayList;

/**
 * <p>
 *     A container for multiple render passes ({@link RenderPass}) and the default camera
 *     for those render passes. <br>
 *     Viewports are contained in the {@link Surface}, which renders the viewports. <br>
 *     It can only be created by a surface.
 * </p>
 * <p>
 *     The most common example for something using a viewport, is a scene ({@link dev.timkloepper.visual_container.A_Scene}).
 * </p>
 * <p>
 *     Can be sorted inside the {@link Surface} by either moving to a certain index, stepping or pending onto another viewport,
 *     which will hold the viewport relative to the pended-to viewport.
 * </p>
 * <p>
 *     Provides the option to create render passes, which are automatically added to this surface. <br>
 *     It is also possible to transfer, add and remove render passes from and to a viewport.
 * </p>
 *
 * @version 1.0
 *
 * @author Tim Kloepper
 */
public class Viewport {

    // <editor-fold desc="-+- CREATION -+-">

    protected Viewport() {
        _PASSES = new ArrayList<>();

        _camera = new Camera2D();
    }
    protected Viewport(Camera2D customCamera) {
        if (customCamera == null) throw new NoCameraException("[VIEWPORT ERROR] : Viewport's camera cannot be null!");

        _camera = customCamera;

        _PASSES = new ArrayList<>();
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private Camera2D _camera;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final ArrayList<RenderPass> _PASSES;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- CAMERA MANAGEMENT -+-">

    public void setCamera(Camera2D camera) {
        if (camera == null) throw new NoCameraException("[VIEWPORT ERROR] : Viewport's camera cannot be null!");

        _camera = camera;

        for (RenderPass pass : _PASSES) pass.p_setViewportCamera(_camera);
    }
    public Camera2D getCamera() {
        return _camera;
    }

    // </editor-fold>
    // <editor-fold desc="-+- RENDER PASS MANAGEMENT -+-">

    public RenderPass pass() {
        return pass(_camera);
    }
    public RenderPass pass(Camera2D customCamera) {
        RenderPass pass;

        _PASSES.add((pass = new RenderPass(customCamera)));
        pass.p_setViewport(this);

        return pass;
    }

    public boolean addPass(RenderPass pass) {
        if (containsPass(pass)) return false;

        _PASSES.add(pass);
        pass.p_setViewport(this);

        return true;
    }
    public boolean rmvPass(RenderPass pass) {
        return _PASSES.remove(pass);
    }
    public boolean transferPassTo(RenderPass pass, Viewport to) {
        if (!rmvPass(pass)) return false;

        return to.addPass(pass);
    }
    public boolean transferPassFrom(RenderPass pass, Viewport from) {
        return from.transferPassTo(pass, this);
    }

    public boolean movePass(RenderPass pass, int toIndex) {
        if (!_PASSES.remove(pass)) return false;

        _PASSES.add(toIndex, pass);

        return true;
    }
    public boolean movePassFirst(RenderPass pass) {
        return movePass(pass, 0);
    }
    public boolean movePassLast(RenderPass pass) {
        return movePass(pass, _PASSES.size() - 1);
    }

    public boolean containsPass(RenderPass pass) {
        return _PASSES.contains(pass);
    }

    // </editor-fold>

    // <editor-fold desc="-+- RENDER LOGIC -+-">

    public void p_render() {
        if (_camera == null) throw new NoCameraException("[VIEWPORT ERROR] : Camera is null!");

        for (RenderPass pass : _PASSES) pass.p_render();
    }

    // </editor-fold>

}