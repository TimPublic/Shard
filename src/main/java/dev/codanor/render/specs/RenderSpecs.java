package dev.codanor.render.specs;

import dev.codanor.engine.Worker;
import dev.codanor.render.specs.buffers.I_FrameBuffer;
import dev.codanor.render.specs.buffers.I_Image;
import dev.codanor.render.specs.buffers.I_FloatBuffer;
import dev.codanor.render.specs.buffers.I_IntBuffer;
import dev.codanor.render.specs.shaders.I_GeometryShader;
import dev.codanor.render.specs.shaders.I_PostProcessShader;
import dev.codanor.render.viewport.Batch;
import dev.codanor.render.specs.allocation.I_Allocator;
import dev.codanor.util.Indexer;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class RenderSpecs {

    public record SpecsRegistry<T>(HashMap<Integer, T> _SPECS) {

        public SpecsRegistry(Map<Integer, T> specs) {
            this(new HashMap<>(specs));
        }

        public void set(int mode, T specs) {
            if (mode < 0 || specs == null) throw new IllegalArgumentException();

            _SPECS.put(mode, specs);
        }
        public T get() {
            T specs;

            specs = _SPECS.get(_currentMode);

            if (specs == null) throw new IllegalStateException();

            return specs;
        }

    }

    public record BatchSpecs(
            Consumer<Batch.BatchRenderData> drawCall,
            int indexBufferSize,
            int vertexBufferSize,
            Supplier<I_Allocator> allocatorFactory
    ) {}
    public record BufferSpecs(
            Function<Integer, I_IntBuffer> intBufferFactory,
            Function<Integer, I_FloatBuffer> floatBufferFactory,
            Function<I_FrameBuffer.FrameBufferRecipe, I_FrameBuffer> frameBufferFactory,
            Function<I_Image.ImageRecipe, I_Image> imageFactory,
            Vector2i imageLayout,
            I_Image.FORMAT imageFormat,
            Runnable bindMainFrameCall,
            Runnable unbindMainFrameCall,
            Runnable clearMainFrameCall
    ) {}
    public record ShaderSpecs(
            Function<String, I_GeometryShader> geometryShaderFactory,
            Function<String, I_PostProcessShader> postProcessShaderFactory
    ) {}
    public record PipeSpecs(
            Runnable drawQuadCall
    ) {}

    private static final Indexer _MODE_INDEXER = new Indexer();
    private static final HashSet<Integer> _REGISTERED_MODES = new HashSet<>();

    public static final int OPEN_GL = newMode();

    public static final SpecsRegistry<BatchSpecs> BATCH = new SpecsRegistry<>(Map.of(
            OPEN_GL, new BatchSpecs(
                    (data) -> {
                        data.material().getShader().bind();
                        data.vertexBuffer().bind();
                        data.indexBuffer().bind();

                        Worker.GLFW.instruct(
                                () -> {
                                    int stride;

                                    stride = data.exampleMesh().getVertexSize() * Float.BYTES;

                                    glBindVertexArray(data.vertexArray());

                                    for (Map.Entry<String, Integer> entry : data.material().getShader().getLocationNames().entrySet()) {
                                        int location = entry.getValue();
                                        // attrib offset + stride need to come from BatchRenderData — see below
                                        glEnableVertexAttribArray(location);
                                        glVertexAttribPointer(location, data.exampleMesh().getAttribSize(entry.getKey()), GL_FLOAT, false, stride, data.exampleMesh().getAttribOffset(entry.getKey()));
                                    }

                                    glDrawElements(GL_TRIANGLES, data.indexBuffer().getSize(), GL_UNSIGNED_INT, 0);

                                    glBindVertexArray(0);
                                }
                        );

                        data.indexBuffer().unbind();
                        data.vertexBuffer().unbind();
                        data.material().getShader().unbind();
                    },
                    1000,
                    1000,
                    FlatAllocator::new
            )
    ));
    public static final SpecsRegistry<BufferSpecs> BUFFER = new SpecsRegistry<>(Map.of(
            OPEN_GL, new BufferSpecs(
                    GL_ElementBuffer::new,
                    GL_VertexBuffer::new,
                    GL_FrameBuffer::new,
                    GL_Texture::new,
                    new Vector2i(1080, 1920),
                    I_Image.FORMAT.RGBA8,
                    () -> Worker.GLFW.instruct(() -> glBindFramebuffer(GL_FRAMEBUFFER, 0)),
                    () -> Worker.GLFW.instruct(() -> glBindFramebuffer(GL_FRAMEBUFFER, 0)),
                    () -> Worker.GLFW.instruct(() -> {
                        glBindFramebuffer(GL_FRAMEBUFFER, 0);
                        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    })
            )
    ));
    public static final SpecsRegistry<ShaderSpecs> SHADER = new SpecsRegistry<>(Map.of(
            OPEN_GL, new ShaderSpecs(GL_GeometryShader::load, GL_PostProcessShader::load)
    ));
    public static final SpecsRegistry<PipeSpecs> PIPE = new SpecsRegistry<>(Map.of(
            OPEN_GL, new PipeSpecs(() -> {
                    GL_Quad.init();
                    GL_Quad.draw();
            })
    ));

    private static int _currentMode = OPEN_GL;

    public static int newMode() {
        int mode;

        _REGISTERED_MODES.add((mode = _MODE_INDEXER.get()));

        return mode;
    }

    public static void setMode(int mode) {
        if (!_REGISTERED_MODES.contains(mode)) throw new IllegalStateException();

        _currentMode = mode;
    }
    public static int getCurrentMode() {
        return _currentMode;
    }

}