package org.example;

import org.example.window.Window;

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
import java.util.List;
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
    static float spotLightRotation = -5f;
    static float fogFactor = 0f;
    static Vector3f worldUp = new Vector3f(0, 1, 0);
    private static final List<PointLight> pointLights = new ArrayList<>();
    private static final List<SpotLight> spotLights = new ArrayList<>();
    private static final List<DirectionalLight> directionalLights = new ArrayList<>();
    private static final Vector3f directionalLightColor = new Vector3f(0.15f, 0.15f, 0.15f);
    private static CameraMovementType cameraMovementType = FOLLOW;

    private static Window window = new Window();
    public static void main(String[] args) throws IOException {

        window.init();
        if(!window.checkStatus()) {
            System.out.println("Window initialization failed!");
            return;
        }
        // set up lights and fog
        setupLights();
        Fog fog = Fog.builder()
                .color(directionalLightColor)
                .build();

        // create models
        Model satellite = new Model(Animation.class.getResource("SpaceStation.obj").getPath());
        Model spaceShip = new Model(Animation.class.getResource("SciFi_Fighter_AK5.obj").getPath());
        Model neptune = new Model(Animation.class.getResource("Neptune.obj").getPath());

        // spaceShip positions
        List<Vector3f[]> positions = Files.readAllLines(new File(Animation.class.getResource("positions.txt").getPath()).toPath()).stream()
                .map(line -> line.split(" "))
                .map(l-> new Vector3f[] {
                        new Vector3f(Float.parseFloat(l[0]), Float.parseFloat(l[1]), Float.parseFloat(l[2])),
                        new Vector3f(Float.parseFloat(l[3]), Float.parseFloat(l[4]), Float.parseFloat(l[5]))
                }).collect(Collectors.toList());

        float frame = 0;
        int framesInSecond = 0;
        float prevFullSecondTime = 0;

        window.bindBuffer();

        // Begin animation
        while (!window.shouldBeClosed()) {

            // frames
            frame+= deltaTime * 300;
            framesInSecond ++;
            float currTime = (float) glfwGetTime();
            deltaTime = currTime - lastTime;
            lastTime = currTime;
            if (currTime - prevFullSecondTime >= 1f) {
                window.setWindowTitle(framesInSecond);
                framesInSecond = 0;
                prevFullSecondTime = currTime;
            }

            // position of spaceship
            var spaceShipPosition = positions.get((int)frame % positions.size())[0];
            var spaceShipFrontCameraPosition = positions.get((int)frame % positions.size())[1];
            var nextShipCameraFront = new Vector2f(spaceShipFrontCameraPosition.x, spaceShipFrontCameraPosition.z).normalize();

            processInput();

            // camera movement
            switch(cameraMovementType) {
                case FPV:
                    camera.setCameraPos(new Vector3f(0f,0.3f,0f).add(spaceShipPosition));
                    camera.setCameraFront(new Vector3f(nextShipCameraFront.x, 0f , nextShipCameraFront.y));
                    break;
                case FOLLOW:
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

            final var view = camera.getViewMatrix();
            final var projection = new Matrix4f()
                    .perspective((float) toRadians(camera.getFov()), 1920.0f / 1080.0f, 0.1f, 100.0f);

            Shader currentShader = window.getCurrentShader();

            currentShader.use();
            fog.setDensity(fogFactor);
            fog.applyFog(currentShader);

            try (MemoryStack stack = MemoryStack.stackPush()) {

                // set up view and projection
                currentShader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                currentShader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                currentShader.setVec3("viewPos", camera.getCameraPos());

                // satellite
               var model = new Matrix4f()
                        .rotate((float) glfwGetTime() * 0.2f, new Vector3f(0f, 1f, 1f))
                        .translate(new Vector3f(6f, 0.5f,0))
                        .rotate(0.5f, new Vector3f(1f,0f,1f))
                        .scale(0.04f);
                currentShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                satellite.draw(currentShader);

                Vector3f result = new Vector3f();
                model.transformPosition(new Vector3f(0,0,0), result);
                var SatelliteSpotlightRotationAxis = new Vector3f(-result.x, 0, -result.z).cross(worldUp)
                        .normalize();
                var SatelliteSpotLightDirection = new Vector3f(-result.x, -result.y, -result.z)
                        .rotateAxis((float) toRadians(spotLightRotation),
                                SatelliteSpotlightRotationAxis.x, SatelliteSpotlightRotationAxis.y, SatelliteSpotlightRotationAxis.z)
                        .normalize();

                var satelliteLight = spotLights.get(2);
                satelliteLight.setLightPosition(result);
                satelliteLight.setDirection(SatelliteSpotLightDirection);

                // spaceShip
                model = new Matrix4f()
                        .translate(spaceShipPosition)
                        .rotate(new Vector2f(nextShipCameraFront).angle(new Vector2f(0f,-1f)), new Vector3f(0f, 1f, 0f))
                        .scale(0.0008f);
                currentShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                spaceShip.draw(currentShader);

                var spotlightRotationAxis = new Vector3f(nextShipCameraFront.x, 0, nextShipCameraFront.y).cross(worldUp)
                        .normalize();
                var spotLightDirection = new Vector3f(nextShipCameraFront.x, 0, nextShipCameraFront.y)
                        .rotateAxis((float) toRadians(spotLightRotation),
                                spotlightRotationAxis.x, spotlightRotationAxis.y, spotlightRotationAxis.z)
                        .normalize();

                var leftLight = spotLights.get(0);
                var rightLight = spotLights.get(1);

                leftLight.setLightPosition(new Vector3f(spaceShipPosition)
                        .add(new Vector3f(worldUp).mul(0.1f))
                        .sub(new Vector3f(spotlightRotationAxis).mul(0.15f))
                        .add(new Vector3f(spotLightDirection).mul(0.1f)));
                leftLight.setDirection(spotLightDirection);

                rightLight.setLightPosition(new Vector3f(spaceShipPosition)
                        .add(new Vector3f(worldUp).mul(0.1f))
                        .add(new Vector3f(spotlightRotationAxis).mul(0.15f))
                        .add(new Vector3f(spotLightDirection).mul(0.1f)));
                rightLight.setDirection(spotLightDirection);

                // Neptune
                model = new Matrix4f()
                        .rotate((float) glfwGetTime(), new Vector3f(0f, 1f, 0f))
                        .scale(0.8f);
                currentShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                neptune.draw(currentShader);

                // apply uniforms for lights
                pointLights.forEach(light -> light.applyUniforms(currentShader));
                spotLights.forEach(light -> light.applyUniforms(currentShader));
                directionalLights.forEach(light -> light.applyUniforms(currentShader));

                // draw UFOs
                pointLights.forEach(pointLight -> pointLight.draw(currentShader));
            }
            window.nextFrame();
        }
        window.delete();
    }

    private static void setupLights() {
        Model lamp = new Model(Animation.class.getResource("Low_poly_UFO.obj").getPath());
        DirectionalLight directionalLight = DirectionalLight.builder()
                .direction(new Vector3f(0, -1f, 0))
                .ambient(directionalLightColor)
                .diffuse(directionalLightColor)
                .specular(directionalLightColor)
                .build();
        directionalLights.add(directionalLight);
        //  PointLights
        Vector3f[] pointLightPositions = {
                new Vector3f(new Vector3f(-8f, 2f, -8f)),
                new Vector3f(new Vector3f(10f, 0, -10f)),
                new Vector3f(new Vector3f(10f, 3f, 10f)),
                new Vector3f(new Vector3f(-7f, 0, 7f)),
                new Vector3f(new Vector3f(9f, -2f, 0)),
        };
        int index = 0;
        for (var position : pointLightPositions) {
            PointLight pointLight = PointLight.builder()
                    .index(index++)
                    .model(lamp)
                    .modelPosition(position)
                    .lightPosition(new Vector3f(0, 1.35f, 0).add(position))
                    .ambient(new Vector3f(0.005f, 0.005f, 0.005f))
                    .diffuse(new Vector3f(0.4f, 0.4f, 0.4f))
                    .specular(new Vector3f(0.1f, 0.1f, 0.1f))
                    .constant(0.1f)
                    .linear(0.075f)
                    .quadratic(0.0175f)
                    .build();
            pointLights.add(pointLight);
        }
        // SpotLights
        IntStream.range(0, 3).forEach(i -> {
            SpotLight spotLight = SpotLight.builder()
                    .index(i)
                    .lightPosition(new Vector3f(-10, 1, -5))
                    .direction(new Vector3f(0, -1f, 0))
                    .ambient(new Vector3f(0.15f, 0.15f, 0.10f))
                    .diffuse(new Vector3f(0.3f, 0.3f, 0.2f))
                    .specular(new Vector3f(0.3f, 0.53f, 0.2f))
                    .constant(0.1f)
                    .linear(0.1f)
                    .quadratic(0.0175f)
                    .cutOff((float) cos(toRadians(13)))
                    .outerCutOff((float) cos(toRadians(20)))
                    .build();
            spotLights.add(spotLight);
        });
    }

    private static void processInput() {
        if(window.checkIfPressed(GLFW_KEY_1)) {
            camera.stopProcessingMouseMovement(window.getHandler());
            cameraMovementType = STATIC;
            camera.setCameraPos(new Vector3f(-1.9470121f, 7.8995733f, 25.64252f));
            camera.setCameraFront( new Vector3f(0.110537454f, -0.3583683f, -0.9270134f).normalize());
        } else if (window.checkIfPressed(GLFW_KEY_2)) {
            camera.stopProcessingMouseMovement(window.getHandler());
            cameraMovementType = FOLLOW;
        } else if (window.checkIfPressed(GLFW_KEY_3)) {
            camera.stopProcessingMouseMovement(window.getHandler());
            cameraMovementType = FPV;
        } else if (window.checkIfPressed(GLFW_KEY_4)) {
            cameraMovementType = FREE;
        } else if (window.checkIfPressed(GLFW_KEY_LEFT_CONTROL) && window.checkIfPressed(GLFW_KEY_UP)) {
            // fog factor up
            fogFactor += deltaTime * 0.5f;
            fogFactor = fogFactor > 1 ? 1 : fogFactor;
        } else if (window.checkIfPressed(GLFW_KEY_LEFT_CONTROL) && window.checkIfPressed(GLFW_KEY_DOWN)) {
            // fog factor down
            fogFactor -= deltaTime * 0.5f;
            fogFactor = fogFactor < 0 ? 0 : fogFactor;
        }else if (window.checkIfPressed(GLFW_KEY_LEFT_SHIFT) && window.checkIfPressed(GLFW_KEY_UP)) {
            // light intensity up
            float lightIntensity = directionalLightColor.x;
            lightIntensity += deltaTime * 0.5f;
            lightIntensity = Math.min(lightIntensity, 0.4f);
            directionalLightColor.set(lightIntensity, lightIntensity, lightIntensity);
        } else if (window.checkIfPressed(GLFW_KEY_LEFT_SHIFT) && window.checkIfPressed(GLFW_KEY_DOWN)) {
            // light intensity down
            float lightIntensity = directionalLightColor.x;
            lightIntensity -= deltaTime * 0.5f;
            lightIntensity = Math.max(0, lightIntensity);
            directionalLightColor.set(lightIntensity, lightIntensity, lightIntensity);
        } else if (window.checkIfPressed(GLFW_KEY_UP)) {
            spotLightRotation += deltaTime * 10f;
            spotLightRotation = spotLightRotation > 30 ? 30 : spotLightRotation;
        } else if (window.checkIfPressed(GLFW_KEY_DOWN)) {
            spotLightRotation -= deltaTime * 10f;
            spotLightRotation = spotLightRotation < -30 ? -30 : spotLightRotation;
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