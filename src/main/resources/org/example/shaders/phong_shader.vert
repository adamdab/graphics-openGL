#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 normal;
out vec3 fragPos;
out vec2 TexCoords;
out vec4 eyeSpacePosition;

void main()
{
    mat4 mvMatrix = view * model;
    eyeSpacePosition = mvMatrix * vec4(aPos, 1.0f);
    gl_Position = projection * eyeSpacePosition;

    normal = mat3(model) * aNormal;

    fragPos = vec3(model * vec4(aPos, 1.0f));
    TexCoords = aTexCoords;
}