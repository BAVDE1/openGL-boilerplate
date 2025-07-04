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

//layout (std140) uniform CameraView {
//    mat4 projection;
//    mat4 view;
//};

uniform vec3 objectColour;
uniform vec3 lightColour;
uniform vec3 lightPos;
uniform vec3 viewPos;

const float ambientStrength = .15;
const float specularStrength = .5;

in vec3 v_fragPos;
in vec3 v_normal;

out vec4 colour;

void main() {
    // ambient
    vec3 ambient = lightColour * ambientStrength;

    // diffuse
    vec3 norm = normalize(v_normal);
//    vec3 lightDir = normalize(v_fragPos - (view * vec4(lightPos, 1)).xyz);  // view space
    vec3 lightDir = normalize(v_fragPos - lightPos);
    float diff = max(dot(lightDir, norm), 0);
    vec3 diffuse = diff * lightColour;

    // specular
    vec3 viewDir = normalize(v_fragPos - viewPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0), 64);
    vec3 specular = specularStrength * spec * lightColour;

    colour = vec4((ambient + diffuse + specular) * objectColour, 1);
}