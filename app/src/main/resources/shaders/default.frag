#version 130

in vec4 vert_color;
flat in float out_time;
flat in int vert_drawable_id;

out vec4 color;

void main()
{
    color = vert_color;
}
