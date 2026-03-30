package dev.timkloepper.main;

import dev.timkloepper.engine.Shard;
import dev.timkloepper.visual_container.Window;

public class Main {

    public static void main(String[] args) {
        Window.create(100, 100, "Hello World!");
        Shard.runAsync();

        while (true);
    }

}