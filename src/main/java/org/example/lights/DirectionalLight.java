package org.example.lights;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.shader.Shader;
import org.joml.Vector3f;

@Getter
@Setter
@SuperBuilder
public class DirectionalLight extends AbstractLight{
    protected Vector3f direction;

    @Override
    public void applyUniforms(Shader shader) {
        super.applyUniforms(shader);
        shader.setVec3(name() + ".direction", direction);
    }

    @Override
    public String name() {
        return "dirLight";
    }
}
