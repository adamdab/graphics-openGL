package org.example.lights;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.shader.Shader;
import org.joml.Vector3f;
/***
 *
 */
@Getter @Setter
@SuperBuilder
public abstract class AbstractLight {
    @Builder.Default
    protected int index = 0;
    protected Vector3f ambient;
    protected Vector3f diffuse;
    protected Vector3f specular;
    public void applyUniforms(Shader shader) {
        shader.setVec3(name() + ".ambient", ambient);
        shader.setVec3(name() + ".diffuse", diffuse);
        shader.setVec3(name() + ".specular", specular);
    }

    public abstract String name();
}
