package com.opengl.scene.objects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.opengl.scene.utils.MatrixState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by bozhao on 2017/12/12.
 */

public class TextureRect {
    int mProgram;

    //--------- 纹理矩形类型
    //--------- type = 0, 默认类型，标识单纹理
    //--------- type = 1, 按钮类型
    //--------- type = 2, 雾化纹理类型
    private int mType = 0;

    private int aPositionLocation;
    private int aTexCoordLocation;
    private int uMVPMatrixLocation;
    private int uModelMatrixLocation;
    private int uCameraPosLocation;
    private int uTextureSamplerLocation;
    private int uIsButtonDownLocation;

    private int mVertexCount;   // 顶点数量

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;

    public float[] mTexCoord;        // 纹理坐标
    public int mIsButtonDown = 0;   // 按钮是否按下,0表示没有按下,1表示按下

    private float mCameraPosX, mCameraPosY, mCameraPosZ;

    /**
     * 普通构造器
     */
    public TextureRect(float width, float height, int program) {
        mProgram = program;
        initVertexData(width, height,false,1.0f);
        initShader(program);
    }

    /**
     * 特殊构造器，如按钮等
     * @param[in] type 类型, 1为按钮, 2为生命值
     */
    public TextureRect(float width, float height, int program, int type) {
        mProgram = program;
        mType = type;
        initVertexData(width, height,false,1.0f);
        initShader(mProgram);
    }

    /**
     * 初始化顶点数据
     */
    public void initVertexData(float width, float height, boolean hasTexture, float n) {

        mVertexCount = 4;
        float vertices[] = new float[]
        {
            -width / 2.0f,  height / 2.0f, 0.0f,
            -width / 2.0f, -height / 2.0f, 0.0f,
             width / 2.0f,  height / 2.0f, 0.0f,
             width / 2.0f, -height / 2.0f, 0.0f
        };

        // 创建顶点坐标数据缓冲
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        // 如果没有传入纹理坐标
        if (!hasTexture) {
            mTexCoord = new float[]
            {
                0.0f, 0.0f,
                0.0f, n,
                n,    0.0f,
                n,    n
            };
        }

        // 创建顶点纹理坐标数据缓冲
        ByteBuffer cbb = ByteBuffer.allocateDirect(mTexCoord.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(mTexCoord);
        mTexCoorBuffer.position(0);
    }

    /**
     * 初始化着色器程序
     */
    public void initShader(int program) {
        aPositionLocation  = GLES20.glGetAttribLocation(program, "a_Position");
        aTexCoordLocation  = GLES20.glGetAttribLocation(program, "a_TexCoord");
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        uTextureSamplerLocation = GLES20.glGetUniformLocation(program, "u_TextureSampler");

        if (1 == mType) {
            uIsButtonDownLocation = GLES20.glGetUniformLocation(program, "u_IsButtonDown");
        } else if (2 == mType) {
            uModelMatrixLocation = GLES20.glGetUniformLocation(program, "u_MMatrix");
            uCameraPosLocation = GLES20.glGetUniformLocation(program, "u_CameraPos");
        }
    }

    /**
     * 更新摄像机位置
     */
    public void updateCameraPos(float posX, float posY, float posZ) {
        mCameraPosX = posX;
        mCameraPosY = posY;
        mCameraPosZ = posZ;
    }

    /**
     * 执行绘制
     */
    public void drawSelf(int textureId) {

        // 使用着色器程序
        GLES20.glUseProgram(mProgram);

        // 设置变换矩阵
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MatrixState.getFinalMatrix(), 0);

        if (1 == mType) {   // 按钮类型
            GLES20.glUniform1i(uIsButtonDownLocation, mIsButtonDown);
        } else if (2 == mType) {  //  雾化纹理
            GLES20.glUniformMatrix4fv(uModelMatrixLocation, 1, false, MatrixState.getModelMatrix(), 0);
            GLES20.glUniform3f(uCameraPosLocation, mCameraPosX, mCameraPosY, mCameraPosZ);
        }

        // 设置顶点位置
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(
                aPositionLocation, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer );

        // 设置纹理坐标
        GLES20.glEnableVertexAttribArray(aTexCoordLocation);
        GLES20.glVertexAttribPointer(
                aTexCoordLocation, 2, GLES20.GL_FLOAT, false, 2 * 4, mTexCoorBuffer);

        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(uTextureSamplerLocation, 0);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
    }
}
