package com.opengl.scene.utils;

import android.content.res.Resources;
import android.opengl.GLSurfaceView;

/**
 * Created by bozhao on 2017/12/11.
 * 该shader管理类主要是用于加载shader和编译shader
 */

public class ShaderManager {

    final static String[][] shaderName =
    {
        {"vertex_tex_only.glsl", "frag_tex_only.glsl"},
        {"vertex_landform.glsl", "frag_landform.glsl"},  // 地形的shader
        {"vertex_button.glsl", "frag_button.glsl"},       // 按钮的shader
        {"vertex_fog_tex.glsl", "frag_fog_tex.glsl"},     // 雾化纹理
        {"vertex_water.glsl", "frag_water.glsl"},         // 水面纹理
        {"vertex_model.glsl", "frag_model.glsl"},         // 模型对象shader
    };

    static String[] mVertexShader   = new String[shaderName.length];    // 顶点着色器字符串数组
    static String[] mFragmentShader = new String[shaderName.length];    // 片元着色器字符串数组

    static int[] mProgram = new int[shaderName.length];  // 程序数组

    /**
     * 加载第一个视图的shader字符串（大多为loading视图）
     */
    public static void loadFistViewCodeFromFile(Resources r)
    {
        mVertexShader[0] = ShaderUtil.loadFromAssetsFile(shaderName[0][0], r);
        mFragmentShader[0] = ShaderUtil.loadFromAssetsFile(shaderName[0][1], r);
    }

    /**
     * 加载shader字符串
     */
    public static void loadCodeFromFile(Resources r)
    {
        for (int i = 1; i < shaderName.length; ++i) {

            // 加载顶点着色器脚本内容
            mVertexShader[i] = ShaderUtil.loadFromAssetsFile(shaderName[i][0], r);

            // 加载片元着色器脚本内容
            mFragmentShader[i] = ShaderUtil.loadFromAssetsFile(shaderName[i][1], r);
        }
    }

    /**
     * 编译第一个视图的shader(大多为loading视图)
     */
    public static void compileFistViewShader()
    {
        mProgram[0] = ShaderUtil.createProgram(mVertexShader[0], mFragmentShader[0]);
    }

    /**
     * 编译shader
     */
    public static void compileShader()
    {
        for (int i = 1; i < shaderName.length; ++i) {
            mProgram[i] = ShaderUtil.createProgram(mVertexShader[i], mFragmentShader[i]);
        }
    }

    /**
     * 返回第一个视图的shader程序
     */
    public static int getFirstViewShaderProgram() {
        return mProgram[0];
    }

    /**
     * 返回只有纹理的shader程序
     */
    public static int getOnlyTextureShaderProgram() {
        return mProgram[0];
    }

    /**
     * 返回地形的shader程序
     */
    public static int getLandformShaderProgram() {
        return mProgram[1];
    }

    /**
     * 返回按钮的shader程序
     */
    public static int getButtonShaderProgram() {
        return mProgram[2];
    }

    /**
     * 返回雾化纹理的shader程序
     */
    public static int getFogTextureShaderProgram() {
        return mProgram[3];
    }

    /**
     * 返回水面的shader程序
     */
    public static int getWaterShaderProgram() {
        return mProgram[4];
    }

    /**
     * 返回模型的shader程序
     */
    public static int getModelShaderProgram() {
        return mProgram[5];
    }
}
