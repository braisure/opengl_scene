package com.opengl.scene.objects;

import android.support.annotation.NonNull;

import static com.opengl.scene.Constant.*;
import com.opengl.scene.utils.MatrixState;


/**
 * Created by bozhao on 2017/12/27.
 * 本质是个广告牌
 */

public class Tree implements Comparable<Tree> {

    private TextureRect mTextureRect;
    private int mTextureId;

    private float mCameraPosX, mCameraPosY, mCameraPosZ;
    private float mPosX, mPosY, mPosZ;
    private float mAngleY;      // 沿Y轴的旋转角度，主要决定树的朝向
    private int mRow, mCol;     // 在地形中，树所在的行号和列号，当前地形是由20*20个地形chunk拼接成的

    public Tree(TextureRect textureRect, int textureId, float posX, float posY, float posZ, int row, int col) {
        mTextureRect = textureRect;
        mTextureId = textureId;
        mPosX = posX;
        mPosY = posY;
        mPosZ = posZ;
        mRow = row;
        mCol = col;
    }

    public void drawSelf(int rowi, int colj, int rowT, int colT) {
        if (mRow < rowi || mRow > rowT || mCol < colj || mCol > colT) {
            return;
        }
        MatrixState.pushMatrix();
        MatrixState.translate(mPosX, mPosY, mPosZ);
        MatrixState.rotate(mAngleY, 0,1,0);
        mTextureRect.drawSelf(mTextureId);
        MatrixState.popMatrix();
    }

    public void updateCameraPos(float x, float y, float z) {
        mCameraPosX = x;
        mCameraPosY = y;
        mCameraPosZ = z;
        mTextureRect.updateCameraPos(x, y, z);
    }

    /**
     * 计算广告牌的朝向
     */
    public void calculateBillboardDirection() {

        // 根据摄像机为值计算树的朝向
        float currSpanX = mPosX - mCameraPosX;
        float currSpanZ = mPosZ - mCameraPosZ;
        if (currSpanZ < 0) {
            mAngleY = (float)Math.toDegrees(Math.atan(currSpanX / currSpanZ));
        } else if (currSpanZ == 0) {
            mAngleY = currSpanX > 0 ? 90.0f : - 90.0f;
        } else {
            mAngleY = 180.0f + (float)Math.toDegrees(Math.atan(currSpanX / currSpanZ));
        }
    }

    @Override
    public int compareTo(@NonNull Tree tree) {

        // 比较两棵树离摄像机距离的方法，从大到小进行排序
        float currSpanX = mPosX - mCameraPosX;
        float currSpanY = mPosY - mCameraPosY;
        float currSpanZ = mPosZ - mCameraPosZ;

        float otherSpaxX = tree.mPosX - mCameraPosX;
        float otherSpaxY = tree.mPosY - mCameraPosY;
        float otherSpaxZ = tree.mPosZ - mCameraPosZ;

        float currDistance  = currSpanX * currSpanX + currSpanY * currSpanY + currSpanZ * currSpanZ;
        float otherDistance = otherSpaxX * otherSpaxX + otherSpaxY * otherSpaxY + otherSpaxZ * otherSpaxZ;
        return ((currDistance - otherDistance) == 0) ? 0 :((currDistance - otherDistance) > 0) ? -1 : 1;
    }
}
