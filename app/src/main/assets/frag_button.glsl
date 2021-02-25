precision mediump float;

uniform sampler2D u_TextureSampler;
uniform int u_IsButtonDown;

varying vec2 v_TexCoord;
varying float v_PosX;

void main()
{
    vec4 finalColor = texture2D(u_TextureSampler, v_TexCoord);
    if (u_IsButtonDown == 1)
    {
        gl_FragColor = finalColor * 0.5f;
    }
    else
    {
        gl_FragColor=finalColor;
    }
}
