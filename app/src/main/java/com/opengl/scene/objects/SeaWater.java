package com.opengl.scene.objects;

import android.opengl.GLES20;

import com.opengl.scene.utils.MatrixState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.opengl.scene.Constant.kWaterCols;
import static com.opengl.scene.Constant.kWaterRows;
import static com.opengl.scene.Constant.kWaterSpan;

/**
 * Created by bozhao on 2017/12/28.
 */

public class SeaWater {
    private int mProgram;

    private int aPositionLocation;
    private int aTexCoordLocation;
    private int uMVPMatrixLocation;
    private int uTextureSamplerLocation;

    private int uStartAngleLocation;
    private int uWidthLocation;
    private int uHeightLocation;

    private int mVertexCount;   // 顶点数量

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;

    float mCurrStartAngle = 0.0f;   // 当前帧的起始角度，范围[0, 2PI]

    float mWidth;
    float mHeight;

    public SeaWater(float width, float height, int program) {
        mProgram = program;
        mWidth = width;
        mHeight = height;
        initVertexData();
        initShader(program);

        // 启动一个线程定时换帧
        new Thread() {
            public void run() {
                while (true) {
                    mCurrStartAngle += (Math.PI / 16.0f);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 初始化顶点数据
     */
    public void initVertexData() {
        mVertexCount = kWaterRows * kWaterCols * 2 * 3;   // 每个格子两个三角形，每个三角形3个顶点

        // 创建绘制顶点数据缓冲
        float vertices[] = new float[mVertexCount * 3];    // 每个顶点xyz三个坐标
        int count = 0;          // 顶点计数器
        for (int i = 0; i < kWaterRows; ++i) {
            for (int j = 0; j < kWaterCols; ++j) {
                // 计算当前格子左上侧点坐标
                float zsx = j * kWaterSpan;
                float zsy = 0;
                float zsz = i * kWaterSpan;

                // 左上点
                vertices[count++] = zsx;
                vertices[count++] = zsy;
                vertices[count++] = zsz;

                // 左下点
                vertices[count++] = zsx;
                vertices[count++] = zsy;
                vertices[count++] = zsz + kWaterSpan;

                // 右上点
                vertices[count++] = zsx + kWaterSpan;
                vertices[count++] = zsy;
                vertices[count++] = zsz;

                // 右上点
                vertices[count++] = zsx + kWaterSpan;
                vertices[count++] = zsy;
                vertices[count++] = zsz;

                // 左下点
                vertices[count++] = zsx;
                vertices[count++] = zsy;
                vertices[count++] = zsz + kWaterSpan;

                // 右下点
                vertices[count++] = zsx + kWaterSpan;
                vertices[count++] = zsy;
                vertices[count++] = zsz + kWaterSpan;
            }
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());             // 设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();          // 转换为Float型缓冲
        mVertexBuffer.put(vertices);                  // 向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);                    // 设置缓冲区起始位置

        // 创建纹理坐标缓冲
        float texCoord[] = generateTexCoord(kWaterCols, kWaterRows);
        ByteBuffer cbb = ByteBuffer.allocateDirect(texCoord.length * 4);
        cbb.order(ByteOrder.nativeOrder());             // 设置字节顺序
        mTexCoorBuffer = cbb.asFloatBuffer();         // 转换为Float型缓冲
        mTexCoorBuffer.put(texCoord);                 // 向缓冲区中放入顶点着色数据
        mTexCoorBuffer.position(0);                   // 设置缓冲区起始位置
    }

    /**
     * 初始化着色器程序方法
     */
    public void initShader(int program) {
        aPositionLocation  = GLES20.glGetAttribLocation(program, "a_Position");
        aTexCoordLocation  = GLES20.glGetAttribLocation(program, "a_TexCoord");
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        uTextureSamplerLocation = GLES20.glGetUniformLocation(program, "u_TextureSampler");

        uStartAngleLocation = GLES20.glGetUniformLocation(program, "u_StartAngle");
        uWidthLocation = GLES20.glGetUniformLocation(program, "u_Width");
        uHeightLocation = GLES20.glGetUniformLocation(program, "u_Height");
    }

    public void drawSelf(int textureId) {

        // 使用着色器程序
        GLES20.glUseProgram(mProgram);

        // 设置变换矩阵
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MatrixState.getFinalMatrix(), 0);

        // 设置本帧起始角度
        GLES20.glUniform1f(uStartAngleLocation, mCurrStartAngle);

        // 设置宽、高
        GLES20.glUniform1f(uWidthLocation, mWidth);
        GLES20.glUniform1f(uHeightLocation, mHeight);

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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);
    }

    private float[] generateTexCoord(int bw,int bh) {
        float[] result = new float[bw * bh * 6 * 2];
        float sizeW = 16.0f / bw;   // 列数
        float sizeH = 16.0f / bh;   // 行数
        int c = 0;
        for (int i = 0; i < bh; ++i) {
            for (int j = 0; j < bw; ++j) {
                // 每行列一个矩形，由两个三角形构成，共六个点，12个纹理坐标
                float s = j * sizeW;
                float t = i * sizeH;

                result[c++] = s;
                result[c++] = t;

                result[c++] = s;
                result[c++] = t + sizeH;

                result[c++] = s + sizeW;
                result[c++] = t;

                result[c++] =s + sizeW;
                result[c++] = t;

                result[c++] = s;
                result[c++] = t + sizeH;

                result[c++] = s + sizeW;
                result[c++] = t + sizeH;
            }
        }
        return result;
    }

}
