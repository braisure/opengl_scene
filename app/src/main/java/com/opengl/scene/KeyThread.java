package com.opengl.scene;

import android.opengl.Matrix;

import com.opengl.scene.objects.Tree;

import java.util.Collections;

import static com.opengl.scene.Constant.*;
import static com.opengl.scene.SceneView.mTreeList;

/**
 * Created by bozhao on 2017/12/15.
 */

public class KeyThread extends Thread {
    SceneView mSceneView;         // 主场景类的引用
    public boolean mWorkFlag;   // 线程标志位

    public KeyThread(SceneView sv) {
        mSceneView = sv;
        mWorkFlag = true;
    }

    @Override
    public void run() {
        while (mWorkFlag) {

            synchronized (lock) {
                // 摄像机目标位置更新
                kCameraTargetX -= Math.sin(Math.toRadians(kCameraRotateAngleY)) * Math.cos(Math.toRadians(kCameraRotateAngleX)) * kCameraMoveSpan;
                kCameraTargetY += Math.sin(Math.toRadians(kCameraRotateAngleX)) * kCameraMoveSpan;
                kCameraTargetZ -= Math.cos(Math.toRadians(kCameraRotateAngleY)) * Math.cos(Math.toRadians(kCameraRotateAngleX)) * kCameraMoveSpan;

                // 摄像机移动边界检查，这里会更改目标点位置
                /*int gellSize = 2;
                if (kCameraTargetX < (-gellSize + 0.5f) * kLandChunkWidth) {
                    kCameraTargetX = (-gellSize + 0.5f) * kLandChunkWidth;
                } else if (kCameraTargetX > (kLandformArray[1].length + gellSize - 0.5f) * kLandChunkWidth) {
                    kCameraTargetX = (kLandformArray[1].length + gellSize -0.5f) * kLandChunkWidth;
                }
                if (kCameraTargetZ < (-gellSize + 0.5f) * kLandChunkHeight) {
                    kCameraTargetZ = (-gellSize + 0.5f) * kLandChunkHeight;
                } else if (kCameraTargetZ > (kLandformArray[1].length + gellSize - 0.5f) * kWaterSide) {
                    kCameraTargetZ = (kLandformArray[1].length + gellSize - 0.5f) * kWaterSide;
                }*/

                // 重新计算摄像机位置
                kCameraPosX = (float) (kCameraTargetX + Math.sin(Math.toRadians(kCameraRotateAngleY)) * kCameraDistance);
                kCameraPosY = (float) (kCameraTargetY - Math.sin(Math.toRadians(kCameraRotateAngleX)) * kCameraDistance);
                kCameraPosZ = (float) (kCameraTargetZ + Math.cos(Math.toRadians(kCameraRotateAngleY)) * kCameraDistance);

                kSkyBallRotateAngle += 0.03f;      // 天空穹旋转角度
            }

            // 树信息更新
            for (Tree tree : mTreeList) {
                tree.updateCameraPos(kCameraPosX, kCameraPosY, kCameraPosZ);
                tree.calculateBillboardDirection();     // 计算树的朝向
            }
            Collections.sort(mTreeList);    // 对树排序
            
            try {
                Thread.sleep(50);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}