#version 100
#ifdef GL_ES
    precision highp float;
#endif

uniform vec2 resolution;
uniform highp float time;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    gl_FragColor = vec4(uv.xy, abs(sin(time)), 1);
}