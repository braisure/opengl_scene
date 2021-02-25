precision mediump float;

uniform vec4 u_Color;

varying vec4 v_Ambient;  		// 从顶点着色器传递过来的环境光最终强度
varying vec4 v_Diffuse;		// 从顶点着色器传递过来的散射光最终强度
varying vec4 v_Specular;		// 从顶点着色器传递过来的镜面光最终强度

void main()
{
	vec4 finalColor = u_Color;	// 物体本身的颜色

	// 综合三个通道光的最终强度及片元的颜色计算出最终片元的颜色并传递给管线
	gl_FragColor = finalColor * v_Ambient + finalColor * v_Specular + finalColor * v_Diffuse;
}
