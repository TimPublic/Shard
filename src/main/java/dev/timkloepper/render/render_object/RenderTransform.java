package dev.timkloepper.render.render_object;

import org.joml.Vector2d;

import java.util.ArrayList;

public class RenderTransform {

    public RenderTransform(double x, double y, double rotation) {
        _pos = new Vector2d(x, y);
        _rotation = rotation;

        _ON_CHANGED_CALLBACKS = new ArrayList<>();
    }

    private Vector2d _pos;
    private double _rotation;

    private final ArrayList<Runnable> _ON_CHANGED_CALLBACKS;

    public void setPos(double x, double y) {
        _pos.set(x, y);

        h_notifyChange();
    }
    public void setPos(Vector2d position) {
        _pos.set(position);

        h_notifyChange();
    }

    public void setX(double x) {
        _pos.set(x, _pos.y);

        h_notifyChange();
    }
    public void setY(double y) {
        _pos.set(_pos.x, y);

        h_notifyChange();
    }

    public Vector2d getPos() {
        return new Vector2d(_pos);
    }

    public double getX() {
        return _pos.x;
    }
    public double getY() {
        return _pos.y;
    }

    public void setRotation(double rotation) {
        _rotation = rotation;

        h_notifyChange();
    }

    public void rotate(double by) {
        _rotation += by;

        h_notifyChange();
    }

    public double getRotation() {
        return _rotation;
    }

    protected Runnable p_addedToRenderObject(Runnable onChangedCallback) {
        _ON_CHANGED_CALLBACKS.add(onChangedCallback);

        return () -> _ON_CHANGED_CALLBACKS.remove(onChangedCallback);
    }

    private void h_notifyChange() {
        for (Runnable callback : _ON_CHANGED_CALLBACKS) callback.run();
    }

}