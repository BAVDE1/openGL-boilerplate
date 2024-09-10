#version 330 core

layout(location = 0) in vec4 position;

uniform vec2 resolution;
uniform highp float time;

mat4 projectionMatrix = mat4(
    2/resolution.x, 0,               0, -1,
    0,              2/resolution.y,  0, -1,
    0,              0,              -1,  0,
    0,              0,               0,  1
);

void main() {
    vec4 pos = position;
    pos += vec4(50 * sin(time), 50 * cos(time), 0, 0);
    gl_Position = pos * projectionMatrix;
}