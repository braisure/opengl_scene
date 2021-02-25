uniform mat4 u_MVPMatrix;
uniform mat4 u_MMatrix;         // 模型变换矩阵，用于将模型坐标转换到世界坐标
uniform vec3 u_CameraPos;       // 摄像机位置，主要用于实现雾化

attribute vec3 a_Position;
attribute vec2 a_TexCoord;

varying vec2 v_TexCoord;
varying float v_FogFactor;      // 雾化因子

float computerFogFactor()
{
    // 计算摄像机位置距离绘制顶点的距离，距离越大，雾越浓
    float distance = length(u_CameraPos - (u_MMatrix * vec4(a_Position, 1.0f)).xyz);
    const float start = 1000.0f; // 雾开始位置
    const float end = 3000.0f;   // 雾结束位置

    float factor = 1.0f - smoothstep(start, end, distance); // 计算雾化因子
    return factor;
}

void main()
{
    gl_Position = u_MVPMatrix * vec4(a_Position, 1.0f);
    v_TexCoord = a_TexCoord;
    v_FogFactor = computerFogFactor();
}
