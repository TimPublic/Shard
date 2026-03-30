package dev.timkloepper.rendering.api.pipeline;

import dev.timkloepper.rendering.api.camera.Camera2D;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 *     Holds multiple viewports {@link Viewport} and renders them. <br>
 * </p>
 * <p>
 *     The most common example for something holding a surface, is the {@link dev.timkloepper.visual_container.Window}.
 * </p>
 * <p>
 *     Provides the option to create new viewports, which are directly added to this surface. <br>
 *     You can also transfer, add and remove viewports from and to a surface.
 * </p>
 *
 * @version 1.0
 *
 * @author Tim Kloepper
 */
public class Surface {

    // <editor-fold desc="-+- CREATION -+-">

    public Surface() {
        _PACKAGES = new HashMap<>();
        _RENDER_QUEUE = new ArrayList<>();
    }

    // </editor-fold>

    // <editor-fold desc="-+- CLASSES -+-">

    private class ViewportPackage {

        public ViewportPackage(Viewport owner) {
            OWNER = owner;
            PENDING = new ArrayList<>();
        }

        public ArrayList<ViewportPackage> contained;

        public final Viewport OWNER;
        public final ArrayList<ViewportPackage> PENDING;

    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">
    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final HashMap<Viewport, ViewportPackage> _PACKAGES;
    private final ArrayList<ViewportPackage> _RENDER_QUEUE;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- VIEWPORT MANAGEMENT -+-">

    // <editor-fold desc="LOCAL MOVEMENT">

    public boolean lMove(Viewport viewport, int toIndex) {
        ViewportPackage viewportPackage;
        ArrayList<ViewportPackage> localQueue;

        viewportPackage = _PACKAGES.get(viewport);
        if (viewportPackage == null) return false;

        localQueue = h_getLocalQueue(viewportPackage);

        localQueue.remove(viewportPackage);
        localQueue.add(toIndex, viewportPackage);

        return true;
    }
    public boolean lMoveFirst(Viewport viewport) {
        return lMove(viewport, 0);
    }
    public boolean lMoveLast(Viewport viewport) {
        ViewportPackage viewportPackage;
        ArrayList<ViewportPackage> localQueue;

        viewportPackage = _PACKAGES.get(viewport);
        if (viewportPackage == null) return false;

        localQueue = h_getLocalQueue(viewportPackage);

        localQueue.remove(viewportPackage);
        localQueue.addLast(viewportPackage);

        return true;
    }

    public boolean lStep(Viewport viewport, int amount) {
        ViewportPackage viewportPackage;
        ArrayList<ViewportPackage> localQueue;
        int currentIndex, newIndex;

        viewportPackage = _PACKAGES.get(viewport);
        if (viewportPackage == null) return false;

        localQueue = h_getLocalQueue(viewportPackage);
        currentIndex = localQueue.indexOf(viewportPackage);

        newIndex = currentIndex - amount; // negative because 0 is the front.
        if (newIndex < 0) newIndex = 0;
        else if (newIndex >= localQueue.size()) newIndex = localQueue.size() - 1;

        localQueue.remove(viewportPackage);
        localQueue.add(newIndex, viewportPackage);

        return true;
    }
    public boolean lStepBack(Viewport viewport, int amount) {
        return lStep(viewport, -amount);
    }

    // </editor-fold>
    // <editor-fold desc="TOP MOVEMENT">

    public boolean tMove(Viewport viewport, int toIndex) {
        ViewportPackage viewportPackage;
        ArrayList<ViewportPackage> localQueue;

        viewportPackage = _PACKAGES.get(viewport);
        if (viewportPackage == null) return false;

        localQueue = h_getLocalQueue(viewportPackage);

        localQueue.remove(viewportPackage);
        _RENDER_QUEUE.add(toIndex, viewportPackage);
        viewportPackage.contained = _RENDER_QUEUE;

        return true;
    }
    public boolean tMoveFirst(Viewport viewport) {
        return tMove(viewport, 0);
    }
    public boolean tMoveLast(Viewport viewport) {
        ViewportPackage viewportPackage;
        ArrayList<ViewportPackage> localQueue;

        viewportPackage = _PACKAGES.get(viewport);
        if (viewportPackage == null) return false;

        localQueue = h_getLocalQueue(viewportPackage);

        localQueue.remove(viewportPackage);
        _RENDER_QUEUE.addLast(viewportPackage);
        viewportPackage.contained = _RENDER_QUEUE;

        return true;
    }

    public boolean tStep(Viewport viewport, int amount) {
        ViewportPackage viewportPackage;
        ArrayList<ViewportPackage> localQueue;
        int currentIndex, newIndex;

        viewportPackage = _PACKAGES.get(viewport);
        if (viewportPackage == null) return false;

        localQueue = h_getLocalQueue(viewportPackage);
        currentIndex = localQueue.indexOf(viewportPackage);

        if (currentIndex == -1) return false;

        newIndex = currentIndex - amount; // negative because 0 is the front.
        if (newIndex < 0) newIndex = 0;
        else if (newIndex >= localQueue.size()) newIndex = localQueue.size() - 1;

        localQueue.remove(viewportPackage);
        _RENDER_QUEUE.add(newIndex, viewportPackage);
        viewportPackage.contained = _RENDER_QUEUE;

        return true;
    }
    public boolean tStepBack(Viewport viewport, int amount) {
        return tStep(viewport, -amount);
    }

    // </editor-fold>

    public Viewport viewport() {
        Viewport viewport;

        viewport = new Viewport();
        addViewport(viewport);

        return viewport;
    }
    public Viewport viewport(Camera2D camera) {
        Viewport viewport;

        viewport = new Viewport(camera);
        addViewport(viewport);

        return viewport;
    }

    public boolean addViewport(Viewport viewport) {
        ViewportPackage viewportPackage;

        if (containsViewport(viewport)) return false;

        _PACKAGES.put(viewport, (viewportPackage = new ViewportPackage(viewport)));
        _RENDER_QUEUE.add(viewportPackage);

        return true;
    }
    public boolean rmvViewport(Viewport viewport) {
        ViewportPackage viewportPackage;
        ArrayList<ViewportPackage> containingQueue;
        int index;

        viewportPackage = _PACKAGES.remove(viewport);
        if (viewportPackage == null) return false;

        containingQueue = h_getLocalQueue(viewportPackage);
        index = containingQueue.indexOf(viewportPackage);
        containingQueue.remove(viewportPackage);

        containingQueue.addAll(index, viewportPackage.PENDING);
        for (ViewportPackage pendingPackage : viewportPackage.PENDING) pendingPackage.contained = containingQueue;

        return true;
    }

    public boolean transferViewportTo(Viewport viewport, Surface to) {
        if (!rmvViewport(viewport)) return false;

        return to.addViewport(viewport);
    }
    public boolean transferViewportFrom(Viewport viewport, Surface from) {
        if (!from.rmvViewport(viewport)) return false;

        return addViewport(viewport);
    }

    public boolean pend(Viewport viewport, Viewport to, int toIndex) {
        ViewportPackage pending, containing;

        pending = _PACKAGES.get(viewport);
        if (pending == null) return false;

        containing = _PACKAGES.get(to);
        if (containing == null) return false;

        h_getLocalQueue(pending).remove(pending);
        containing.PENDING.add(toIndex, pending);
        pending.contained = containing.PENDING;

        return true;
    }

    public boolean containsViewport(Viewport viewport) {
        return _PACKAGES.containsKey(viewport);
    }

    private ArrayList<ViewportPackage> h_getLocalQueue(ViewportPackage viewportPackage) {
        ArrayList<ViewportPackage> localQueue;

        if (viewportPackage.contained == null) localQueue = _RENDER_QUEUE;
        else localQueue = viewportPackage.contained;

        return localQueue;
    }

    // </editor-fold>
    // <editor-fold desc="-+- RENDER MANAGEMENT -+-">

    public void render() {
        for (ViewportPackage viewportPackage : _RENDER_QUEUE) h_renderPackage(viewportPackage);
    }

    private void h_renderPackage(ViewportPackage viewportPackage) {
        viewportPackage.PENDING.forEach(this::h_renderPackage);

        viewportPackage.OWNER.p_render();
    }

    // </editor-fold>

}