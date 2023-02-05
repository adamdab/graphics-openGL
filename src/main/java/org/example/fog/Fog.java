package org.example.fog;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.shader.Shader;
import org.joml.Vector3f;

@Builder
@Getter @Setter
public class Fog {
    private Vector3f color;
    private float density;

    public void applyFog(Shader shader) {
        shader.setVec3("fogParameters.color", color);
        shader.setFloat("fogParameters.density", density);
    }
}
