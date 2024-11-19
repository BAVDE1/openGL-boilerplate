#version 450 core

layout(location = 0) in vec2 position;
layout(location = 1) in float mode;
layout(location = 2) in vec3 modeVars;

uniform vec2 resolution;
uniform mat4 projectionMatrix;

uniform highp float time;
uniform vec2 viewPos;
uniform float viewScale;
uniform int useView;

out float v_mode;
out vec3 v_modeVars;

void main() {
    vec4 pos = vec4((position.xy - (viewPos.xy * useView)) / (useView == 0 ? 1.:viewScale), 1, 1);
//    float t = time + length(position.xy) * .01;
//    pos += vec4(20 * sin(t), 20 * cos(t), 0, 0);
    gl_Position = pos * projectionMatrix;

    v_mode = mode;
    v_modeVars = modeVars;
}