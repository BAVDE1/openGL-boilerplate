//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec3 textureVars;

uniform mat4 projectionMatrix;

out vec3 v_textureVars;

void main() {
    gl_Position = vec4(pos.xy, 1, 1) * projectionMatrix;
    v_textureVars = textureVars;
}

//--- FRAG
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