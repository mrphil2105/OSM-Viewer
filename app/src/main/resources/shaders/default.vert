#version 130

in vec3 position;
in int drawable;
uniform mat4 transform;
uniform mat4 orthographic;
uniform sampler1D color_map;
uniform usampler1D category_map;
uniform uint category_bitset;

out vec4 vert_color;

void main()
{
    gl_Position = orthographic * transform * vec4(position, 1.0);

    // This assumes color_map and category_map are the same size
    float idx = float(drawable) / (textureSize(category_map, 0) - 1.0);
    vec4 color = texture(color_map, idx);
    uint category = texture(category_map, idx).r;

    vert_color = vec4(color.rgb, color.a * int((category_bitset & category) != 0u));
}
