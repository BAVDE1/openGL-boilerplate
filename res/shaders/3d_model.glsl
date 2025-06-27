//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texPos;
layout(location = 3) in ivec4 boneIds;
layout(location = 4) in vec4 boneWeights;

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
    vec4 totalPosition = vec4(0.0f);
    for (int i = 0; i < MAX_BONE_INFLUENCE; i++) {
        if (boneIds[i] == -1) continue;
        if (boneIds[i] >= MAX_BONES) {
            totalPosition = vec4(pos, 1.0f);
            break;
        }

        vec4 localPosition = finalBonesMatrices[boneIds[i]] * vec4(pos, 1.0f);
        totalPosition += localPosition * boneWeights[i];
//        vec3 localNormal = mat3(finalBonesMatrices[boneIds[i]]) * normal;
    }

    gl_Position = projection * view * totalPosition;
//    gl_Position = projection * view * vec4(pos, 1);
    v_texPos = texPos;
}

//--- FRAG
#version 450 core

uniform sampler2D modelTexture;

in vec2 v_texPos;

out vec4 colour;

void main() {
    //    colour = vec4(1, 0, 1, 1);
    colour = texture(modelTexture, v_texPos);
    //    colour = vec4(v_texPos.x, v_texPos.y, 1, 1);
}