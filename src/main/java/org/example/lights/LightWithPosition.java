package org.example.lights;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.shader.Shader;
import org.example.models.Model;
import org.joml.Vector3f;

@Getter @Setter
@SuperBuilder
public abstract class LightWithPosition extends Light {
    private static final Vector3f lightColor = new Vector3f(1, 1, 1);
    protected Vector3f lightPosition;
    protected Vector3f modelPosition;
    protected Model model;
    protected float constant;
    protected float linear;
    protected float quadratic;
    protected int vao;

    @Override
    public void apply(Shader shader) {
        super.apply(shader);
        shader.setVec3(name() + ".position", lightPosition);
        shader.setFloat(name() + ".constant", constant);
        shader.setFloat(name() + ".linear", linear);
        shader.setFloat(name() + ".quadratic", quadratic);
    }

    public abstract void draw(Shader shader);
}
