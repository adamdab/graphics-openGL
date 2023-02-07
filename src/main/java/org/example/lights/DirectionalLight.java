package org.example.lights;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.shader.Shader;
import org.joml.Vector3f;

@Getter
@Setter
@SuperBuilder
public class DirectionalLight extends Light{
    protected Vector3f direction;

    @Override
    public void apply(Shader shader) {
        super.apply(shader);
        shader.setVec3(name() + ".direction", direction);
    }

    @Override
    public String name() {
        return "dirLight";
    }
}
