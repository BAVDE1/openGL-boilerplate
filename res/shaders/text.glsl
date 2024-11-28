//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec2 texturePos;
layout(location = 2) in vec4 texColour;

uniform mat4 projectionMatrix;

out vec2 v_texturePos;
out vec4 v_texColour;

void main() {
    gl_Position = vec4(pos.xy, 1, 1) * projectionMatrix;
    v_texturePos = texturePos;
    v_texColour = texColour;
}

//--- FRAG
#version 450 core

uniform sampler2D fontTexture;

in vec2 v_texturePos;
in vec4 v_texColour;

out vec4 colour;

void main() {
    float alpha = v_texturePos.x > -1 ? texture(fontTexture, v_texturePos.xy).a / 255 : 1;
    colour = v_texColour * alpha;  // background
}