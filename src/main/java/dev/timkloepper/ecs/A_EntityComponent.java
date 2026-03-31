package dev.timkloepper.ecs;

public class A_EntityComponent {

    public A_EntityComponent() {
        p_ownerId = -1;
    }

    protected boolean p_init(int ownerId) {
        if (p_ownerId != -1) return false;

        p_ownerId = ownerId;

        return true;
    }

    protected int p_ownerId;

}