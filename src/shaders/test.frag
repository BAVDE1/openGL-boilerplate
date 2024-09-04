#version 100
#ifdef GL_ES
    precision mediump float;
#endif

uniform vec2 resolution;

void main() {
    gl_FragColor = vec4(gl_FragCoord.xy / resolution.xy, 0, 1);
}