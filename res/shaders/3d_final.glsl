//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec2 texPos;

out vec2 v_texPos;

void main() {
    gl_Position = vec4(pos.x, pos.y, 0, 1);
    v_texPos = texPos;
}

//--- FRAG
#version 450 core

uniform sampler2D screenTexture;

in vec2 v_texPos;

out vec4 colour;

void main() {
    colour = vec4(1.0 - texture(screenTexture, v_texPos).xyz, 1);
}