package org.example;

import org.example.window.Window;
import org.example.manager.InputManager;
import org.example.manager.CameraManager;
import org.example.manager.LightManager;
import org.example.controller.SpaceShip;
import org.example.controller.Satellite;
import org.example.controller.Neptune;
import org.example.shader.Shader;


import java.io.IOException;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import static java.lang.Math.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("DataFlowIssue")
public class Animation {
    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastTime = 0.0f; // Time of last frame

    private static Window window = new Window();
    private static InputManager inputManager;
    private static LightManager lightManager;
    private static CameraManager cameraManager;
    private static SpaceShip spaceShip;
    private static Satellite satellite;
    private static Neptune neptune;
    public static void main(String[] args) throws IOException {

        window.init();
        if(!window.checkStatus()) {
            System.out.println("Window initialization failed!");
            return;
        }

        cameraManager = new CameraManager();
        lightManager = new LightManager();
        inputManager = new InputManager(window, cameraManager, lightManager);
        lightManager.init();

        spaceShip = new SpaceShip(lightManager.getSpotLight(1), lightManager.getSpotLight(0));
        satellite = new Satellite(lightManager.getSpotLight(2));
        neptune = new Neptune();

        float frame = 0;
        int framesInSecond = 0;
        float prevFullSecondTime = 0;

        window.bindBuffer();

        // Begin animation
        while (!window.shouldBeClosed()) {

            // frames per second calculation
            float currentTime = (float) glfwGetTime();
            frame+= deltaTime * 200;
            framesInSecond ++;
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;
            if (currentTime - prevFullSecondTime >= 1f) {
                window.setWindowTitle(framesInSecond);
                framesInSecond = 0;
                prevFullSecondTime = currentTime;
            }

            // position of spaceship
            var spaceShipPosition = spaceShip.getShipPosition(frame);
            var spaceShipFrontCameraPosition = spaceShip.getShipFront(frame);
            var nextShipCameraFront = spaceShip.getNextShipFront(frame);

            inputManager.processInput(deltaTime);

            cameraManager.moveCamera(spaceShipPosition, nextShipCameraFront, deltaTime, window.getHandler());

            window.cleanFrame();
            Shader currentShader = window.getCurrentShader();

            currentShader.use();
            lightManager.useFog(currentShader);

            try (MemoryStack stack = MemoryStack.stackPush()) {

                // set up view and projection
                final var view = cameraManager.getView();
                final var projection = new Matrix4f()
                        .perspective((float) toRadians(cameraManager.getFov()), 1920.0f / 1080.0f, 0.1f, 100.0f);

                currentShader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                currentShader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                currentShader.setVec3("viewPos", cameraManager.getPosition());

                // satellite
                satellite.setStack(stack);
                satellite.generateFrame(currentShader, lightManager.rotation(), frame);

                // spaceShip
                spaceShip.setStack(stack);
                spaceShip.generateFrame(currentShader, lightManager.rotation(), frame);

                // Neptune
                neptune.setStack(stack);
                neptune.generateFrame(currentShader, lightManager.rotation(), frame);

                lightManager.applyLighting(currentShader);
            }
            window.nextFrame();
        }
        window.delete();
    }
}