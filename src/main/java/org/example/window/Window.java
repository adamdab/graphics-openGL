package org.example.window;

import org.example.window.Sources;
import org.example.shader.Shader;
import org.example.shader.ShaderType;
import static org.example.shader.ShaderType.*;
import org.example.shader.fog.Fog;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;
public class Window {

    private long window;

    private ShaderType currentShaderType;
    private Shader currentShader;
    private Shader phongShader;
    private Shader gouraudShader;
    private Shader flatShader;

    private Sources source;

    private boolean isInitialized;

    public Window() {
        isInitialized = false;
        currentShaderType = PHONG;
        source = new Sources();
    }

    public void init() {
        // init window
        GLFWErrorCallback.createPrint(System.err).set();
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        window = glfwCreateWindow(1920, 1080, "Neptun Wars", NULL, NULL);
        if (window == NULL) {
            System.out.println("Failed to create GLFW window");
            glfwTerminate();
            isInitialized = false;
            return;
        }

        isInitialized = true;
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glViewport(0, 0, 1920, 1080);
        glfwSetFramebufferSizeCallback(window, (window1, width, height) -> {
            glViewport(0, 0, width, height);
        });
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);

        // init shaders
        try {
            phongShader = new Shader(source.getPhongVertexShaderSource(), source.getPhongFragmentShaderSource());
            gouraudShader = new Shader(source.getGouraudVertexShaderSource(), source.getGouraudFragmentShaderSource());
            flatShader = new Shader(source.getFlatVertexShaderSource(), source.getFlatFragmentShaderSource());
            currentShader = phongShader;
        } catch (IOException e) {
            isInitialized = false;
            System.out.println("Failed to initialize shaders");
            return;
        }
    }

    public long getHandler() {
        return window;
    }

    public boolean checkStatus() {
        return isInitialized;
    }

    public Shader getCurrentShader() {
        return currentShader;
    }

    public boolean shouldBeClosed() {
        return glfwWindowShouldClose(window);
    }

    public void setWindowTitle(int frames) {
        String title = "";
        switch(currentShaderType) {
            case PHONG:
                title = "Phong | ";
                break;
            case GOURAUD:
                title = "Gouraud | ";
                break;
            case FLAT:
                title = "Flat | ";
                break;
        }
        title+="FPS: " + frames;
        glfwSetWindowTitle(window, title);
    }

    public void bindBuffer() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void cleanFrame() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void nextFrame() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public void delete() {
        glfwTerminate();
    }

    public void changeCurrentShader(ShaderType shaderType) {
        switch(shaderType)  {
            case GOURAUD:
                currentShader = gouraudShader;
                break;
            case FLAT:
                currentShader = flatShader;
                break;
            default:
                currentShader = phongShader;
                break;
        }
        currentShaderType = shaderType;
    }

    public boolean checkIfPressed(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    public void exit() {
        glfwSetWindowShouldClose(window, true);
    }


}
