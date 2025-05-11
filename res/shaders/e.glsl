//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 col;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 v_col;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1);
    v_col = col;
}

//--- FRAG
#version 450 core

in vec3 v_col;

out vec4 colour;

void main() {
    colour = vec4(v_col, 1);
}