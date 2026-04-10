package dev.codanor.render.specs.pipes;

import dev.codanor.render.specs.RenderSpecs;

public class Pipes {

    public static void renderQuad() {
        RenderSpecs.PIPE.get().drawQuadCall().run();
    }

}