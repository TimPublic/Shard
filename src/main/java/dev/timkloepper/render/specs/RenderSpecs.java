package dev.timkloepper.render.specs;

import dev.timkloepper.engine.Worker;
import dev.timkloepper.render.objects.frame_buffer.I_FrameBuffer;
import dev.timkloepper.render.specs.buffers.I_FloatBuffer;
import dev.timkloepper.render.specs.buffers.I_IntBuffer;
import dev.timkloepper.render.specs.shaders.I_GeometryShader;
import dev.timkloepper.render.viewport.Batch;
import dev.timkloepper.render.viewport.Camera2D;
import dev.timkloepper.render.specs.allocation.I_Allocator;
import dev.timkloepper.util.Indexer;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
            Function<Vector2i, I_FrameBuffer> frameBufferFactory
    ) {}
    public record ShaderSpecs(
            Function<String, I_GeometryShader> geometryShaderFactory
    ) {}

    private static final Indexer _MODE_INDEXER = new Indexer();
    private static final HashSet<Integer> _REGISTERED_MODES = new HashSet<>();

    public static final int OPEN_GL = newMode();

    public static final SpecsRegistry<BatchSpecs> BATCH = new SpecsRegistry<>(Map.of(
            OPEN_GL, new BatchSpecs(
                    (data) -> Worker.GLFW.instruct(
                            () -> {return;}
                    ),
                    1000,
                    1000,
                    FlatAllocator::new
            )
    ));
    public static final SpecsRegistry<BufferSpecs> BUFFER = new SpecsRegistry<>(Map.of(
            OPEN_GL, new BufferSpecs(
                    GL_ElementBuffer::new,
                    GL_VertexBuffer::new,
                    (layout) -> null
            )
    ));
    public static final SpecsRegistry<ShaderSpecs> SHADER = new SpecsRegistry<>(Map.of(
            OPEN_GL, new ShaderSpecs(GL_Shader::load)
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