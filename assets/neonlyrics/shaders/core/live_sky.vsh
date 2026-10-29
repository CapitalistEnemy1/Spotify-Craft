#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

uniform sampler2D Sampler0;

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec3 skyDir;
out vec3 skyInfo;

float sky_u16(int i, bool second) {
    vec4 p = texelFetch(Sampler0, ivec2(i, 0), 0);
    vec2 b = second ? p.ba : p.rg;
    return (floor(b.x * 255.0 + 0.5) * 256.0 + floor(b.y * 255.0 + 0.5)) / 65535.0 * 4.0 - 2.0;
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // Position ist schon mit der Pose gedreht - zurueck in Welt-Achsen (Inverse steht in Pixel 8..12)
    mat3 inv = mat3(
        sky_u16(8, false), sky_u16(8, true), sky_u16(9, false),
        sky_u16(9, true), sky_u16(10, false), sky_u16(10, true),
        sky_u16(11, false), sky_u16(11, true), sky_u16(12, false));
    skyDir = inv * Position;
    // x = Himmelszeit, y = Helligkeit, z = Deckkraft
    skyInfo = vec3(UV0.x, Color.r * 2.0, Color.a);
}
