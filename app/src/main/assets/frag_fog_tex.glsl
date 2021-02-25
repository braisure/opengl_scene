precision mediump float;
uniform sampler2D u_TextureSampler;
varying vec2 v_TexCoord;
varying float v_FogFactor;      // 雾化因子

void main()
{
    vec4 fogColor = vec4(0.3f, 0.3f, 0.3f, 0.5f);    // 雾的颜色
    gl_FragColor = texture2D(u_TextureSampler, v_TexCoord) * v_FogFactor + fogColor * (1.0f - v_FogFactor);
}
