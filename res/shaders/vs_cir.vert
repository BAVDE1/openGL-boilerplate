#version 450 core

// instanced
layout(location = 0) in vec2 circlePosition;
layout(location = 1) in float radius;
layout(location = 2) in float innerRadius;
layout(location = 3) in vec3 colour;

uniform vec2 resolution;
uniform highp float time;

out vec2 v_circlePos;
out float v_radius;
out float v_innerRadius;
out vec3 v_colour;

/**
 https://en.wikipedia.org/wiki/Orthographic_projection
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

vec2 TRI_POSITIONS[3] = vec2[3] (
        vec2(0,       -2),
        vec2(1.7321,   1),
        vec2(-1.7321,  1)
);

void main() {
    vec2 radiusMultiplier = TRI_POSITIONS[gl_VertexID % 3];
    vec2 pos = vec2(
        circlePosition.x + (radius * radiusMultiplier.x),
        circlePosition.y + (radius * radiusMultiplier.y)
    );
    gl_Position = vec4(pos, 1, 1) * projectionMatrix;

    v_circlePos = circlePosition;
    v_radius = radius;
    v_innerRadius = innerRadius;
    v_colour = colour;
}