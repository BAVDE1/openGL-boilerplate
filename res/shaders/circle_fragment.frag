#version 450 core

uniform vec2 resolution;
uniform int debugMode;

const float EPSILON = 0.0001;
const int smoothness = 1;

in vec2 v_circlePos;
in float v_radius;
in float v_innerRadius;
in vec3 v_colour;

out vec4 colour;

vec2 invResolution = 1 / resolution;

void main() {
    float dist = length(gl_FragCoord.xy - v_circlePos);

    if (debugMode == 0 && dist > v_radius) discard;

    colour = vec4(v_colour, smoothstep(v_radius, v_radius - smoothness, dist));

    if (v_innerRadius > 0) {
        if (debugMode == 0 && dist < v_radius - v_innerRadius) discard;
        colour.a *= smoothstep(v_radius - v_innerRadius, v_radius - (v_innerRadius - smoothness), dist);
    }

    colour.a += .3 * debugMode;
}