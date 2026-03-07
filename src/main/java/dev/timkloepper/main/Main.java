package dev.timkloepper.main;


import dev.timkloepper.engine.Engine;


public class Main {


    static void main() {
        new Thread(Engine::run).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Engine.kill();
    }


}