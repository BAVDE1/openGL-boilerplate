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

const float kernelOffset = 1.0 / 800.0;

void main() {
    colour = texture(screenTexture, v_texPos);

//     invert
//    colour = vec4(1 - colour.xyz, 1);

//    greyscale (weighted)
//    float avg = .21 * colour.r + .71 * colour.g + .07 * colour.b;
//    colour = vec4(avg, avg, avg, 1);

//    kernel (blur or edge detection)
//    vec2 offsets[9] = vec2[](
//        vec2(-kernelOffset,  kernelOffset), // top-left
//        vec2( 0.0f,          kernelOffset), // top-center
//        vec2( kernelOffset,  kernelOffset), // top-right
//        vec2(-kernelOffset,  0.0f),   // center-left
//        vec2( 0.0f,          0.0f),   // center-center
//        vec2( kernelOffset,  0.0f),   // center-right
//        vec2(-kernelOffset, -kernelOffset), // bottom-left
//        vec2( 0.0f,         -kernelOffset), // bottom-center
//        vec2( kernelOffset, -kernelOffset)  // bottom-right
//    );
//
//    float blurKernel[9] = float[](
//        1.0 / 16, 2.0 / 16, 1.0 / 16,
//        2.0 / 16, 4.0 / 16, 2.0 / 16,
//        1.0 / 16, 2.0 / 16, 1.0 / 16
//    );
//
//    float edgeKernel[9] = float[](
//        1, 1, 1,
//        1, -8, 1,
//        1, 1, 1
//    );
//
//    vec3 col = vec3(0);
//    for(int i = 0; i < 9; i++) {
//        vec3 sampleTex = vec3(texture(screenTexture, v_texPos.st + kernelOffset[i]));
//        col += sampleTex * edgeKernel[i];
//    }
//    colour = vec4(col, 1.0);
}