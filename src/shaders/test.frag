#version 330 core

uniform vec2 resolution;
uniform highp float time;

out vec4 colour;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    colour = vec4(uv.xy, abs(sin(time)), 1);
}