#version 450 core

// instanced
layout(location = 0) in vec2 circlePosition;
layout(location = 1) in float radius;
layout(location = 2) in float innerRadius;
layout(location = 3) in vec3 colour;

uniform vec2 resolution;
uniform mat4 projectionMatrix;

uniform highp float time;
uniform vec2 viewPos;
uniform float viewScale;

out vec2 v_circlePos;
out float v_radius;
out float v_innerRadius;
out vec3 v_colour;

vec2 TRI_POSITIONS[3] = vec2[3] (
        vec2(0,       -2),
        vec2(1.7321,   1),
        vec2(-1.7321,  1)
);

void main() {
    vec2 circlePos = (circlePosition - viewPos) / viewScale;
    float scaledRadius = abs(radius / viewScale);

    vec2 radiusMultiplier = TRI_POSITIONS[gl_VertexID % 3];
    vec2 pos = vec2(
        circlePos.x + (scaledRadius * radiusMultiplier.x),
        circlePos.y + (scaledRadius * radiusMultiplier.y)
    );
    gl_Position = vec4(pos, 1, 1) * projectionMatrix;

    v_circlePos = vec2(circlePos.x, resolution.y - circlePos.y);
    v_radius = scaledRadius;
    v_innerRadius = abs(innerRadius / viewScale);
    v_colour = colour;
}