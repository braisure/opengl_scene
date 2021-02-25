package com.opengl.scene.utils;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by bozhao on 2017/12/12.
 * 存储系统矩阵状态的类
 */

public class MatrixState {

    private static float[] mProjMatrix = new float[16];     // 4*4 投影矩阵
    private static float[] mViewMatrix = new float[16];     // 摄像机位置矩阵
    private static float[] mCurrMatrix;     // 当前变换矩阵

    public static float[] mLightLocation = new float[] {0, 0, 0};  // 定位光光源位置
    public static FloatBuffer mLightPositionFB;
    public static FloatBuffer mCameraFB;

    // 保护变换矩阵的栈
    public static float[][] mStack = new float[10][16];
    public static int mStackTop = -1;

    /**
     * 初始化变换矩阵
     */
    public static void setInitStack()
    {
        mCurrMatrix = new float[16];
        Matrix.setRotateM(mCurrMatrix, 0, 0, 1,0,0);
    }

    /**
     * 保护变换矩阵
     */
    public static void pushMatrix()
    {
        mStackTop++;
        for (int i = 0; i < 16; ++i) {
            mStack[mStackTop][i] = mCurrMatrix[i];
        }
    }

    /**
     * 恢复变换矩阵
     */
    public static void popMatrix()
    {
        for (int i = 0; i < 16; ++i) {
            mCurrMatrix[i] = mStack[mStackTop][i];
        }
        mStackTop--;
    }

    /**
     * 平移、旋转、缩放
     */
    public static void translate(float x, float y, float z)
    {
        Matrix.translateM(mCurrMatrix, 0, x, y, z);
    }

    public static void rotate(float angle, float x, float y, float z)
    {
        Matrix.rotateM(mCurrMatrix, 0, angle, x, y, z);
    }

    public static void scale(float x, float y, float z)
    {
        Matrix.scaleM(mCurrMatrix, 0, x, y, z);
    }

    /**
     * 设置摄像机
     */
    public static void setCamera(
        float cx,	 // 摄像机位置x
        float cy,   // 摄像机位置y
        float cz,   // 摄像机位置z
        float tx,   // 摄像机目标点x
        float ty,   // 摄像机目标点y
        float tz,   // 摄像机目标点z
        float upx,  // 摄像机UP向量X分量
        float upy,  // 摄像机UP向量Y分量
        float upz   // 摄像机UP向量Z分量
    )
    {
        Matrix.setLookAtM(mViewMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);

        float[] cameraLocation = new float[3];  // 摄像机位置
        cameraLocation[0] = cx;
        cameraLocation[1] = cy;
        cameraLocation[2] = cz;

        ByteBuffer llbb = ByteBuffer.allocateDirect(3 * 4);
        llbb.order(ByteOrder.nativeOrder());    // 设置字节顺序
        mCameraFB = llbb.asFloatBuffer();
        mCameraFB.put(cameraLocation);
        mCameraFB.position(0);
    }

    /**
     *  获取摄像机矩阵的逆矩阵的方法
     */
    public static float[] getInvertMvMatrix()
    {
        float[] invM = new float[16];
        Matrix.invertM(invM, 0, mViewMatrix, 0);
        return invM;
    }

    /**
     *  通过摄像机变换后的点求变换前的点的方法：乘以摄像机矩阵的逆矩阵
     */
    public static float[] fromPtoPreP(float[] p)
    {
        // 通过逆变换，得到变换之前的点
        float[] inverM = getInvertMvMatrix();// 获取逆变换矩阵
        float[] preP = new float[4];
        Matrix.multiplyMV(preP, 0, inverM, 0, new float[]{p[0], p[1], p[2],1}, 0); // 求变换前的点
        return new float[] {preP[0], preP[1], preP[2]}; // 变换前的点就是变换之前的法向量
    }

    /**
     * 设置透视投影
     */
    public static void setProjectFrustum(
        float left,		// near面的left
        float right,   // near面的right
        float bottom,  // near面的bottom
        float top,     // near面的top
        float near,		// near面距离
        float far      // far面距离
    )
    {
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public static void setProjectPerspective(
        float fovy,
        float aspect,
        float zNear,
        float zFar
    )
    {
        Matrix.perspectiveM(mProjMatrix, 0, fovy, aspect, zNear, zFar);
    }

    /**
     * 设置正交投影
     */
    public static void setProjectOrtho(
        float left,		// near面的left
        float right,   // near面的right
        float bottom,  // near面的bottom
        float top,     // near面的top
        float near,		// near面距离
        float far      // far面距离
    )
    {
        Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    /**
     * 获取具体物体的总变换矩阵
     */
    public static float[] getFinalMatrix()
    {
        float[] mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mCurrMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    /**
     * 获取具体物体的变换矩阵
     */
    public static float[] getModelMatrix()
    {
        return mCurrMatrix;
    }

    public static float[] getViewProjMatrix()
    {
        float[] mVPMatrix = new float[16];
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
        return mVPMatrix;
    }

    /**
     * 设置灯光位置的方法
     */
    public static void setLightLocation(float x, float y, float z)
    {
        mLightLocation[0] = x;
        mLightLocation[1] = y;
        mLightLocation[2] = z;

        ByteBuffer llbb = ByteBuffer.allocateDirect(3 * 4);
        llbb.order(ByteOrder.nativeOrder());    // 设置字节顺序
        mLightPositionFB = llbb.asFloatBuffer();
        mLightPositionFB.put(mLightLocation);
        mLightPositionFB.position(0);
    }
}
