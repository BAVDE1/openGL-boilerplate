#version 100
#ifdef GL_ES
    precision mediump float;
#endif

uniform vec2 resolution;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    gl_FragColor = vec4(uv.xy, 0, 1);
}