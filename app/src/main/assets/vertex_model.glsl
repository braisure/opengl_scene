uniform mat4 u_MVPMatrix;
uniform mat4 u_MMatrix;         // 模型变换矩阵
uniform vec3 u_LightLocation;   // 光源位置
uniform vec3 u_CameraPos;       // 摄像机位置

attribute vec3 a_Position;
attribute vec3 a_Normal;        // 顶点法向量

varying vec4 v_Ambient;
varying vec4 v_Diffuse;
varying vec4 v_Specular;

// 定位光光照计算的方法
void pointLight(
    in vec3 normal,				// 法向量
    inout vec4 ambient,			// 环境光最终强度
    inout vec4 diffuse,			// 散射光最终强度
    inout vec4 specular,			// 镜面光最终强度
    in vec3 lightLocation,			// 光源位置
    in vec4 lightAmbient,			// 环境光强度
    in vec4 lightDiffuse,			// 散射光强度
    in vec4 lightSpecular			// 镜面光强度
)
{
    ambient = lightAmbient;			// 直接得出环境光的最终强度

    // 计算变换后的法向量
    vec3 normalTarget = a_Position + normal;
    vec3 newNormal = (u_MMatrix * vec4(normalTarget, 1)).xyz - (u_MMatrix * vec4(a_Position, 1)).xyz;
    newNormal = normalize(newNormal); 	// 对法向量规格化

    // 计算从表面点到摄像机的向量
    vec3 eye = normalize(u_CameraPos - (u_MMatrix * vec4(a_Position, 1)).xyz);

    // 计算从表面点到光源位置的向量vp
    vec3 vp = normalize(u_LightLocation - (u_MMatrix * vec4(a_Position, 1)).xyz);
    vp = normalize(vp);

    vec3 halfVector = normalize(vp + eye);	// 求视线与光线的半向量
    float shininess = 50.0;				    // 粗糙度，越小越光滑

    // 求法向量与vp的点积与0的最大值
    float nDotViewPosition = max(0.0, dot(newNormal, vp));
    diffuse = lightDiffuse * nDotViewPosition;				// 计算散射光的最终强度

    // 法线与半向量的点积
    float nDotViewHalfVector = dot(newNormal, halfVector);
    float powerFactor = max(0.0, pow(nDotViewHalfVector, shininess)); 	// 镜面反射光强度因子
    specular=lightSpecular*powerFactor;    			// 计算镜面光的最终强度
}

void main()
{
	gl_Position = u_MVPMatrix * vec4(a_Position, 1);
    pointLight(normalize(a_Normal), v_Ambient, v_Diffuse, v_Specular, u_LightLocation,
    		vec4(0.4, 0.4, 0.4, 1.0), vec4(0.7, 0.7, 0.7, 1.0),vec4(0.3, 0.3, 0.3, 1.0));
}
