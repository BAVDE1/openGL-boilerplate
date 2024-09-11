#version 330 core

layout(location = 0) in vec4 position;

uniform vec2 resolution;
uniform highp float time;

/**
 l = 0     r = width
 b = 0     t = height
 n = -1    f = 1
*/
mat4 projectionMatrix = mat4(
    2/resolution.x, 0,               0, -1,
    0,              2/-resolution.y, 0,  1,
    0,              0,              -1,  0,
    0,              0,               0,  1
);

void main() {
    vec4 pos = position;
    float t = time + length(position);
    pos += vec4(20 * sin(t), 20 * cos(t), 0, 0);
    gl_Position = pos * projectionMatrix;
}