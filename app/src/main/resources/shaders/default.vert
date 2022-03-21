#version 130

in vec3 position;
in int drawable;
uniform mat4 transform;
uniform mat4 orthographic;
uniform sampler1D color_map;
uniform usampler1D category_map;

out vec4 vert_color;

void main()
{
    gl_Position = orthographic * transform * vec4(position, 1.0);
    vert_color = texture(color_map, float(drawable) / (textureSize(color_map, 0) - 1.0));
}
