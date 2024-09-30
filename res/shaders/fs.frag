#version 330

uniform vec2 resolution;
uniform highp float time;

uniform sampler2D sampleTexture;
uniform sampler2D fontTexture;

in vec2 v_texCoord;
out vec4 colour;

vec2 invResolution = 1 / resolution;

void main() {
//    vec2 uv = gl_FragCoord.xy * invResolution;
//    colour = vec4(uv.xy, abs(sin(time - length(uv))), 1);

    colour = texture(fontTexture, v_texCoord);
}