package com.opengl.scene.models;

import android.opengl.GLES20;

import com.opengl.scene.utils.AABB3;
import com.opengl.scene.utils.MatrixState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by bozhao on 2018/1/3.
 */

public class CommonModel {

    private int mProgram;

    private int aPositionLocation;
    private int aNormalLocation;

    private int uModelMatrixLocation;
    private int uMVPMatrixLocation;

    private int uLightLocation;
    private int uCameraPosLocation;

    private int uColorLocation;

    private int mVertexCount;   // 顶点数量

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mNormalBuffer;

    private AABB3 mPreBoundBox;         // 仿射变换之前的包围盒
    private float[] mBoundBoxMMatrix = new float[16];  // 包围盒的仿射变换

    private float[] mColor = new float[] {1, 1, 1, 1};

    public static final float[][] mColorList = new float[][] {
        { 1.0f,  1.0f,  1.0f, 1.0f },
        { 1.0f,  0.0f,  0.0f, 1.0f },
        { 1.0f,  0.65f, 0.0f, 1.0f },
        { 1.0f,  1.0f,  0.0f, 1.0f },
        { 0.0f,  1.0f,  0.0f, 1.0f },
        { 0.0f,  0.5f,  1.0f, 1.0f },
        { 0.0f,  0.0f,  1.0f, 1.0f },
        { 0.55f, 0.0f,  1.0f, 1.0f },
    };
    int mColorIndex = 0;

    public CommonModel(float[] vertices, float[] normals, int program)
    {
        mProgram = program;
        initVertexData(vertices, normals);
        initShader(program);

        // 初始化包围盒
        mPreBoundBox = new AABB3(vertices);
    }

    public AABB3 getBoundBox()
    {
        return mPreBoundBox.setToTransformedBox(mBoundBoxMMatrix);
    }

    public void changeColor()
    {
        int size = mColorList.length;
        mColorIndex++;
        mColorIndex %= size;
        mColor = mColorList[mColorIndex];
    }

    /**
     * 初始化顶点数据
     */
    public void initVertexData(float[] vertices, float[] normals)
    {
        mVertexCount = vertices.length / 3;

        // 特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        // 转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(normals.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mNormalBuffer = cbb.asFloatBuffer();
        mNormalBuffer.put(normals);
        mNormalBuffer.position(0);
    }

    /**
     * 初始化着色器程序方法
     */
    public void initShader(int program)
    {
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, "a_Position");
        aNormalLocation = GLES20.glGetAttribLocation(mProgram, "a_Normal");

        uMVPMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        uModelMatrixLocation =  GLES20.glGetUniformLocation(mProgram, "u_MMatrix");

        uLightLocation = GLES20.glGetUniformLocation(mProgram, "u_LightLocation");
        uCameraPosLocation = GLES20.glGetUniformLocation(mProgram, "u_CameraPos");

        uColorLocation = GLES20.glGetUniformLocation(mProgram, "u_Color");
    }

    /**
     * 执行绘制
     */
    public void drawSelf()
    {
        for (int i = 0;i < 16; i++) {
            mBoundBoxMMatrix[i] = MatrixState.getModelMatrix()[i];
        }

        // 使用着色器程序
        GLES20.glUseProgram(mProgram);

        // 设置变换矩阵
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MatrixState.getFinalMatrix(), 0);
        GLES20.glUniformMatrix4fv(uModelMatrixLocation, 1, false, MatrixState.getModelMatrix(), 0);

        // 设置光源位置
        GLES20.glUniform3fv(uLightLocation, 1, MatrixState.mLightPositionFB);

        // 设置摄像机位置
        GLES20.glUniform3fv(uCameraPosLocation, 1, MatrixState.mCameraFB);

        // 设置顶点位置
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(
                aPositionLocation, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer );

        // 设置法线
        GLES20.glEnableVertexAttribArray(aNormalLocation);
        GLES20.glVertexAttribPointer(
                aNormalLocation, 3, GLES20.GL_FLOAT, false, 3 * 4, mNormalBuffer);


        // 设置顶点颜色数据
        GLES20.glUniform4fv(uColorLocation, 1, mColor, 0);

        //绘制加载的物体
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);
    }

}
