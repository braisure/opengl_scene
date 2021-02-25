uniform mat4 u_MVPMatrix;

attribute vec3 a_Position;
attribute vec2 a_TexCoord;

varying vec2 v_TexCoord;

void main()
{
    gl_Position = u_MVPMatrix * vec4(a_Position, 1.0f);
    v_TexCoord = a_TexCoord;
}
