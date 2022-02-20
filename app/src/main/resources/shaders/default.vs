#version 130

in vec3 inColor;
in vec2 inPosition;
uniform mat4 inTrans;
uniform mat4 inOrthographic;

out vec3 color;

void main()
{
    gl_Position = inOrthographic * inTrans * vec4(inPosition, 0.0, 1.0);
    color = inColor;
}
