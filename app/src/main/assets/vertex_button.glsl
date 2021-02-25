uniform mat4 u_MVPMatrix;

attribute vec3 a_Position;
attribute vec2 a_TexCoord;

varying vec2 v_TexCoord;
varying float v_PosX;
void main()
{
    gl_Position = u_MVPMatrix * vec4(a_Position, 1.0f);
    v_TexCoord = a_TexCoord;
    v_PosX = a_Position.x;
}