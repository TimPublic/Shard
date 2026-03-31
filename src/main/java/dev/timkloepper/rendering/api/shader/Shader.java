package dev.timkloepper.rendering.api.shader;


import dev.timkloepper.engine.Worker;
import dev.timkloepper.rendering.api.shader.exception.CouldNotOpenShaderFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;


public class Shader {

    // <editor-fold desc="-+- CREATION -+-">

    public Shader(String path) {
        _PATH = path;

        Worker.GLFW.instruct(() -> h_compile(_PATH));
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private String _vertexString, _fragmentString;
    private int _vertexId, _fragmentId;
    private int _programId;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final String _PATH;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- LIFE CYCLE -+-">

    public void use() {
        glUseProgram(_programId);
    }
    public void unuse() {
        glUseProgram(0);
    }

    public void kill() {
        glDeleteShader(_vertexId);
        glDeleteShader(_fragmentId);
        glDeleteProgram(_programId);

        _vertexId = -1;
        _fragmentId = -1;
        _programId = -1;
    }
    public void recompile() {
        kill();

        h_compile(_PATH);
    }

    // </editor-fold>
    // <editor-fold desc="-+- COMPILATION -+-">

    private void h_compile(String path) {
        h_parseShaders(path);
        h_compileShaders();
        // h_createAndLink();
    }

    private void h_parseShaders(String path) {
        String[] contents;

        try {
            contents = new String(Files.readAllBytes(Path.of(path))).split("(#type)( )+");
        } catch (IOException e) {
            throw new CouldNotOpenShaderFileException("[shader ERROR] : Could not open the specified file!\n"
            + "|-> File : " + path);
        }

        for (String raw : contents) {
            if (raw.startsWith("vertex")) {
                _vertexString = raw.replace("vertex", "").strip();
                continue;
            }
            if (raw.startsWith("fragment")) {
                _fragmentString = raw.replace("fragment", "").strip();
                continue;
            }
        }
    }

    private void h_compileShaders() {
        _vertexId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(_vertexId, _vertexString);
        glCompileShader(_vertexId);

        h_checkForCompileErrors(_vertexId);

        _fragmentId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(_fragmentId, _fragmentString);
        glCompileShader(_fragmentId);

        h_checkForCompileErrors(_fragmentId);
    }
    private void h_checkForCompileErrors(int shaderId) {
        int logLength;

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) != GL_FALSE) return;

        logLength = glGetShaderi(shaderId, GL_INFO_LOG_LENGTH);

        throw new CouldNotOpenShaderFileException("[shader ERROR] : Could not compile the shader!\n"
        + "|-> Info Log : " + glGetShaderInfoLog(shaderId, logLength));
    }

    private void h_createAndLink() {
        _programId = glCreateProgram();

        glAttachShader(_programId, _vertexId);
        glAttachShader(_programId, _fragmentId);

        glLinkProgram(_programId);

        h_checkForLinkingErrors();
    }
    private void h_checkForLinkingErrors() {
        int logLength;

        if (glGetProgrami(_programId, GL_LINK_STATUS) != GL_FALSE) return;

        logLength = glGetProgrami(_programId, GL_INFO_LOG_LENGTH);

        throw new CouldNotOpenShaderFileException("[shader ERROR] : Could not link the shaders!\n"
                + "|-> Info Log : " + glGetProgramInfoLog(_programId, logLength));
    }

    // </editor-fold>

    // <editor-fold desc="-+- GETTERS, CHECKERS AND SETTERS -+-">

    public int getId() {
        return _programId;
    }
    public boolean isAlive() {
        return _programId != -1;
    }

    // </editor-fold>

}