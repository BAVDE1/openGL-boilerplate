//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 col;

uniform mat4 projectionMatrix;
uniform vec3 camPos;
uniform vec3 camRot;

uniform float time;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 v_col;

void main() {
    // lower e.x, higher fov
    // also higher w, higher fov as well?
    // https://en.wikipedia.org/wiki/3D_projection
    vec3 s = sin(camRot);
    vec3 c = cos(camRot);
    float x = pos.x - camPos.x;
    float y = pos.y - camPos.y;
    float z = pos.z - camPos.z;

    // transformed point (no matrices)
//    vec3 d = vec3(
//        c.y * (s.z * y + c.z * x) - s.y * z,
//        s.x * (c.y * z + s.y * (s.z * y + c.z * x)) + c.x * (c.z * y - s.z * x),
//        c.x * (c.y * z + s.y * (s.z * y + c.z * x)) - s.x * (c.z * y - s.z * x)
//    );

    // transformed point (with matrices)
    mat3 mata = mat3(
        1,  0,   0,
        0,  c.x, s.x,
        0, -s.x, c.x
    );
    mat3 matb = mat3(
        c.y, 0, -s.y,
        0,   1,  0,
        s.y, 0,  c.y
    );
    mat3 matc = mat3(
        c.z,  s.z, 0,
        -s.z, c.z, 0,
        0,    0,   1
    );
    vec3 d = vec3(mata * matb * matc * (pos - camPos));

    // the display surface's position relative to camera
    vec3 e = vec3(
        0, 0, -1
    );

    // projected onto the 2D plane (no matrix)
//    vec2 b = vec2(
//        (e.z / d.z) * d.x + e.x,
//        (e.z / d.z) * d.y + e.y
//    );

    // projected onto the 2D plane (with matrix)
    mat3 matDispSurf = mat3(
        1, 0, e.x/e.z,
        0, 1, e.y/e.z,
        0, 0, 1/e.z
    );
    vec3 f = vec3(matDispSurf * d);
    vec2 b = vec2(f.x/f.z, f.y/f.z);

    gl_Position = vec4(b, 0, 1);
    v_col = col;
}

//--- FRAG
#version 450 core

in vec3 v_col;

out vec4 colour;

void main() {
    colour = vec4(v_col, 1);
}