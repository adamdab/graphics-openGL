package org.example;

import org.example.window.Window;
import org.example.manager.CameraManager;
import org.example.manager.LightManager;
import org.example.controller.SpaceShip;
import org.example.controller.Satellite;
import org.example.controller.Neptune;

import org.example.camera.Camera;
import org.example.camera.CameraMovementType;
import static org.example.camera.CameraMovementType.*;

import org.example.lights.DirectionalLight;
import org.example.lights.PointLight;
import org.example.lights.SpotLight;

import org.example.models.Model;

import org.example.shader.Shader;
import org.example.shader.ShaderType;
import static org.example.shader.ShaderType.*;
import org.example.shader.fog.Fog;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("DataFlowIssue")
public class Animation {
    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastTime = 0.0f; // Time of last frame

    private static Window window = new Window();
    private static SpaceShip spaceShip;
    private static Satellite satellite;
    private static Neptune neptune;
    private static LightManager lightManager;
    private static CameraManager cameraManager;
    public static void main(String[] args) throws IOException {

        window.init();
        if(!window.checkStatus()) {
            System.out.println("Window initialization failed!");
            return;
        }

        cameraManager = new CameraManager();
        lightManager = new LightManager();
        lightManager.init();

        spaceShip = new SpaceShip(lightManager.getSpotLight(1), lightManager.getSpotLight(0));
        satellite = new Satellite(lightManager.getSpotLight(2));
        neptune = new Neptune();

        float frame = 0;
        int framesInSecond = 0;
        float prevFullSecondTime = 0;

        window.bindBuffer();

        // Begin animation
        System.out.println("[Begin of animation]");
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

            processInput();

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

    private static void processInput() {
        if(window.checkIfPressed(GLFW_KEY_1)) {
            cameraManager.changeCameraMovementType(STATIC, window.getHandler());
        } else if (window.checkIfPressed(GLFW_KEY_2)) {
            cameraManager.changeCameraMovementType(TPV, window.getHandler());
        } else if (window.checkIfPressed(GLFW_KEY_3)) {
            cameraManager.changeCameraMovementType(FPV, window.getHandler());
        } else if (window.checkIfPressed(GLFW_KEY_4)) {
            cameraManager.changeCameraMovementType(FREE, window.getHandler());
        } else if (window.checkIfPressed(GLFW_KEY_LEFT_CONTROL) && window.checkIfPressed(GLFW_KEY_UP)) {
            lightManager.increaseFog(deltaTime);
        } else if (window.checkIfPressed(GLFW_KEY_LEFT_CONTROL) && window.checkIfPressed(GLFW_KEY_DOWN)) {
            lightManager.decreaseFog(deltaTime);
        }else if (window.checkIfPressed(GLFW_KEY_LEFT_SHIFT) && window.checkIfPressed(GLFW_KEY_UP)) {
            lightManager.increaseLightIntensity(deltaTime);
        } else if (window.checkIfPressed(GLFW_KEY_LEFT_SHIFT) && window.checkIfPressed(GLFW_KEY_DOWN)) {
            lightManager.decreaseLightIntensity(deltaTime);
        } else if (window.checkIfPressed(GLFW_KEY_UP)) {
            lightManager.liftSpotLights(deltaTime);
        } else if (window.checkIfPressed(GLFW_KEY_DOWN)) {
            lightManager.lowerSpotLights(deltaTime);
        }  else if (window.checkIfPressed(GLFW_KEY_P)) {
            window.changeCurrentShader(PHONG);
        }  else if (window.checkIfPressed(GLFW_KEY_G)) {
            window.changeCurrentShader(GOURAUD);
        }  else if (window.checkIfPressed(GLFW_KEY_F)) {
            window.changeCurrentShader(FLAT);
        }
        if (window.checkIfPressed(GLFW_KEY_ESCAPE))
            window.exit();
    }
}