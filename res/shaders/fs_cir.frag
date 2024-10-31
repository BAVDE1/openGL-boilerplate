#version 450 core

uniform vec2 resolution;
uniform int debugMode;

const float EPSILON = 0.0001;

in vec2 v_circlePos;
in float v_radius;
in float v_innerRadius;
in vec3 v_colour;

out vec4 colour;

vec2 invResolution = 1 / resolution;

void main() {
    float dist = length((gl_FragCoord.xy * invResolution) - v_circlePos);

    if (dist < v_radius) {
        discard;
    }
    colour = vec4(v_colour, 1);
}