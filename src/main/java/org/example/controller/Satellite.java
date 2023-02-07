package org.example.controller;

import org.example.Animation;
import org.example.models.Model;
import org.example.lights.SpotLight;
import org.example.shader.Shader;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;

public class Satellite extends Controller {
    private final Model satellite = new Model(Animation.class.getResource("SpaceStation.obj").getPath());
    private SpotLight satelliteLight;

    public Satellite(SpotLight satelliteLight) {
        this.satelliteLight = satelliteLight;
    }

    @Override
    public void generateFrame(Shader currentShader, float spotLightRotation, float frame) {
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

        satelliteLight.setLightPosition(result);
        satelliteLight.setDirection(SatelliteSpotLightDirection);
    }

}
