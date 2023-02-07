package org.example.manager;

import org.example.Animation;
import org.example.lights.*;
import org.example.shader.Shader;
import org.example.shader.fog.Fog;
import org.example.models.Model;

import org.joml.Vector3f;
import java.util.List;
import java.util.ArrayList;
import static java.lang.Math.*;

public class LightManager {
    private List<PointLight> pointLights = new ArrayList<>();
    private List<SpotLight> spotLights = new ArrayList<>();
    private List<DirectionalLight> directionalLights = new ArrayList<>();
    private Vector3f directionalLightColor = new Vector3f(0.15f, 0.15f, 0.15f);

    private float spotLightRotation = -5f;
    private float fogFactor = 0f;
    private Fog fog = Fog.builder().color(directionalLightColor).build();

    public SpotLight getSpotLight(int id) {
        return spotLights.get(id);
    }

    public float rotation() {
        return spotLightRotation;
    }

    public void init() {
        Model ufo = new Model(Animation.class.getResource("Low_poly_UFO.obj").getPath());
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
                    .model(ufo)
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
         for(int i = 0; i < 3; i++){
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
        }
    }

    public void useFog(Shader currentShader) {
        fog.setDensity(fogFactor);
        fog.applyFog(currentShader);
    }

    public void increaseFog(float dt) {
        fogFactor += dt * 0.5f;
        fogFactor = fogFactor > 1 ? 1 : fogFactor;
    }

    public void decreaseFog(float dt) {
        fogFactor -= dt * 0.5f;
        fogFactor = fogFactor < 0 ? 0 : fogFactor;
    }

    public void increaseLightIntensity(float dt) {
        float lightIntensity = directionalLightColor.x;
        lightIntensity += dt * 0.5f;
        lightIntensity = Math.min(lightIntensity, 0.4f);
        directionalLightColor.set(lightIntensity, lightIntensity, lightIntensity);
    }

    public void decreaseLightIntensity(float dt) {
        float lightIntensity = directionalLightColor.x;
        lightIntensity -= dt * 0.5f;
        lightIntensity = Math.max(0, lightIntensity);
        directionalLightColor.set(lightIntensity, lightIntensity, lightIntensity);
    }

    public void liftSpotLights(float dt) {
        spotLightRotation += dt * 10f;
        spotLightRotation = spotLightRotation > 30 ? 30 : spotLightRotation;
    }

    public void lowerSpotLights(float dt) {
        spotLightRotation -= dt * 10f;
        spotLightRotation = spotLightRotation < -30 ? -30 : spotLightRotation;
    }

    public void applyLighting(Shader currentShader) {

        // apply uniforms for shaders
        pointLights.forEach(light -> light.apply(currentShader));
        spotLights.forEach(light -> light.apply(currentShader));
        directionalLights.forEach(light -> light.apply(currentShader));

        // draw ufos - lamps
        pointLights.forEach(pointLight -> pointLight.draw(currentShader));
    }
}
