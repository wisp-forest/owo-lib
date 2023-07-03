#version 150

uniform vec4 ColorModulator;

in vec4 vertexColor;
out vec4 fragColor;

vec3 hsv2rgb(vec3 hsv) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(hsv.xxx + K.xyz) * 6.0 - K.www);
    return hsv.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), hsv.y);
}

void main() {
    fragColor = vec4(
        hsv2rgb(vertexColor.xyz).xyz,
        vertexColor.w
    ) * ColorModulator;
}
