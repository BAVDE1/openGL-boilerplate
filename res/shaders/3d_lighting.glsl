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

void main() {
    gl_Position = projection * view * model * vec4(pos, 1);
    v_fragPos = vec3(model * vec4(pos, 1));
    v_normal = mat3(transpose(inverse(model))) * normal;
}

//--- FRAG
#version 450 core

struct Material {
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

out vec4 colour;

void main() {
    // ambient
    vec3 ambient = light.ambient * material.ambient;

    // diffuse
    vec3 norm = normalize(v_normal);
    vec3 lightDir = normalize(v_fragPos - light.position);
    float diff = max(dot(lightDir, norm), 0);
    vec3 diffuse = (diff * material.diffuse) * light.diffuse;

    // specular
    vec3 viewDir = normalize(v_fragPos - viewPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0), material.shininess);
    vec3 specular = (spec * material.specular) * light.specular;

    colour = vec4(ambient + diffuse + specular, 1);
}