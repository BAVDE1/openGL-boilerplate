//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;

uniform mat4 projectionMatrix;

void main() {
    gl_Position = vec4(pos.xy, 1, 1) * projectionMatrix;
}

//--- FRAG
#version 450 core

out vec4 colour;

void main() {
    colour = vec4(1, 1, 1, 1);
}