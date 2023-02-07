package org.example.controller;

import org.example.models.Model;
import org.example.shader.Shader;
import org.example.lights.SpotLight;
import org.example.Animation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static java.lang.Math.*;

import org.lwjgl.system.MemoryStack;
public class SpaceShip extends Controller {
    private final Model spaceShip = new Model(Animation.class.getResource("SciFi_Fighter_AK5.obj").getPath());
    private List<Vector3f[]> positions;

    private SpotLight rightLight, leftLight;
    private boolean isInitialized = false;

    public SpaceShip(SpotLight rightLight, SpotLight leftLight) {
        try {
            positions = Files.readAllLines(new File(Animation.class.getResource("positions.txt").getPath()).toPath()).stream()
                    .map(line -> line.split(" "))
                    .map(l-> new Vector3f[] {
                            new Vector3f(Float.parseFloat(l[0]), Float.parseFloat(l[1]), Float.parseFloat(l[2])),
                            new Vector3f(Float.parseFloat(l[3]), Float.parseFloat(l[4]), Float.parseFloat(l[5]))
                    }).collect(Collectors.toList());
        } catch(IOException e) {
            System.out.println("Failed to read spaceship's path");
            isInitialized = false;
            return;
        }

        this.rightLight = rightLight;
        this.leftLight = leftLight;

        isInitialized = true;
    }
    public boolean isInitialized() {
        return isInitialized;
    }

    public Vector3f getShipPosition(float frame) {
        int frameId = (int)frame % positions.size();
        return positions.get(frameId)[0];
    }

    public Vector3f getShipFront(float frame) {
        int frameId = (int)frame % positions.size();
        return positions.get(frameId)[1];
    }
     public Vector2f getNextShipFront(float frame) {
         int frameId = (int)frame % positions.size();
        var spaceShipFrontCameraPosition = getShipFront(frameId);
        return new Vector2f(spaceShipFrontCameraPosition.x, spaceShipFrontCameraPosition.z).normalize();
     }

     @Override
    public void generateFrame(Shader currentShader, float spotLightRotation, float frame) {

        int frameId = (int)frame % positions.size();
        var spaceShipPosition = positions.get(frameId)[0];
        var spaceShipFrontCameraPosition = positions.get(frameId)[1];
        var nextShipCameraFront = new Vector2f(spaceShipFrontCameraPosition.x, spaceShipFrontCameraPosition.z).normalize();


        var model = new Matrix4f()
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
    }
}
