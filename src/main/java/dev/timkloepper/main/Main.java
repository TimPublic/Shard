package dev.timkloepper.main;


import dev.timkloepper.engine.Engine;
import dev.timkloepper.render_container.Window;


public class Main {


    public static void main(String[] args) {
        Window.create(100, 100, "Hello World!");
        Engine.runAsync();

        while (true);
    }


}