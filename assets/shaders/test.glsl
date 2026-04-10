#version 430 core

uniform sampler2D test;

layout(location=0) out vec4 vColor;

in vec2 vTexCoord;

void main() {
    // vColor = texture(test, vTexCoord);
    vColor = vec4(0, 1, 1, 1);
}