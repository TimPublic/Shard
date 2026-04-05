package dev.codanor.visual_container;


import dev.codanor.update.I_UpdateLoop;


public abstract class A_VisualContainer implements I_UpdateLoop {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public A_VisualContainer(int width, int height, boolean resizeable) {
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
    public abstract void update(double delta);

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