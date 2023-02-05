package org.example.lights;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.shader.Shader;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

@Getter
@Setter
@SuperBuilder
public class PointLight extends AbstractLightWithPosition {

    @Override
    public void draw(Shader shader) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var model = new Matrix4f()
                    .translate(modelPosition)
                    .scale(0.02f);
            shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
            this.model.draw(shader);
        }
    }

    @Override
    public String name() {
        return "pointLights[" + index + "]";
    }
}
