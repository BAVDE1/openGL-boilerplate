#version 330

uniform vec2 resolution;
uniform highp float time;

out vec4 colour;

vec2 invResolution = 1 / resolution;

void main() {
    vec2 uv = gl_FragCoord.xy * invResolution;
    colour = vec4(uv.xy, abs(sin(time - length(uv))), 1);
}