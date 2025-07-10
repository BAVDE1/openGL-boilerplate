//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texCoords;
layout(location = 3) in ivec4 boneIds;
layout(location = 4) in vec4 boneWeights;
layout(location = 5) in int isStatic;

layout (std140) uniform CameraView {
    mat4 projection;
    mat4 view;
};

const int MAX_BONES = 100;
const int MAX_BONE_INFLUENCE = 4;

uniform mat4 model;
uniform mat4 finalBonesMatrices[MAX_BONES];

out vec3 v_fragPos;
out vec3 v_normal;
out vec2 v_texCoords;

void main() {
    mat4 animTransformation = mat4(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
    ) * isStatic;  // only use identity if its not static (ie if its not animated)
    for (int i = 0; i < MAX_BONE_INFLUENCE; i++) {
        if (boneIds[i] == -1) continue;
        if (boneIds[i] >= MAX_BONES) break;
        animTransformation += finalBonesMatrices[boneIds[i]] * boneWeights[i];
    }

    vec4 finalPos = animTransformation * vec4(pos, 1);
    gl_Position = projection * view * (model * finalPos);

    v_fragPos = vec3(model * finalPos);
    v_normal = mat3(transpose(inverse(model * animTransformation))) * normal;
    v_texCoords = texCoords;
}

//--- FRAG
#version 450 core

struct Material {
    sampler2D diffuseTexture;

    vec3 specular;
    float shininess;
};

struct PointLight {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

struct DirectionalLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

vec3 calcPointLight(PointLight light, vec3 normal, vec3 diffuseTexture);
vec3 calcDirectionLighting(vec3 normal, vec3 diffuseTexture);

const int LIGHT_COUNT = 2;

uniform Material material;
uniform PointLight lights[LIGHT_COUNT];
uniform DirectionalLight skyLight;
uniform vec3 viewPos;

in vec3 v_fragPos;
in vec3 v_normal;
in vec2 v_texCoords;

out vec4 colour;

void main() {
    vec3 norm = normalize(v_normal);
    vec3 diffuseTexture = texture(material.diffuseTexture, v_texCoords).xyz;

    vec3 finalCol = vec3(0);
    finalCol += calcDirectionLighting(norm, diffuseTexture);
    for (int i = 0; i < LIGHT_COUNT; i++) {
        finalCol += calcPointLight(lights[i], norm, diffuseTexture);
    }

    colour = vec4(finalCol, 1);
}

vec3 calcPointLight(PointLight light, vec3 normal, vec3 diffuseTexture) {
    float distance = length(light.position - v_fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));

    // diffuse
    vec3 lightDir = normalize(light.position - v_fragPos);
    float diff = max(dot(lightDir, normal), 0);

    // specular
    vec3 viewDir = normalize(viewPos - v_fragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0), material.shininess);

    vec3 ambient = light.ambient * diffuseTexture;
    vec3 diffuse = light.diffuse * (diff * diffuseTexture);
    vec3 specular = light.specular * (spec * material.specular);
    return vec3(ambient * attenuation + diffuse * attenuation + specular * attenuation);
}

vec3 calcDirectionLighting(vec3 normal, vec3 diffuseTexture) {
    // ambient
    vec3 ambient = skyLight.ambient * diffuseTexture;

    // diffuse
    vec3 lightDir = normalize(-skyLight.direction);
    float diff = max(dot(lightDir, normal), 0);
    vec3 diffuse = (diff * diffuseTexture) * skyLight.diffuse;

    // specular
    vec3 viewDir = normalize(viewPos - v_fragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0), material.shininess);
    vec3 specular = (spec * material.specular) * skyLight.specular;
    return vec3(ambient + diffuse + specular);
}
