#version 130

in vec4 vert_color;
flat in uint draw;
flat in float out_time;
flat in int vert_drawable_id;

out vec4 color;

void main()
{
    if (draw == 0u) {
        discard;
    }

    color = vert_color;
}
