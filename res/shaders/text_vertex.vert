#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec3 textureVars;

uniform mat4 projectionMatrix;

out vec3 v_textureVars;

void main() {
    gl_Position = vec4(pos.xy, 1, 1) * projectionMatrix;
    v_textureVars = textureVars;
}