#version 100
#ifdef GL_ES
    precision mediump float;
#endif

vec2 resolution = vec2(500, 400);

void main() {
    gl_FragColor = vec4(gl_FragCoord.x / resolution.x, 0, 0, 1);
//    gl_FragColor = gl_Color;
}