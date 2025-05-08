//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 col;

uniform mat4 projectionMatrix;
uniform vec3 camPos;
uniform vec3 camRot;

out vec3 v_col;

void main() {
    // https://en.wikipedia.org/wiki/3D_projection
    vec3 s = sin(camRot);
    vec3 c = cos(camRot);
    float x = pos.x - camPos.x;
    float y = pos.y - camPos.y;
    float z = pos.z - camPos.z;

    // transformed point
    vec3 d = vec3(
        c.y * (s.z * y + c.z * x) - s.y * z,
        s.x * (c.y * z + s.y * (s.z * y + c.z * x)) + c.x * (c.z * y - s.z * x),
        c.x * (c.y * z + s.y * (s.z * y + c.z * x)) - s.x * (c.z * y - s.z * x)
    );
    // the display surface's position relative to camera
    vec3 e = vec3(
        0, 0, -1
    );
    // projected onto the 2D plane
    vec2 b = vec2(
        (e.z / d.z) * d.x + e.x,
        (e.z / d.z) * d.y + e.y
    );
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