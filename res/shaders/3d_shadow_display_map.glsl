//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;

out vec2 v_texPos;

void main() {
    gl_Position = vec4(pos.x, pos.y, 0, 1);
    v_texPos = pos;
}

//--- FRAG
#version 450 core

uniform sampler2D depthMap;

in vec2 v_texPos;

out vec4 colour;

void main() {
//    float depthValue = texture(depthMap, v_texPos).r;
//    colour = vec4(vec3(depthValue), 1.0);
    colour = texture(depthMap, v_texPos);
}
