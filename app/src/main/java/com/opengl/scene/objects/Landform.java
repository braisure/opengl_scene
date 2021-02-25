package com.opengl.scene.objects;

import android.opengl.GLES20;

import com.opengl.scene.utils.MatrixState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.opengl.scene.Constant.kLandChunkUnitSize;
import static com.opengl.scene.Constant.kLandHeightmapArray;

/**
 * Created by bozhao on 2017/12/14.
 */

public class Landform {
    private int mProgram;

    private int aPositionLocation;
    private int aTexCoordLocation;
    private int uModelMatrixLocation;
    private int uMVPMatrixLocation;

    private int uCameraPosLocation;

    private int uTextureTuCengLocation;     // 土层纹理采样器引用ID
    private int uTextureCaoDiLocation;      // 草地纹理采样器引用ID
    private int uTextureShiTouLocation;     // 石头纹理采样器引用ID
    private int uTextureShanDingLocation;   // 山顶纹理采样器引用ID

    private int uHeightLocation;
    private int uHeightSpanLocation;
    private int uLandFlagLocation;           // 标识引用ID，0为陆地，1为陆地上的高山

    private int mVertexCount;   // 顶点数量

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;

    private boolean mIsHuidutu;     // 是否是灰度图还是程序生成

    public Landform(int terrainId, int program) {
        mProgram = program;

        // 如果是陆地上的山，则不绘制倒影
        if (3 == terrainId || 5 == terrainId || 6 == terrainId) {
            mIsHuidutu = true;
        }
        initVertexData(terrainId);
        initShader(program);
    }

    /**
     * 初始化顶点数据
     */
    public void initVertexData(int terrainId) {
        int rows = kLandHeightmapArray[terrainId].length - 1;
        int cols = kLandHeightmapArray[terrainId][0].length - 1;

        if (!mIsHuidutu) {  // 绘制程序生成的地形
            float textureSize = 1.0f;
            float sizeW = textureSize / cols;
            float sizeH = textureSize / rows;

            ArrayList<Float> alVertex   = new ArrayList<Float>();     // 用于存放顶点坐标
            ArrayList<Float> alTexCoord = new ArrayList<Float>();     // 用于存放纹理坐标
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    float zsx = j * kLandChunkUnitSize;
                    float zsz = i * kLandChunkUnitSize;

                    float s = j * sizeW;
                    float t = i * sizeH;

                    if (kLandHeightmapArray[terrainId][i][j] != 0 ||
                            kLandHeightmapArray[terrainId][i + 1][j] != 0 ||
                            kLandHeightmapArray[terrainId][i][j + 1] != 0) {

                        // 左上点
                        alVertex.add(zsx);
                        alVertex.add(kLandHeightmapArray[terrainId][i][j]);
                        alVertex.add(zsz);

                        // 左下点
                        alVertex.add(zsx);
                        alVertex.add(kLandHeightmapArray[terrainId][i + 1][j]);
                        alVertex.add(zsz + kLandChunkUnitSize);

                        // 右上点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(kLandHeightmapArray[terrainId][i][j + 1]);
                        alVertex.add(zsz);

                        alTexCoord.add(s);
                        alTexCoord.add(t);

                        alTexCoord.add(s);
                        alTexCoord.add(t + sizeH);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t);

                        //--------- 倒影
                        // 左上点
                        alVertex.add(zsx);
                        alVertex.add(-kLandHeightmapArray[terrainId][i][j]);
                        alVertex.add(zsz);

                        // 右上点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(-kLandHeightmapArray[terrainId][i][j + 1]);
                        alVertex.add(zsz);

                        // 左下点
                        alVertex.add(zsx);
                        alVertex.add(-kLandHeightmapArray[terrainId][i + 1][j]);
                        alVertex.add(zsz + kLandChunkUnitSize);

                        alTexCoord.add(s);
                        alTexCoord.add(t);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t);

                        alTexCoord.add(s);
                        alTexCoord.add(t + sizeH);
                    }
                    if (kLandHeightmapArray[terrainId][i][j + 1] != 0 ||
                            kLandHeightmapArray[terrainId][i + 1][j] != 0 ||
                            kLandHeightmapArray[terrainId][i + 1][j + 1] != 0) {

                        // 右上点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(kLandHeightmapArray[terrainId][i][j + 1]);
                        alVertex.add(zsz);

                        // 左下点
                        alVertex.add(zsx);
                        alVertex.add(kLandHeightmapArray[terrainId][i + 1][j]);
                        alVertex.add(zsz + kLandChunkUnitSize);

                        // 右下点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(kLandHeightmapArray[terrainId][i + 1][j + 1]);
                        alVertex.add(zsz + kLandChunkUnitSize);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t);

                        alTexCoord.add(s);
                        alTexCoord.add(t + sizeH);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t + sizeH);

                        //--------- 倒影
                        // 右上点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(-kLandHeightmapArray[terrainId][i][j + 1]);
                        alVertex.add(zsz);

                        // 右下点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(-kLandHeightmapArray[terrainId][i + 1][j + 1]);
                        alVertex.add(zsz + kLandChunkUnitSize);

                        // 左下点
                        alVertex.add(zsx);
                        alVertex.add(-kLandHeightmapArray[terrainId][i + 1][j]);
                        alVertex.add(zsz + kLandChunkUnitSize);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t + sizeH);

                        alTexCoord.add(s);
                        alTexCoord.add(t + sizeH);
                    }
                }
            }
            mVertexCount = alVertex.size() / 3;

            // 创建绘制顶点数据缓冲
            float vertices[] = new float[alVertex.size()];
            for (int i = 0; i< alVertex.size(); ++i) {
                vertices[i] = alVertex.get(i);
            }
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            mVertexBuffer = vbb.asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);

            // 创建纹理坐标缓冲
            float textures[] = new float[alTexCoord.size()];
            for (int i = 0; i < alTexCoord.size(); ++i) {
                textures[i] = alTexCoord.get(i);
            }
            ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length * 4);
            tbb.order(ByteOrder.nativeOrder());
            mTexCoorBuffer = tbb.asFloatBuffer();
            mTexCoorBuffer.put(textures);
            mTexCoorBuffer.position(0);

        } else {             // 加载灰度图，绘制的时候用trangle_strip方式
            float textureSize = 1.0f;
            float sizeW = textureSize / cols;
            float sizeH = textureSize / rows;

            ArrayList<Float> alVertex   = new ArrayList<Float>();     // 用于存放顶点坐标
            ArrayList<Float> alTexCoord = new ArrayList<Float>();     // 用于存放纹理坐标
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    float zsx = j * kLandChunkUnitSize;
                    float zsz = i * kLandChunkUnitSize;

                    float s = j * sizeW;
                    float t = i * sizeH;

                    if (i != 0 && j == 0) {
                        alVertex.add(zsx);
                        alVertex.add(kLandHeightmapArray[terrainId][i][j]);
                        alVertex.add(zsz);

                        alTexCoord.add(s);
                        alTexCoord.add(t);
                    }

                    // 左上点
                    alVertex.add(zsx);
                    alVertex.add(kLandHeightmapArray[terrainId][i][j]);
                    alVertex.add(zsz);

                    alTexCoord.add(s);
                    alTexCoord.add(t);

                    // 左下点
                    alVertex.add(zsx);
                    alVertex.add(kLandHeightmapArray[terrainId][i + 1][j]);
                    alVertex.add(zsz + kLandChunkUnitSize);

                    alTexCoord.add(s);
                    alTexCoord.add(t + sizeH);

                    if (j == cols - 1) {
                        // 右上点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(kLandHeightmapArray[terrainId][i][j + 1]);
                        alVertex.add(zsz);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t);

                        // 右下点
                        alVertex.add(zsx + kLandChunkUnitSize);
                        alVertex.add(kLandHeightmapArray[terrainId][i + 1][j + 1]);
                        alVertex.add(zsz + kLandChunkUnitSize);

                        alTexCoord.add(s + sizeW);
                        alTexCoord.add(t + sizeH);

                        if (i != rows - 1) {
                            // 右下点
                            alVertex.add(zsx + kLandChunkUnitSize);
                            alVertex.add(kLandHeightmapArray[terrainId][i + 1][j + 1]);
                            alVertex.add(zsz + kLandChunkUnitSize);

                            alTexCoord.add(s + sizeW);
                            alTexCoord.add(t + sizeH);
                        }
                    }
                }
            }
            mVertexCount = alVertex.size() / 3;

            // 创建绘制顶点数据缓冲
            float vertices[] = new float[alVertex.size()];
            for (int i = 0; i< alVertex.size(); ++i) {
                vertices[i] = alVertex.get(i);
            }
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            mVertexBuffer = vbb.asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);

            // 创建纹理坐标缓冲
            float textures[] = new float[alTexCoord.size()];
            for (int i = 0; i < alTexCoord.size(); ++i) {
                textures[i] = alTexCoord.get(i);
            }
            ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length * 4);
            tbb.order(ByteOrder.nativeOrder());
            mTexCoorBuffer = tbb.asFloatBuffer();
            mTexCoorBuffer.put(textures);
            mTexCoorBuffer.position(0);
        }
    }

    /**
     * 初始化着色器程序方法
     */
    public void initShader(int program) {
        aPositionLocation  = GLES20.glGetAttribLocation(program, "a_Position");
        aTexCoordLocation  = GLES20.glGetAttribLocation(program, "a_TexCoord");

        uModelMatrixLocation = GLES20.glGetUniformLocation(program, "u_MMatrix");
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");

        uCameraPosLocation = GLES20.glGetUniformLocation(program, "u_CameraPos");

        uTextureTuCengLocation = GLES20.glGetUniformLocation(program, "u_TextureTuCeng");
        uTextureCaoDiLocation = GLES20.glGetUniformLocation(program, "u_TextureCaoDi");
        uTextureShiTouLocation = GLES20.glGetUniformLocation(program, "u_TextureShiTou");
        uTextureShanDingLocation = GLES20.glGetUniformLocation(program, "u_TextureShanDing");

        uHeightLocation = GLES20.glGetUniformLocation(program, "u_Height");
        uHeightSpanLocation = GLES20.glGetUniformLocation(program, "u_HeightSpan");
        uLandFlagLocation = GLES20.glGetUniformLocation(program, "u_LandFlag");
    }

    /**
     * 执行绘制
     */
    public void drawSelf(int landFlag, int shanDingTexId, int tuCengTexId, int caoDiTexId, int shiTouTexId, float height, float heightSpan,
                         float cameraPosX, float cameraPosY,float cameraPosZ) {
        // 使用着色器程序
        GLES20.glUseProgram(mProgram);

        // 设置变换矩阵
        GLES20.glUniformMatrix4fv(uModelMatrixLocation, 1, false, MatrixState.getModelMatrix(), 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MatrixState.getFinalMatrix(), 0);

        // 设置其他参数
        GLES20.glUniform1f(uHeightLocation, height);
        GLES20.glUniform1f(uHeightSpanLocation, heightSpan);
        GLES20.glUniform1i(uLandFlagLocation, landFlag);

        GLES20.glUniform3f(uCameraPosLocation, cameraPosX, cameraPosY, cameraPosZ);

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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tuCengTexId);
        GLES20.glUniform1i(uTextureTuCengLocation, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, caoDiTexId);
        GLES20.glUniform1i(uTextureCaoDiLocation, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shiTouTexId);
        GLES20.glUniform1i(uTextureShiTouLocation, 2);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shanDingTexId);
        GLES20.glUniform1i(uTextureShanDingLocation, 3);

        if (!mIsHuidutu) {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);
        } else {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        }
    }
}
