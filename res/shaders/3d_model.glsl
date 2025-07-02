//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texPos;
layout(location = 3) in ivec4 boneIds;
layout(location = 4) in vec4 boneWeights;
layout(location = 5) in int isStatic;

layout (std140) uniform CameraView {
    mat4 projection;
    mat4 view;
};

const int MAX_BONES = 100;
const int MAX_BONE_INFLUENCE = 4;

uniform mat4 model;
uniform mat4 finalBonesMatrices[MAX_BONES];

out vec2 v_texPos;

void main() {
    mat4 animTransformation = mat4(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
    ) * isStatic;  // only use identity if its not static (ie if its not animated)
    for (int i = 0; i < MAX_BONE_INFLUENCE; i++) {
        if (boneIds[i] == -1) continue;
        if (boneIds[i] >= MAX_BONES) break;
        animTransformation += finalBonesMatrices[boneIds[i]] * boneWeights[i];
    }

    vec4 finalPos = animTransformation * vec4(pos, 1);
    gl_Position = projection * view * (model * finalPos);
    v_texPos = texPos;
}

//--- FRAG
#version 450 core

uniform sampler2D modelTexture;

in vec2 v_texPos;

out vec4 colour;

void main() {
    colour = texture(modelTexture, v_texPos);
}