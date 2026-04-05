package dev.timkloepper.render.specs;

import dev.timkloepper.engine.Worker;
import dev.timkloepper.render.render_object.Mesh;
import dev.timkloepper.render.specs.shaders.*;
import dev.timkloepper.render.specs.shaders.exceptions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL20.*;

public class GL_Shader implements I_GeometryShader {

    private GL_Shader(String path) {
        _programId = -1;
        _vertexId = -1;
        _fragmentId = -1;

        _vertexSrc = "";
        _fragmentSrc = "";

        _LAYOUTS = new HashMap<>();

        h_parseShaderSources(path);

        Worker.GLFW.instruct(this::h_createProgram);

        h_parseLayouts();
    }

    protected static GL_Shader load(String path) {
        GL_Shader shader;

        shader = s_SHADER_REGISTRY.computeIfAbsent(path, GL_Shader::new);

        return shader;
    }

    private int _programId, _vertexId, _fragmentId;
    private String _vertexSrc, _fragmentSrc;

    private static final HashMap<String, GL_Shader> s_SHADER_REGISTRY = new HashMap<>();

    private final HashMap<String, Integer> _LAYOUTS;

    private final String _SHADER_FILE_SPLIT_REGEX = "(#type)( )+";
    private final String _VERTEX_TYPE_ID = "#type vertex";
    private final String _FRAGMENT_TYPE_ID = "#type fragment";
    private final String _VERTEX_NAME = "vertex";
    private final String _FRAGMENT_NAME = "fragment";
    private final String _LAYOUT_DETECTION_REGEX = "layout\\s*\\(\\s*location\\s*=\\s*(\\d+)\\s*\\)\\s*in\\s+(\\w+)\\s+(\\w+)";

    private void h_createProgram() {
        Worker.GLFW.contextFocus();

        h_createAndCompileVertexShader();
        h_createAndCompileFragmentShader();

        h_createAndLinkProgram();
    }

    private void h_parseShaderSources(String path) {
        String[] contents;

        try {
            contents = new String(Files.readAllBytes(Path.of(path))).split(_SHADER_FILE_SPLIT_REGEX);
        } catch (IOException e) {
            throw new CouldNotReadShaderFileException("[SHADER ERROR] : An error occurred while trying to read the shader file from the specified path!");
        }

        for (String src : contents) {
            src = src.trim();

            if (src.startsWith(_VERTEX_NAME)) {
                _vertexSrc = src.substring(_VERTEX_NAME.length()).trim();
            }

            if (src.startsWith(_FRAGMENT_NAME)) {
                _fragmentSrc = src.substring(_FRAGMENT_NAME.length()).trim();
            }
        }

        if (Objects.equals(_vertexSrc, "")) throw new CouldNotParseVertexShaderException("[SHADER ERROR] : Could not find the vertex shader in the specified file!");
        if (Objects.equals(_fragmentSrc, "")) throw new CouldNotParseFragmentShaderException("[SHADER ERROR] : Could not find the fragment shader in the specified file!");
    }
    private void h_parseShaderSources(String vertexPath, String fragmentPath) {
        String vertexSrcFile, fragmentSrcFile;

        try {
            vertexSrcFile = new String(Files.readAllBytes(Path.of(vertexPath)));
            fragmentSrcFile = new String(Files.readAllBytes(Path.of(fragmentPath)));
        } catch (IOException e) {
            throw new CouldNotReadShaderFileException("[SHADER ERROR] : An error occurred while trying to read the shader files from the specified paths!");
        }

        if (!vertexSrcFile.startsWith(_VERTEX_TYPE_ID)) throw new CouldNotParseVertexShaderException("[SHADER ERROR] : Specified vertex shader file does not have the correct type id!");
        if (!fragmentSrcFile.startsWith(_FRAGMENT_TYPE_ID)) throw new CouldNotParseFragmentShaderException("[SHADER ERROR] : Specified fragment shader file does not have the correct type id!");

        _vertexSrc = vertexSrcFile.replace(_VERTEX_TYPE_ID, "");
        _fragmentSrc = fragmentSrcFile.replace(_FRAGMENT_TYPE_ID, "");
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

    private void h_parseLayouts() {
        Pattern pattern = Pattern.compile(_LAYOUT_DETECTION_REGEX);

        Matcher matcher = pattern.matcher(_vertexSrc);

        while (matcher.find()) {
            int location = Integer.parseInt(matcher.group(1));
            String name = matcher.group(3);

            _LAYOUTS.put(name, location);
        }
    }

    public boolean isReady() {
        return _programId >= 0 && _vertexId >= 0 && _fragmentId >= 0;
    }

    @Override
    public void bind() {
        glUseProgram(_programId);
    }
    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public HashMap<String, Integer> getLocationNames() {
        return _LAYOUTS;
    }
    @Override
    public boolean supportsMesh(Mesh mesh) {
        for (String attribName : mesh.getAttribNames()) if (!_LAYOUTS.containsKey(attribName)) return false;

        return true;
    }

}