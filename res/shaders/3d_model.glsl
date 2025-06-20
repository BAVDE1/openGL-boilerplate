//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;

layout (std140) uniform CameraView {
    mat4 projection;
    mat4 view;
};

//uniform mat4 model;

void main() {
    gl_Position = projection * view * vec4(pos, 1);
}

//--- FRAG
#version 450 core

out vec4 colour;

void main() {
    colour = vec4(0, 1, 1, 1);
//    colour = vec4(v_texPos.x, v_texPos.y, v_texPos.z, 1);
}