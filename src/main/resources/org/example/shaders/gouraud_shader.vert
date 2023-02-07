#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

struct Material
{
    sampler2D texture_diffuse1;
    sampler2D specular;
    vec3 diffuseColor;
};

struct DirectionalLight
{
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct PointLight
{
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct SpotLight
{
    vec3 position;
    vec3 direction;
    float cutOff;
    float outerCutOff;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct Fog
{
    vec3 color;
    float density;
};

#define NR_POINT_LIGHTS 5
#define NR_SPOT_LIGHTS 3

uniform Material material;
uniform DirectionalLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotLights[NR_SPOT_LIGHTS];
uniform Fog fogParameters;
uniform vec3 viewPos;
uniform int useTexture;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 fragColor;

vec3 CalcDirLight(DirectionalLight light, vec3 normal, vec3 viewDir, vec3 diffuseColor);
vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 diffuseColor);
vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 diffuseColor);
float getFogFactor(Fog params, float fogCoordinate);

void main()
{
    mat4 mvMatrix = view * model;
    vec4 eyeSpacePosition = mvMatrix * vec4(aPos, 1.0f);
    gl_Position = projection * eyeSpacePosition;

    vec3 normal = mat3(model) * aNormal;

    vec3 fragPos = vec3(model * vec4(aPos, 1.0f));
    vec2 TexCoords = aTexCoords;

    vec3 diffuseColor = useTexture > 0 ? vec3(texture(material.texture_diffuse1, TexCoords)) : material.diffuseColor;
    vec3 norm = normalize(normal);
    vec3 viewDir = normalize(viewPos - fragPos);

    // Directional lighting
    vec3 result = CalcDirLight(dirLight, norm, viewDir, diffuseColor);
    // Point lightsf
    for(int i = 0; i < NR_POINT_LIGHTS; i++)
    result += CalcPointLight(pointLights[i], norm, fragPos, viewDir, diffuseColor);
    // Spot lights
    for(int i = 0; i < NR_SPOT_LIGHTS; i++)
    result += CalcSpotLight(spotLights[i], norm, fragPos, viewDir, diffuseColor);
    // fog
    float fogCoordinate = abs(eyeSpacePosition.z / eyeSpacePosition.w);
    result = mix(result, fogParameters.color, getFogFactor(fogParameters, fogCoordinate));

    fragColor = vec4(result, 1.0);
}

vec3 CalcDirLight(DirectionalLight light, vec3 normal, vec3 viewDir, vec3 diffuseColor)
{
    vec3 lightDir = normalize(-light.direction);

    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);

    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);

    // combine results
    vec3 ambient  = light.ambient  * diffuseColor;
    vec3 diffuse  = light.diffuse  * diff * diffuseColor;
    vec3 specular = light.specular * spec * diffuseColor;

    return (ambient + diffuse + specular);
}

vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 diffuseColor)
{
    vec3 lightDir = normalize(light.position - fragPos);

    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);

    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);

    // attenuation
    float distance    = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance +
    light.quadratic * (distance * distance));

    // combine results
    vec3 ambient  = light.ambient  * diffuseColor;
    vec3 diffuse  = light.diffuse  * diff * diffuseColor;
    vec3 specular = light.specular * spec * diffuseColor;

    ambient  *= attenuation;
    diffuse  *= attenuation;
    specular *= attenuation;

    return (ambient + diffuse + specular);
}

vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 diffuseColor)
{
    vec3 lightDir = normalize(light.position - fragPos);

    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);

    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);

    // attenuation
    float distance = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));

    // spotlight intensity
    float theta = dot(lightDir, normalize(-light.direction));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);

    // combine results
    vec3 ambient = light.ambient * diffuseColor;
    vec3 diffuse = light.diffuse * diff * diffuseColor;
    vec3 specular = light.specular * spec * diffuseColor;

    ambient *= attenuation * intensity;
    diffuse *= attenuation * intensity;
    specular *= attenuation * intensity;

    return (ambient + diffuse + specular);
}

float getFogFactor(Fog params, float fogCoordinate)
{
    float result = 0.0;
    result = exp(-params.density * fogCoordinate);
    result = 1.0 - clamp(result, 0.0, 1.0);
    return result;
}