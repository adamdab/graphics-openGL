package org.example.lights;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.shader.Shader;
import org.joml.Vector3f;

@Getter
@Setter
@SuperBuilder
public class SpotLight extends LightWithPosition {
    protected Vector3f direction;
    protected float cutOff;
    protected float outerCutOff;

    @Override
    public void apply(Shader shader) {
        super.apply(shader);
        shader.setVec3(name() + ".direction", direction);
        shader.setFloat(name() + ".cutOff", cutOff);
        shader.setFloat(name() + ".outerCutOff", outerCutOff);
    }

    @Override
    public void draw(Shader shader) {

    }

    @Override
    public String name() {
        return "spotLights[" + index + "]";
    }
}
