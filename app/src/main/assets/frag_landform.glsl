precision mediump float;

uniform sampler2D u_TextureTuCeng;      // 土层纹理
uniform sampler2D u_TextureCaoDi;       // 草地纹理
uniform sampler2D u_TextureShiTou;      // 石头纹理
uniform sampler2D u_TextureShanDing;    // 山顶纹理

uniform float u_Height;     // 最低点高度
uniform float u_HeightSpan;
uniform int u_LandFlag;     // 标识，0为陆地，1为陆地上的高山

varying vec2 v_TexCoord;
varying float v_VertexHeight;
varying float v_FogFactor;      // 雾化因子

void main()
{
    // 地形渐变高度
    float height1 = 30.0f;
    float height2 = 30.0f;
    float height3 = 10.0f;

    vec4 finalColor0 = vec4(0.3f, 0.3f, 0.3f, 0.5f);    // 黑色条
    vec4 finalColor1 = texture2D(u_TextureTuCeng, v_TexCoord);   // 土层
    vec4 finalColor2 = texture2D(u_TextureCaoDi, v_TexCoord);    // 草地
    vec4 finalColor3 = texture2D(u_TextureShiTou, v_TexCoord);   // 石头
    vec4 finalColor4 = texture2D(u_TextureShanDing, v_TexCoord); // 山顶

    vec4 fogColor = vec4(0.3f, 0.3f, 0.3f, 0.5f);    // 雾的颜色

    vec4 fragColor = vec4(0.0f, 0.0f, 0.0f, 0.0f);
    if (0 == u_LandFlag)
    {
        if (abs(v_VertexHeight) < u_Height)
        {
    	    float ratio = abs(v_VertexHeight) / u_Height;
    	    finalColor3 *= (1.0f - ratio);
    	   	finalColor0 *= ratio;
    	    fragColor = finalColor3 + finalColor0;
        }
    	else if (abs(v_VertexHeight) >= u_Height && abs(v_VertexHeight) <= u_Height + height1)              // 第一个地形渐变高度
        {
            float ratio = (abs(v_VertexHeight) - u_Height) / height1;
            finalColor0 *= (1.0f - ratio);
    	   	finalColor1 *= ratio;
    	   	fragColor = finalColor1 + finalColor0;
        }
    	else if (abs(v_VertexHeight) > u_Height + height1 && abs(v_VertexHeight) <= u_HeightSpan - height2) // 第二个地形渐变高度
        {
            fragColor = finalColor1;
        }
        else if (abs(v_VertexHeight) >= u_HeightSpan - height2 && abs(v_VertexHeight) <= u_HeightSpan)
        {
            float ratio = (abs(v_VertexHeight) - u_HeightSpan + height2) / height2;
            finalColor1 *= (1.0f - ratio);
            finalColor0 *= ratio;
            fragColor = finalColor1 + finalColor0;
        }
    	else if (abs(v_VertexHeight) >= u_HeightSpan && abs(v_VertexHeight) <= u_HeightSpan + height3)
        {
            float ratio = (abs(v_VertexHeight) - u_HeightSpan) / height3;
            finalColor0 *= (1.0f - ratio);
            finalColor2 *= ratio;
            finalColor0.a = 0.2;
            fragColor = finalColor2 + finalColor0;
        }
        else
        {
            fragColor = finalColor2;
        }
    }
    else
    {
        if (abs(v_VertexHeight) < u_Height) {
            fragColor = finalColor2;
        }
        else if (abs(v_VertexHeight) >= u_Height && abs(v_VertexHeight) <= u_HeightSpan)
        {
            float ratio = (abs(v_VertexHeight) - u_Height) / (u_HeightSpan - u_Height);
            finalColor2 *= (1.0f - ratio);
            finalColor4 *= ratio;
            fragColor = finalColor2 + finalColor4;
        }
        else
        {
            fragColor = finalColor4;
        }
    }

    // 考虑雾化因子，输出颜色
    gl_FragColor = fragColor * v_FogFactor + fogColor * (1.0f - v_FogFactor);
    // gl_FragColor =  vec4(0.0f, 0.0f, 0.0f, 0.0f);
}
