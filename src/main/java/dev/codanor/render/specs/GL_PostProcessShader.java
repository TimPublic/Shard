package dev.codanor.render.specs;

import dev.codanor.engine.Worker;
import dev.codanor.render.specs.shaders.I_PostProcessShader;
import dev.codanor.render.specs.shaders.exceptions.CouldNotCompileFragmentShaderException;
import dev.codanor.render.specs.shaders.exceptions.CouldNotCompileVertexShaderException;
import dev.codanor.render.specs.shaders.exceptions.CouldNotLinkGL_ProgramException;
import dev.codanor.render.specs.shaders.exceptions.CouldNotReadShaderFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class GL_PostProcessShader implements I_PostProcessShader {

    private GL_PostProcessShader(String path) {
        _programId = -1;
        _vertexId = -1;
        _fragmentId = -1;

        _vertexSrc = "";
        _fragmentSrc = "";

        _LAYOUTS = new HashMap<>();
        _UNIFORMS = new HashMap<>();

        h_parseShaderSources(path);

        Worker.GLFW.instruct(this::h_createProgram);

        h_parseLayoutsAndUniforms();
    }

    protected static GL_PostProcessShader load(String path) {
        GL_PostProcessShader shader;

        shader = s_SHADER_REGISTRY.computeIfAbsent(path, GL_PostProcessShader::new);

        return shader;
    }

    private int _programId, _vertexId, _fragmentId;
    private String _vertexSrc, _fragmentSrc;

    private static final HashMap<String, GL_PostProcessShader> s_SHADER_REGISTRY = new HashMap<>();

    private final HashMap<String, Integer> _LAYOUTS, _UNIFORMS;
    private final String _UNIFORM_DETECTION_REGEX = "uniform\\s+sampler\\w+\\s+(\\w+)\\s*;";
    private final String _LAYOUT_DETECTION_REGEX = "layout\\s*\\(\\s*location\\s*=\\s*(\\d+)\\s*\\)\\s*out\\s+\\w+\\s+(\\w+)\\s*;";

    private void h_createProgram() {
        Worker.GLFW.contextFocus();

        h_createAndCompileVertexShader();
        h_createAndCompileFragmentShader();

        h_createAndLinkProgram();
    }

    private void h_parseShaderSources(String fragmentPath) {
        try {
            _vertexSrc = new String(Files.readAllBytes(Path.of("assets/shaders/std_postProcessVertex.glsl")));
            _fragmentSrc = new String(Files.readAllBytes(Path.of(fragmentPath)));
        } catch (IOException e) {
            throw new CouldNotReadShaderFileException("[SHADER ERROR] : An error occurred while trying to read the shader files from the specified paths!");
        }
    }

    private void h_createAndCompileVertexShader() {
        _vertexId = glCreateShader(GL_VERTEX_SHADER);

        glShaderSource(_vertexId, _vertexSrc);
        glCompileShader(_vertexId);

        if (glGetShaderi(_vertexId, GL_COMPILE_STATUS) != GL_FALSE) return;

        System.out.println(_vertexSrc);

        throw new CouldNotCompileVertexShaderException("[SHADER ERROR] : Could not compile the vertex shader:\n" + glGetShaderInfoLog(_vertexId));
    }
    private void h_createAndCompileFragmentShader() {
        _fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(_fragmentId, _fragmentSrc);
        glCompileShader(_fragmentId);

        if (glGetShaderi(_fragmentId, GL_COMPILE_STATUS) != GL_FALSE) return;

        throw new CouldNotCompileFragmentShaderException("[SHADER ERROR] : Could not compile the fragment shader:\n" + glGetShaderInfoLog(_fragmentId));
    }

    private void h_createAndLinkProgram() {
        _programId = glCreateProgram();

        glAttachShader(_programId, _vertexId);
        glAttachShader(_programId, _fragmentId);

        glLinkProgram(_programId);

        if (glGetProgrami(_programId, GL_LINK_STATUS) != GL_FALSE) return;

        throw new CouldNotLinkGL_ProgramException("[SHADER ERROR] : An error occurred while trying to link the gl program!");
    }

    private void h_parseLayoutsAndUniforms() {
        Matcher matcher;

        matcher = Pattern.compile(_LAYOUT_DETECTION_REGEX).matcher(_fragmentSrc);
        while (matcher.find()) {
            int location = Integer.parseInt(matcher.group(1));

            _LAYOUTS.put(matcher.group(2), location);
        }

        matcher = Pattern.compile(_UNIFORM_DETECTION_REGEX).matcher(_fragmentSrc);
        while (matcher.find()) {
            _UNIFORMS.put(matcher.group(1), 0);
        }

    }

    public boolean isReady() {
        return _programId >= 0 && _vertexId >= 0 && _fragmentId >= 0;
    }

    @Override
    public void bind() {
        Worker.GLFW.instruct(() -> glUseProgram(_programId));
    }
    @Override
    public void unbind() {
        Worker.GLFW.instruct(() -> glUseProgram(0));
    }

    @Override
    public Map<String, Integer> getImageUniforms() {
        return _UNIFORMS;
    }
    @Override
    public Map<String, Integer> getImageOutputs() {
        return _LAYOUTS;
    }

}