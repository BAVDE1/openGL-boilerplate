//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 texPos;

layout (std140) uniform CameraView {
    mat4 projection;
    mat4 view;
};

uniform mat4 model;

out vec3 v_texPos;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1);
    v_texPos = texPos;
}

//--- FRAG
#version 450 core

uniform samplerCube theTexture;

in vec3 v_texPos;

out vec4 colour;

void main() {
    colour = vec4(texture(theTexture, v_texPos).xyz, 1);
//    colour = vec4(v_texPos.x, v_texPos.y, v_texPos.z, 1);
}