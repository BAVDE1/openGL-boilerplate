//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;

layout (std140) uniform CameraView {
    mat4 projection;
    mat4 view;
};

uniform mat4 model;

out vec3 v_fragPos;
out vec3 v_normal;
out vec2 v_texCoords;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1);
    v_fragPos = vec3(model * vec4(pos, 1));
    v_normal = mat3(transpose(inverse(model))) * normal;
    v_texCoords = pos.xy + .5;
}

//--- FRAG
#version 450 core

struct Material {
    sampler2D diffuseTexture;
    sampler2D specularMap;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform Material material;
uniform Light light;
uniform vec3 viewPos;

in vec3 v_fragPos;
in vec3 v_normal;
in vec2 v_texCoords;

out vec4 colour;

void main() {
    vec3 diffuseTexture = texture(material.diffuseTexture, v_texCoords).xyz;
    vec3 specularMap = texture(material.specularMap, v_texCoords).xyz;

    // ambient
    vec3 ambient = light.ambient * diffuseTexture;

    // diffuse
    vec3 norm = normalize(v_normal);
    vec3 lightDir = normalize(v_fragPos - light.position);
    float diff = max(dot(lightDir, norm), 0);
    vec3 diffuse = (diff * diffuseTexture) * light.diffuse;

    // specular
    vec3 viewDir = normalize(v_fragPos - viewPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0), material.shininess);
    vec3 specular = (spec * specularMap) * light.specular;

    colour = vec4(ambient + diffuse + specular, 1);
}