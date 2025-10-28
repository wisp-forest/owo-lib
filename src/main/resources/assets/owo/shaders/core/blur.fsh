#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec4 ColorModulator;

uniform float Directions;
uniform float Quality;
uniform float Size;

out vec4 fragColor;

// shader adapted from https://www.shadertoy.com/view/Xltfzj

void main() {
    #define TAU 6.28318530718

    vec2 Radius = Size / InputResolution.xy;

    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = gl_FragCoord.xy / InputResolution.xy;
    // Pixel colour
    vec4 Color = texture(InputSampler, uv);

    // Blur calculations
    for (float d = 0.0; d < TAU; d += TAU / Directions) {
        for (float i = 1.0 / Quality; i <= 1.0; i += 1.0 / Quality) {
            Color += texture(InputSampler, uv + vec2(cos(d), sin(d)) * Radius * i);
        }
    }

    // Output to screen
    Color /= Quality * Directions;
    fragColor = Color * ColorModulator;
}
