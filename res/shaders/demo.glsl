//--- VERT
#version 450 core

uniform vec2 resolution;
uniform mat4 projectionMatrix;

uniform highp float time;
uniform vec2 viewPos;
uniform float viewScale;

layout(location = 0) in vec2 position;
layout(location = 1) in float mode;
layout(location = 2) in vec3 modeVars;

out float v_mode;
out vec3 v_modeVars;

void main() {
    vec4 pos = vec4((position.xy - viewPos.xy) / viewScale, 1, 1);
    gl_Position = pos * projectionMatrix;

    v_mode = mode;
    v_modeVars = modeVars;
}

//--- FRAG
#version 450 core

uniform vec2 resolution;
uniform highp float time;
uniform int debugMode;
uniform sampler2D textures[16];

const float EPSILON = 0.0001;

in float v_mode;
in vec3 v_modeVars;

out vec4 colour;

vec2 invResolution = 1 / resolution;

void doColourfull() {
    vec2 uv = gl_FragCoord.xy * invResolution;
    colour = vec4(uv.xy, abs(sin(time - length(uv))), 1);
}

void main() {
    int m = int(v_mode);

    if (m == 0) {
        colour = vec4(1, 1, 1, .3);
        return;
    }

    if (m == 1) {
        int slot = int(v_modeVars.z);
        colour = texture(textures[slot], v_modeVars.xy);
        return;
    }

    if (m == 2) {
        colour = vec4(v_modeVars, 1);
        return;
    }

    if (m == 3) {
        doColourfull();
    }
}