//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;

layout (std140) uniform CameraView {
    mat4 projection;
    mat4 view;
};

uniform mat4 model;

out vec3 v_texPos;
out vec3 v_position;
out vec3 v_normal;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1);
    v_texPos = pos;

    // reflection values
    v_position = vec3(model * vec4(pos, 1));
    v_normal = mat3(transpose(inverse(model))) * normal;
}

//--- FRAG
#version 450 core

uniform samplerCube skyBoxTexture;
uniform vec3 camPos;

in vec3 v_texPos;
in vec3 v_position;
in vec3 v_normal;

out vec4 colour;

void main() {
    vec3 I = normalize(v_position - camPos);
    vec3 R = reflect(I, normalize(v_normal));
    colour = vec4(texture(skyBoxTexture, R).rgb, 1);
//    colour = vec4(v_texPos.x, v_texPos.y, v_texPos.z, 1);
//    colour = vec4(abs(v_normal.x), abs(v_normal.y), abs(v_normal.z), 1);
}