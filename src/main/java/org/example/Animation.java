package org.example;

import org.example.window.Window;
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
    static Camera camera = new Camera();
    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastTime = 0.0f; // Time of last frame
    private static CameraMovementType cameraMovementType = TPV;

    private static Window window = new Window();
    private static SpaceShip spaceShip;
    private static Satellite satellite;
    private static Neptune neptune;
    private static LightManager lightManager;
    public static void main(String[] args) throws IOException {

        window.init();
        if(!window.checkStatus()) {
            System.out.println("Window initialization failed!");
            return;
        }

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

            // camera movement
            switch(cameraMovementType) {
                case FPV:
                    camera.setCameraPos(new Vector3f(0f,0.3f,0f).add(spaceShipPosition));
                    camera.setCameraFront(new Vector3f(nextShipCameraFront.x, 0f , nextShipCameraFront.y));
                    break;
                case TPV:
                    camera.setCameraPos(new Vector3f(0f,0.5f,0f)
                            .add(new Vector3f(-nextShipCameraFront.x * 3, 0f , -nextShipCameraFront.y * 3))
                            .add(spaceShipPosition));
                    camera.setCameraFront(new Vector3f(nextShipCameraFront.x, 0f , nextShipCameraFront.y));
                    break;
                case FREE:
                    camera.processInput(window.getHandler(), deltaTime);
                    camera.startProcessingMouseMovement(window.getHandler());
                    break;
            }

            window.cleanFrame();
            Shader currentShader = window.getCurrentShader();

            currentShader.use();
            lightManager.useFog(currentShader);

            try (MemoryStack stack = MemoryStack.stackPush()) {

                // set up view and projection
                final var view = camera.getViewMatrix();
                final var projection = new Matrix4f()
                        .perspective((float) toRadians(camera.getFov()), 1920.0f / 1080.0f, 0.1f, 100.0f);

                currentShader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                currentShader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                currentShader.setVec3("viewPos", camera.getCameraPos());

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
            camera.stopProcessingMouseMovement(window.getHandler());
            cameraMovementType = STATIC;
            camera.setCameraPos(new Vector3f(-1.9470121f, 7.8995733f, 25.64252f));
            camera.setCameraFront( new Vector3f(0.110537454f, -0.3583683f, -0.9270134f).normalize());
        } else if (window.checkIfPressed(GLFW_KEY_2)) {
            camera.stopProcessingMouseMovement(window.getHandler());
            cameraMovementType = TPV;
        } else if (window.checkIfPressed(GLFW_KEY_3)) {
            camera.stopProcessingMouseMovement(window.getHandler());
            cameraMovementType = FPV;
        } else if (window.checkIfPressed(GLFW_KEY_4)) {
            cameraMovementType = FREE;
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