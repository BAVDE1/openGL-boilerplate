#version 450 core

uniform vec2 resolution;
uniform int debugMode;

const float EPSILON = 0.0001;

in float v_radius;
in vec3 v_colour;

out vec4 colour;

vec2 invResolution = 1 / resolution;

void main() {
    colour = vec4(v_colour, 0);
}