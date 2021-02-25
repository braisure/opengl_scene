package com.opengl.scene.objects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.opengl.scene.utils.MatrixState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by bozhao on 2017/12/11.
 * 绘制纹理天空球
 */

public class SkyBall {
    private int mProgram;

    private int aPositionLocation;
    private int aTexCoordLocation;
    private int uMVPMatrixLocation;
    private int uTextureSamplerLocation;

    private int mVertexCount;   // 顶点数量

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;

    public SkyBall(float radius, int program, float startX, float startY, float startZ) {
        mProgram = program;
        initVertexData(radius, startX, startY, startZ);
        initShader(program);
    }

    /**
     * 初始化顶点数据
     */
    public void initVertexData(float radius, float startX,float startY,float startZ) {
        float ANGLE_SPAN = 15;  // 切分间隔
        float angleV = 90;      // 纵向上的起始度数

        // 获取切分整图的纹理数组
        float[] texCoordArray =
            generateTexCoord(
                (int)(360 / ANGLE_SPAN),    // 纹理图切分的列数
                (int)(angleV / ANGLE_SPAN)  // 纹理图切分的行数
            );
        int tc = 0;                      // 纹理数组计数器
        int ts = texCoordArray.length;  // 纹理数组长度

        ArrayList<Float> alVertex   = new ArrayList<Float>();     // 用于存放顶点坐标
        ArrayList<Float> alTexCoord = new ArrayList<Float>();     // 用于存放纹理坐标

        for (float vAngle = angleV; vAngle > 0; vAngle = vAngle - ANGLE_SPAN) {
            for (float hAngle = 360; hAngle > 0; hAngle = hAngle - ANGLE_SPAN) {

                // 纵向横向各到一个角度后计算对应的此点在球面上的四边形顶点坐标
                // 并构建两个组成四边形的三角形

                double xozLength = radius * Math.cos(Math.toRadians(vAngle));
                float x1 = (float)(xozLength * Math.cos(Math.toRadians(hAngle))) + startX;
                float z1 = (float)(xozLength * Math.sin(Math.toRadians(hAngle))) + startZ;
                float y1 = (float)(radius*Math.sin(Math.toRadians(vAngle))) + startY;

                xozLength = radius*Math.cos(Math.toRadians(vAngle - ANGLE_SPAN));
                float x2 = (float)(xozLength*Math.cos(Math.toRadians(hAngle))) + startX;
                float z2 = (float)(xozLength*Math.sin(Math.toRadians(hAngle))) + startZ;
                float y2 = (float)(radius*Math.sin(Math.toRadians(vAngle - ANGLE_SPAN))) + startY;

                xozLength = radius*Math.cos(Math.toRadians(vAngle - ANGLE_SPAN));
                float x3 = (float)(xozLength*Math.cos(Math.toRadians(hAngle - ANGLE_SPAN))) + startX;
                float z3 = (float)(xozLength*Math.sin(Math.toRadians(hAngle - ANGLE_SPAN))) + startZ;
                float y3 = (float)(radius*Math.sin(Math.toRadians(vAngle - ANGLE_SPAN))) + startY;

                xozLength = radius*Math.cos(Math.toRadians(vAngle));
                float x4 = (float)(xozLength*Math.cos(Math.toRadians(hAngle - ANGLE_SPAN))) + startX;
                float z4 = (float)(xozLength*Math.sin(Math.toRadians(hAngle - ANGLE_SPAN))) + startZ;
                float y4 = (float)(radius*Math.sin(Math.toRadians(vAngle))) + startY;

                // 构建第一三角形
                alVertex.add(x1);alVertex.add(y1);alVertex.add(z1);
                alVertex.add(x4);alVertex.add(y4);alVertex.add(z4);
                alVertex.add(x2);alVertex.add(y2);alVertex.add(z2);

                // 构建第二三角形
                alVertex.add(x2);alVertex.add(y2);alVertex.add(z2);
                alVertex.add(x4);alVertex.add(y4);alVertex.add(z4);
                alVertex.add(x3);alVertex.add(y3);alVertex.add(z3);

                // 构建第一三角形
                alVertex.add(x1);alVertex.add(-y1);alVertex.add(z1);
                alVertex.add(x2);alVertex.add(-y2);alVertex.add(z2);
                alVertex.add(x4);alVertex.add(-y4);alVertex.add(z4);

                // 构建第二三角形
                alVertex.add(x2);alVertex.add(-y2);alVertex.add(z2);
                alVertex.add(x3);alVertex.add(-y3);alVertex.add(z3);
                alVertex.add(x4);alVertex.add(-y4);alVertex.add(z4);

                int tcc = tc;

                // 第一三角形3个顶点的6个纹理坐标
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);

                // 第二三角形3个顶点的6个纹理坐标
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);
                alTexCoord.add(texCoordArray[tc++ % ts]);

                // 第一三角形3个顶点的6个纹理坐标
                alTexCoord.add(texCoordArray[tcc++ % ts]);
                alTexCoord.add(texCoordArray[tcc++ % ts]);
                alTexCoord.add(texCoordArray[tcc++ % ts + 2]);
                alTexCoord.add(texCoordArray[tcc++ % ts + 2]);
                alTexCoord.add(texCoordArray[tcc++ % ts - 2]);
                alTexCoord.add(texCoordArray[tcc++ % ts - 2]);

                // 第二三角形3个顶点的6个纹理坐标
                alTexCoord.add(texCoordArray[tcc++ % ts]);
                alTexCoord.add(texCoordArray[tcc++ % ts]);
                alTexCoord.add(texCoordArray[tcc++ % ts + 2]);
                alTexCoord.add(texCoordArray[tcc++ % ts + 2]);
                alTexCoord.add(texCoordArray[tcc++ % ts - 2]);
                alTexCoord.add(texCoordArray[tcc++ % ts - 2]);
            }
        }
        mVertexCount = alVertex.size() / 3;     // 顶点的数量为坐标值数量的1/3，因为一个顶点有3个坐标

        // 创建绘制顶点数据缓冲
        float vertices[] = new float[alVertex.size()];
        for (int i = 0; i < alVertex.size(); i++)  {
            vertices[i] = alVertex.get(i);
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());       // 设置字节顺序为本地操作系统顺序
        mVertexBuffer = vbb.asFloatBuffer();    // 转换为float型缓冲
        mVertexBuffer.put(vertices);            // 向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);              // 设置缓冲区起始位置

        // 创建纹理坐标缓冲
        float textureCoors[] = new float[alTexCoord.size()];
        for(int i = 0; i < alTexCoord.size(); i++) {
            textureCoors[i] = alTexCoord.get(i);
        }
        ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoors.length * 4);
        tbb.order(ByteOrder.nativeOrder());       // 设置字节顺序为本地操作系统顺序
        mTexCoorBuffer = tbb.asFloatBuffer();   // 转换为int型缓冲
        mTexCoorBuffer.put(textureCoors);       // 向缓冲区中放入顶点着色数据
        mTexCoorBuffer.position(0);             // 设置缓冲区起始位置
    }

    /**
     * 初始化着色器程序方法
     */
    public void initShader(int program) {
        aPositionLocation  = GLES20.glGetAttribLocation(program, "a_Position");
        aTexCoordLocation  = GLES20.glGetAttribLocation(program, "a_TexCoord");
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        uTextureSamplerLocation = GLES20.glGetUniformLocation(program, "u_TextureSampler");
    }

    /**
     * 执行绘制
     */
    public void drawSelf(int textureId, float transX, float transY, float transZ, float rotateAngleY) {
        MatrixState.pushMatrix();
        MatrixState.translate(transX, transY, transZ);
        MatrixState.rotate(rotateAngleY, 0, 1, 0);

        // 使用着色器程序
        GLES20.glUseProgram(mProgram);

        // 设置变换矩阵
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MatrixState.getFinalMatrix(), 0);

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

        MatrixState.popMatrix();
    }

    /**
     * 自动切分纹理产生纹理数组的方法
     */
    private float[] generateTexCoord(int bw, int bh) {
        float[] result = new float[bw * bh * 6 * 2];
        float sizeW = 1.0f / bw;    // 列数
        float sizeH = 1.0f / bh;    // 行数

        int index = 0;
        for (int i = 0; i < bh; ++i) {
            for (int j = 0; j < bw; ++j) {

                // 每行列一个矩阵，由2个三角形构成，共6个点，12个纹理坐标
                float s = j * sizeW;
                float t = i * sizeH;

                result[index++] = s;
                result[index++] = t;

                result[index++] = s + sizeW;
                result[index++] = t;

                result[index++] = s;
                result[index++] = t + sizeH;

                result[index++] = s;
                result[index++] = t + sizeH;

                result[index++] = s + sizeW;
                result[index++] = t;

                result[index++] = s + sizeW;
                result[index++] = t + sizeH;
            }
        }
        return result;
    }
}
