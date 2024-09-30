#version 450 core

uniform vec2 resolution;
uniform highp float time;

uniform sampler2D textures[2];

in vec2 v_texCoord;
in float v_texSlot;

out vec4 colour;

vec2 invResolution = 1 / resolution;

void main() {
    int slot = int(v_texSlot);

    if (slot < 0) {
        vec2 uv = gl_FragCoord.xy * invResolution;
        colour = vec4(uv.xy, abs(sin(time - length(uv))), 1);
    } else {
        colour = texture(textures[slot], v_texCoord);
    }
}