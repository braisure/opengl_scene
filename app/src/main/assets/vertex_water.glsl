uniform mat4 u_MVPMatrix;
uniform float u_StartAngle;     // 本帧起始角度
uniform float u_Width;          // 横向长度
uniform float u_Height;         // 纵向长度

attribute vec3 a_Position;
attribute vec2 a_TexCoord;

varying vec2 v_TexCoord;

void main()
{
    // 计算x方向角度
    float angleSpanX = 50.0f * 3.14159265f;     // 横向角度总跨度
    float startX = 0.0f;                        // 起始X坐标

    // 根据横向角度总跨度、横向长度总跨度和当前X坐标计算当前X坐标对应的角度
    float currAngleX = u_StartAngle + ((a_Position.x - startX) / u_Width) * angleSpanX;

    // 计算随Z方向发展起始角度的扰动值
    float angleSpanZ = 50.0f * 3.14159265f;     // 纵向角度总跨度
    float startZ = 0.0f;                        // 起始Z坐标

    // 根据纵向角度总跨度、纵向长度总跨度及当前点Y坐标折算出当前点Y坐标对应的角度
    float currAngleZ = ((a_Position.z - startZ) / u_Height) * angleSpanZ;

    // 计算斜向波浪
    float tzH = sin(currAngleX - currAngleZ) * 5.0f;

    gl_Position = u_MVPMatrix * vec4(a_Position.x, tzH, a_Position.z, 1.0f);
    v_TexCoord = a_TexCoord;
}
