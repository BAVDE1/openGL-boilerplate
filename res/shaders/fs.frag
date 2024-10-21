#version 450 core

uniform vec2 resolution;
uniform highp float time;
uniform int debugMode;

uniform sampler2D textures[3];

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
        colour = vec4(1, 1, 1, .5);
        return;
    }

    if (m == 1) {
        int slot = int(v_modeVars.z);
        colour = texture(textures[slot], v_modeVars.xy);

        // debug bg colour
        if (debugMode == 1 && colour.a < EPSILON) {
            doColourfull();
            colour.a = .2;
        }
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