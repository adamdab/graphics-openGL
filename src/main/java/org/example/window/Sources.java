package org.example.window;

import org.example.Animation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sources {
    private  final String phongVertexShaderSource = Animation.class.getResource("shaders/phong_shader.vert").getFile();
    private  final String phongFragmentShaderSource = Animation.class.getResource("shaders/phong_shader.frag").getFile();
    private  final String gouraudVertexShaderSource = Animation.class.getResource("shaders/gouraud_shader.vert").getFile();
    private  final String gouraudFragmentShaderSource = Animation.class.getResource("shaders/gouraud_shader.frag").getFile();
    private  final String flatVertexShaderSource = Animation.class.getResource("shaders/flat_shader.vert").getFile();
    private  final String flatFragmentShaderSource = Animation.class.getResource("shaders/flat_shader.frag").getFile();
}
