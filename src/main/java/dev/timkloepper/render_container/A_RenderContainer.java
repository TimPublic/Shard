package dev.timkloepper.render_container;


import dev.timkloepper.update.I_UpdateLoop;


public abstract class A_RenderContainer implements I_UpdateLoop {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public A_RenderContainer(int width, int height, boolean resizeable) {
        this.width = width;
        this.height = height;

        _RESIZEABLE = resizeable;
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="-+- NON FINALS -+-">

    private int width, height;

    // </editor-fold>
    // <editor-fold desc="-+- FINALS -+-">

    private final boolean _RESIZEABLE;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- UPDATE LOOP -+-">

    @Override
    public void update(double delta) {
        p_render();
    }

    // </editor-fold>


    // <editor-fold desc="-+- RENDERING -+-">

    protected abstract void p_render();

    // </editor-fold>


    // <editor-fold desc="-+- GETTERS -+-">

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    // </editor-fold>
    // <editor-fold desc="-+- CHECKERS -+-">

    public boolean isResizeable() {
        return _RESIZEABLE;
    }

    // </editor-fold>


}