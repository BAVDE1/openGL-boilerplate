#version 450 core

uniform sampler2D fontTexture;

in vec3 v_textureVars;

out vec4 colour;

void main() {
    if (v_textureVars.z > -1) {
        colour = vec4(v_textureVars.xyz, 1);  // background
    } else {
        colour = texture(fontTexture, v_textureVars.xy);  // character
    }
}