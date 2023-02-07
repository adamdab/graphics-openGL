package org.example.controller;

import org.example.shader.Shader;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
public class Controller {
    protected MemoryStack stack;
    protected static Vector3f worldUp = new Vector3f(0, 1, 0);
    public void setStack(MemoryStack stack) {
        this.stack = stack;
    }
    public void generateFrame(Shader currentShader, float spotLightRotation, float frame) {};
}
