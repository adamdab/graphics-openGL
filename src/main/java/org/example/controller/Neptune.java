package org.example.controller;

import org.example.Animation;
import org.example.models.Model;
import org.example.lights.SpotLight;
import org.example.shader.Shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;
public class Neptune extends Controller {
    private final Model neptune = new Model(Animation.class.getResource("Neptune.obj").getPath());

    public Neptune(){}

    @Override
    public void generateFrame(Shader currentShader, float spotLightRotation, float frame) {
        var model = new Matrix4f()
                .rotate((float) glfwGetTime(), new Vector3f(0f, 1f, 0f))
                .scale(0.8f);
        currentShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
        neptune.draw(currentShader);
    }
}
