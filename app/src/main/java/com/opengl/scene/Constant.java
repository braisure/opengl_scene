package com.opengl.scene;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;

/**
 * Created by bozhao on 2017/12/12.
 */

public class Constant {

    //--------- 屏幕尺寸
    public static float kScreenWidth;
    public static float kScreenHeight;

    //--------- 用于将纳秒转换为秒的因子
    public static final double kNS2SFactor = 1.0f / 1000000000.0f;

    //--------- 对象锁
    public static Object lock = new Object();

    //--------- 控制方式 1、 通过屏幕倾斜控制  2、触摸控制  3、手机旋转控制
    public static int kControlType = 3;

    //--------- 陆地chunk块相关，整块陆地地形是由各种陆地chunk块拼接而成
    public static final float kLandChunkUnitSize = 60.0f;   // chunk块单位尺寸，可以用来决定chunk块尺寸
    public static float kLandChunkWidth;                        // chunk块宽度
    public static float kLandChunkHeight;                       // chunk块高度
    public static final int kLandChunkDrawSize = 15;        // chunk块绘制范围一边的个数，即绘制当前位置周边15*15个chunk块组成的地形

    //--------- 陆地的高度图相关，每个高度图绘制一种陆地chunk块，高度图均为
    public static final int kLandHeightmapNumber = 8;   // 高度图数
    public static final int kLandHeightmapWidth  = 8;   // 高度图像素宽度
    public static final int kLandHeightmapHeight = 8;   // 高度图像素高度
    public static float[][][] kLandHeightmapArray =         // 高度图数组
            new float[kLandHeightmapNumber][kLandHeightmapWidth][kLandHeightmapHeight];

    public static float zdYRowFunction(float rowIndex) {
        if (rowIndex >= 0 && rowIndex < 4) {
            return kLandformPlainHeight;
        } else {
            return 0;
        }
    }

    public static float[][] genCustomHeightmap1() {         // 自定义高度图1，用来产生直道地形
        float[][] temp = new float[kLandHeightmapWidth][kLandHeightmapHeight];
        for (int i = 0; i < kLandHeightmapHeight; i++) {
            float h = zdYRowFunction(i);
            for (int j = 0; j < kLandHeightmapWidth; j++) {
                temp[i][j] = h;
            }
        }
        return temp;
    }

    public static float[][] genCustomHeightmap2() {        // 自定义高度图2，用来产生上弯道地形
        float[][] temp = new float[kLandHeightmapWidth][kLandHeightmapHeight];
        for (int i = 0; i < kLandHeightmapHeight; i++) {
            for (int j = 0; j < kLandHeightmapWidth; j++) {
                float p = (float)Math.sqrt(i * i + j * j);
                float h = zdYRowFunction(p);
                temp[i][j] = h;
            }
        }
        return temp;
    }

    public static float[][] genCustomHeightmap3() {        // 自定义高度图3，用来产生下弯道地形
        float[][] temp = new float[kLandHeightmapWidth][kLandHeightmapHeight];
        for(int i = 0; i < kLandHeightmapHeight; i++) {
            for(int j = 0; j < kLandHeightmapWidth; j++) {
                float p = (float)Math.sqrt(i * i + j * j);
                float h = zdYRowFunction(p);
                temp[7 - i][7 - j] = kLandformPlainHeight - h;
            }
        }
        return temp;
    }

    public static float[][] genCustomHeightmap4() {         // 自定义高度图0，用来产生平面地形
        float[][] temp = new float[kLandHeightmapWidth][kLandHeightmapHeight];
        for (int i = 0; i < kLandHeightmapHeight; i++) {
            for (int j = 0; j < kLandHeightmapWidth; j++) {
                temp[i][j] = kLandformPlainHeight;
            }
        }
        return temp;
    }

    public static float[][] loadHeightmap(                 // 从灰度图片中加载生成高度图
            Resources resource, int landformDrawable, float height) {

        // 加载地形灰度图
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bt = BitmapFactory.decodeResource(resource, landformDrawable, options);

        // 获取灰度图的高度与宽度（像素数总比列数和行数大一）
        int colsPlusOne = bt.getWidth();
        int rowsPlusOne = bt.getHeight();

        // 将灰度图中的每个像素的灰度值换算成陆地此点的高度值
        float[][] result = new float[rowsPlusOne][colsPlusOne];
        for (int i = 0; i < rowsPlusOne; i++) {
            for (int j = 0; j < colsPlusOne; j++) {
                int color = bt.getPixel(j, i);  // 获取指定位置颜色值
                int r = Color.red(color);   // 获取红色分量值
                int g = Color.green(color); // 获取绿色分量值
                int b = Color.blue(color);  // 获取蓝色分量值
                int h = (r + g + b) / 3;    // 颜色均值为60
                result[i][j] = h / 255.0f * kLandformHillHeight  + height;
            }
        }
        return result;
    }

    //--------- 陆地地形相关，整块陆地地形是由各种陆地chunk块拼接而成
    public static final int kLandformIndex = 1;                             // 地形索引（取值范围为[0,5]），当前有6中地形，见下
    public static final int kLandformArray[][][] = new int[6][20][20];     // 地形数组，数组中的值为chunk块索引，标识一种陆地chunk

    public static final float kLandformPlainHeight = 150.0f;              // 地形平面高度
    public static final float kLandformPlainSpan1  = 10.0f;               // 地形平面侧切层分隔高度1
    public static final float kLandformPlainSpan2  = 130.0f;              // 地形平面侧切层分隔高度2

    public static final float kLandformHillHeight  = 600.0f;              // 地形平面上的山的高度
    public static final float kLandformHillSpan1   = 250.0f;              // 地形平面上的山侧切层分隔高度1
    public static final float kLandformHillSpan2   = 400.0f;              // 地形平面上上的山侧切层分隔高度2
    public static final float kLandformHillWaterSpan1 = 20.0f;           // 地形水面上的山侧切层分隔高度1
    public static final float kLandformHillWaterSpan2 = 180.0f;          // 地形水面上的山侧切层分隔高度2

    public static void initLandformInfo(Resources r) {              // 初始化地形信息
        // 初始化高度图
        kLandHeightmapArray[0] = genCustomHeightmap1();
        kLandHeightmapArray[1] = genCustomHeightmap2();
        kLandHeightmapArray[2] = genCustomHeightmap3();
        kLandHeightmapArray[3] = loadHeightmap(r,R.drawable.landform, kLandformPlainHeight);     // 陆地上山的左边
        kLandHeightmapArray[4] = loadHeightmap(r,R.drawable.landform1,0);                   // 水里面的山
        kLandHeightmapArray[5] = loadHeightmap(r,R.drawable.landform3, kLandformPlainHeight);    // 陆地上山的右边
        kLandHeightmapArray[6] = loadHeightmap(r,R.drawable.landform2, kLandformPlainHeight);    // 陆地上山的中间部分
        kLandHeightmapArray[7] = genCustomHeightmap4();

        // 初始化陆地chunk块
        kLandChunkWidth = kLandChunkUnitSize * (kLandHeightmapArray[0][0].length - 1);
        kLandChunkHeight = kLandChunkUnitSize * (kLandHeightmapArray[0].length - 1);

        // 初始化陆地地形数组
        kLandformArray[0] = new int[][] {
            { 14,  8,  5,  7, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14,  6, 13, 10,  7, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
            {  8, 11, 13,  2,  1, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
            {  9, 12, 13,  4, 14, 14, 14, 14, 14, 14,  8,  7, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14,  9,  0,  1, 14, 14, 14, 14, 14, 14,  6, 10,  5,  7, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14, 14, 14,  8, 11, 13, 13, 10,  7, 14, 14, 14, 14, 14 },
            { 14, 14, 14,  8,  5,  5,  5,  5,  5, 11, 13, 13, 13, 13, 10,  5,  5,  7, 14, 14 },
            { 14, 14,  8, 11, 13, 13, 13, 13,  3, 17, 17, 16, 13, 13, 13, 13, 13, 10,  7, 14 },
            { 14,  8, 11, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 18, 13, 13, 13, 10,  7 },
            {  8, 11, 13,  2,  0,  0, 12, 13, 13, 18, 13, 21, 13, 13, 19, 13, 13, 13,  2,  1 },
            {  6, 13,  2,  1, 14, 14,  9, 12, 13, 20, 13, 21, 13, 13, 20, 13, 13,  2,  1, 14 },
            {  6,  2,  1, 14, 14, 14, 14,  9, 12, 13, 13, 21, 13, 13, 13, 13,  2,  1, 14, 14 },
            {  9,  1, 14,  8,  5,  7, 14, 14,  6, 13, 13, 21, 13, 13, 13,  2,  1, 14, 14, 14 },
            { 14, 14,  8, 11, 13,  4, 14, 14,  6, 13, 13, 13, 13, 13,  2,  1, 14, 14, 14, 14 },
            { 14,  8, 11, 13,  2,  1, 14,  8, 11, 13, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14 },
            {  8, 11, 13, 13,  4,  8,  5, 11, 13, 13, 13, 13, 13,  4, 14, 14, 14, 14, 14, 14 },
            {  6, 13, 13,  2,  1,  9, 12, 13, 13, 13,  3, 17, 16,  4, 14, 14, 14, 14, 14, 14 },
            {  6, 13,  2,  1, 14, 14,  9, 12, 13, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14 },
            {  9,  0,  1, 14, 14, 14, 14,  9, 12, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14, 14,  9,  0,  0,  1, 14, 14, 14, 14, 14, 14, 14, 14 },
        };
        kLandformArray[1] = new int[][] {
            { 14, 14, 14, 14, 14, 14, 14, 14,  8,  5,  5,  5,  7, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14,  8, 11, 13, 13, 13, 10,  7, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14,  8, 11, 13, 13, 13, 13, 13, 10,  5,  5,  7, 14, 14, 14 },
            { 14, 14, 14, 14, 14,  8, 11,  3, 16,  2,  0,  0, 12, 13, 13, 13, 10,  7, 14, 14 },
            { 14, 14, 14, 14,  8, 11, 13, 13, 13,  4, 14, 14,  9, 12, 13, 13, 18, 10,  5,  7 },
            { 14, 14, 14,  8, 11, 18, 13, 13, 13,  4, 14, 14, 14,  9, 12, 13, 20, 13,  2,  1 },
            { 14, 14,  8, 11, 13, 19, 13, 13,  2,  1, 14, 14, 14, 14,  6, 13, 13,  2,  1, 14 },
            { 14, 14,  9, 12, 13, 20,  2,  0,  1, 14, 14, 14, 14, 14,  6, 13,  2,  1, 14, 14 },
            { 14, 14, 14,  9, 12,  2,  1, 14, 14, 14, 14, 14, 14, 14,  6, 13,  4, 14, 14, 14 },
            { 14, 14, 14, 14,  9,  1, 14, 14,  8,  5,  7, 14, 14, 14,  6, 13,  4, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14,  8, 11,  2,  1, 14, 14,  8, 11, 13, 10,  5,  7, 14 },
            { 14, 14, 14,  8,  7, 14, 14,  6, 13,  4, 14, 14,  8, 11, 13, 13, 13, 13, 10,  7 },
            { 14, 14,  8, 11,  4, 14, 14,  9,  0,  1, 14,  8, 11, 13, 13, 13,  2,  0,  0,  1 },
            { 14,  8, 11, 18, 10,  7, 14, 14, 14, 14,  8, 11, 13, 13, 13,  2,  1, 14, 14, 14 },
            {  8, 11, 13, 19,  2,  1, 14, 14, 14,  8, 11,  3, 17, 16,  2,  1, 14, 14, 14, 14 },
            {  6, 13, 13, 19,  4, 14, 14, 14,  8, 11, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14 },
            {  9, 12, 13, 20,  4, 14, 14,  8, 11, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14 },
            { 14,  9, 12,  2,  1, 14, 14,  9, 12, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14,  9,  1, 14, 14, 14, 14,  9, 12, 13,  4, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14, 14, 14,  9,  0,  1, 14, 14, 14, 14, 14, 14, 14, 14 },
        };
        kLandformArray[2] = new int[][] {
            { 14, 14, 14, 14, 14, 14, 14,  8,  5,  5,  5,  7, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14,  8,  5,  5, 11,  3, 17, 16, 10,  5,  5,  7, 14, 14, 14, 14, 14 },
            { 14, 14,  8,  5, 11, 13, 13, 13,  2,  0,  0,  0, 12, 13, 10,  5,  5,  7, 14, 14 },
            { 14, 14,  6, 13, 13, 13,  2,  0,  1, 14, 14, 14,  9,  0, 12, 13, 13,  4, 14, 14 },
            { 14, 14,  6, 18,  2,  0,  1, 14, 14, 14, 14, 14, 14, 14,  9, 12, 13, 10,  7, 14 },
            { 14,  8, 11, 20,  4, 14, 14, 14,  8,  5,  7, 14, 14, 14, 14,  9,  0,  0,  1, 14 },
            { 14,  6, 13,  2,  1, 14, 14,  8, 11,  2,  1, 14, 14,  8,  7, 14, 14, 14, 14, 14 },
            {  8, 11, 13,  4, 14, 14,  8, 11,  2,  1, 14, 14,  8, 11,  4, 14, 14, 14, 14, 14 },
            {  6,  2,  0,  1, 14,  8, 11,  2,  1, 14, 14, 14,  6, 13,  4, 14,  8,  5,  7, 14 },
            {  9,  1, 14, 14, 14,  6,  2,  1, 14, 14, 14, 14,  6, 13,  4, 14,  9, 12, 10,  7 },
            { 14, 14, 14, 14,  8, 11,  4, 14, 14, 14,  8,  5, 11, 13,  4, 14, 14,  6, 13,  4 },
            { 14, 14, 14, 14,  9, 12,  4, 14, 14, 14,  6, 13, 13,  2,  1, 14, 14,  6,  2,  1 },
            {  8,  5,  7, 14, 14,  6, 10,  5,  5,  5, 11,  2,  0,  1, 14, 14,  8, 11,  4, 14 },
            {  9, 12, 10,  5,  7,  9,  0, 12,  3, 16,  2,  1, 14, 14, 14,  8, 11,  2,  1, 14 },
            { 14,  6, 13, 13,  4, 14, 14,  9,  0,  0,  1, 14, 14, 14, 14,  6, 13,  4, 14, 14 },
            { 14,  6, 13, 13, 10,  5,  7, 14, 14, 14, 14, 14, 14,  8,  5, 11,  2,  1, 14, 14 },
            { 14,  9, 12, 13, 13, 13, 10,  5,  7, 14, 14,  8,  5, 11, 13,  2,  1, 14, 14, 14 },
            { 14, 14,  9, 12,  3, 17, 17, 16,  4, 14,  8, 11,  3, 16,  2,  1, 14, 14, 14, 14 },
            { 14, 14, 14,  9, 12,  2,  0,  0,  1, 14,  9,  0,  0,  0,  1, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14,  9,  1, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
        };
        kLandformArray[3] = new int[][] {
            { 14, 14, 14, 14, 14, 14, 14, 14,  8,  5,  5,  5,  7, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14,  8, 11, 13, 13, 13, 10,  7, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14,  8, 11, 13, 13, 13, 13, 13, 10,  5,  5,  7, 14, 14, 14 },
            { 14, 14, 14, 14, 14,  8, 11,  3, 16,  2,  0,  0, 12, 13, 13, 13, 10,  7, 14, 14 },
            { 14, 14, 14, 14,  8, 11, 13, 13, 13,  4, 14, 14,  9, 12, 13, 13, 18, 10,  5,  7 },
            { 14, 14, 14,  8, 11, 18, 13, 13, 13,  4, 14, 14, 14,  9, 12, 13, 20, 13,  2,  1 },
            { 14, 14,  8, 11, 13, 19, 13, 13,  2,  1, 14, 14, 14, 14,  6, 13, 13,  2,  1, 14 },
            { 14, 14,  9, 12, 13, 20,  2,  0,  1, 14, 14, 14, 14, 14,  6, 13,  2,  1, 14, 14 },
            { 14, 14, 14,  9, 12,  2,  1, 14, 14, 14, 14, 14, 14, 14,  6, 13,  4, 14, 14, 14 },
            { 14, 14, 14, 14,  9,  1, 14, 14,  8,  5,  7, 14, 14, 14,  6, 13,  4, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14,  8, 11,  2,  1, 14, 14,  8, 11, 13, 10,  5,  7, 14 },
            { 14, 14, 14,  8,  7, 14, 14,  6, 13,  4, 14, 14,  8, 11, 13, 13, 13, 13, 10,  7 },
            { 14, 14,  8, 11,  4, 14, 14,  9,  0,  1, 14,  8, 11, 13, 13, 13,  2,  0,  0,  1 },
            { 14,  8, 11, 18, 10,  7, 14, 14, 14, 14,  8, 11, 13, 13, 13,  2,  1, 14, 14, 14 },
            {  8, 11, 13, 19,  2,  1, 14, 14, 14,  8, 11,  3, 17, 16,  2,  1, 14, 14, 14, 14 },
            {  6, 13, 13, 19,  4, 14, 14, 14,  8, 11, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14 },
            {  9, 12, 13, 20,  4, 14, 14,  8, 11, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14 },
            { 14,  9, 12,  2,  1, 14, 14,  9, 12, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14,  9,  1, 14, 14, 14, 14,  9, 12, 13,  4, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14, 14, 14,  9,  0,  1, 14, 14, 14, 14, 14, 14, 14, 14 },
        };
        kLandformArray[4] = new int[][] {
            { 14, 14, 14, 14, 14, 14, 14,  8,  5,  5,  5,  7, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14,  8,  5,  5, 11,  3, 17, 16, 10,  5,  5,  7, 14, 14, 14, 14, 14 },
            { 14, 14,  8,  5, 11, 13, 13, 13,  2,  0,  0,  0, 12, 13, 10,  5,  5,  7, 14, 14 },
            { 14, 14,  6, 13, 13, 13,  2,  0,  1, 14, 14, 14,  9,  0, 12, 13, 13,  4, 14, 14 },
            { 14, 14,  6, 18,  2,  0,  1, 14, 14, 14, 14, 14, 14, 14,  9, 12, 13, 10,  7, 14 },
            { 14,  8, 11, 20,  4, 14, 14, 14,  8,  5,  7, 14, 14, 14, 14,  9,  0,  0,  1, 14 },
            { 14,  6, 13,  2,  1, 14, 14,  8, 11,  2,  1, 14, 14,  8,  7, 14, 14, 14, 14, 14 },
            {  8, 11, 13,  4, 14, 14,  8, 11,  2,  1, 14, 14,  8, 11,  4, 14, 14, 14, 14, 14 },
            {  6,  2,  0,  1, 14,  8, 11,  2,  1, 14, 14, 14,  6, 13,  4, 14,  8,  5,  7, 14 },
            {  9,  1, 14, 14, 14,  6,  2,  1, 14, 14, 14, 14,  6, 13,  4, 14,  9, 12, 10,  7 },
            { 14, 14, 14, 14,  8, 11,  4, 14, 14, 14,  8,  5, 11, 13,  4, 14, 14,  6, 13,  4 },
            { 14, 14, 14, 14,  9, 12,  4, 14, 14, 14,  6, 13, 13,  2,  1, 14, 14,  6,  2,  1 },
            {  8,  5,  7, 14, 14,  6, 10,  5,  5,  5, 11,  2,  0,  1, 14, 14,  8, 11,  4, 14 },
            {  9, 12, 10,  5,  7,  9,  0, 12,  3, 16,  2,  1, 14, 14, 14,  8, 11,  2,  1, 14 },
            { 14,  6, 13, 13,  4, 14, 14,  9,  0,  0,  1, 14, 14, 14, 14,  6, 13,  4, 14, 14 },
            { 14,  6, 13, 13, 10,  5,  7, 14, 14, 14, 14, 14, 14,  8,  5, 11,  2,  1, 14, 14 },
            { 14,  9, 12, 13, 13, 13, 10,  5,  7, 14, 14,  8,  5, 11, 13,  2,  1, 14, 14, 14 },
            { 14, 14,  9, 12,  3, 17, 17, 16,  4, 14,  8, 11,  3, 16,  2,  1, 14, 14, 14, 14 },
            { 14, 14, 14,  9, 12,  2,  0,  0,  1, 14,  9,  0,  0,  0,  1, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14,  9,  1, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
        };
        kLandformArray[5] = new int[][] {
            { 14,  8,  5,  7, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14,  6, 13, 10,  7, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
            {  8, 11, 13,  2,  1, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 },
            {  9, 12, 13,  4, 14, 14, 14, 14, 14, 14,  8,  7, 14, 14, 14, 14, 14, 14, 14, 14 },
            { 14,  9,  0,  1, 14, 14, 14, 14, 14, 14,  6, 10,  5,  7, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14, 14, 14,  8, 11, 13, 13, 10,  7, 14, 14, 14, 14, 14 },
            { 14, 14, 14,  8,  5,  5,  5,  5,  5, 11, 13, 13, 13, 13, 10,  5,  5,  7, 14, 14 },
            { 14, 14,  8, 11, 13, 13, 13, 13,  3, 17, 17, 16, 13, 13, 13, 13, 13, 10,  7, 14 },
            { 14,  8, 11, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 18, 13, 13, 13, 10,  7 },
            {  8, 11, 13,  2,  0,  0, 12, 13, 13, 18, 13, 21, 13, 13, 19, 13, 13, 13,  2,  1 },
            {  6, 13,  2,  1, 14, 14,  9, 12, 13, 20, 13, 21, 13, 13, 20, 13, 13,  2,  1, 14 },
            {  6,  2,  1, 14, 14, 14, 14,  9, 12, 13, 13, 21, 13, 13, 13, 13,  2,  1, 14, 14 },
            {  9,  1, 14,  8,  5,  7, 14, 14,  6, 13, 13, 21, 13, 13, 13,  2,  1, 14, 14, 14 },
            { 14, 14,  8, 11, 13,  4, 14, 14,  6, 13, 13, 13, 13, 13,  2,  1, 14, 14, 14, 14 },
            { 14,  8, 11, 13,  2,  1, 14,  8, 11, 13, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14 },
            {  8, 11, 13, 13,  4,  8,  5, 11, 13, 13, 13, 13, 13,  4, 14, 14, 14, 14, 14, 14 },
            {  6, 13, 13,  2,  1,  9, 12, 13, 13, 13,  3, 17, 16,  4, 14, 14, 14, 14, 14, 14 },
            {  6, 13,  2,  1, 14, 14,  9, 12, 13, 13, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14 },
            {  9,  0,  1, 14, 14, 14, 14,  9, 12, 13, 13,  2,  1, 14, 14, 14, 14, 14, 14, 14 },
            { 14, 14, 14, 14, 14, 14, 14, 14,  9,  0,  0,  1, 14, 14, 14, 14, 14, 14, 14, 14 },
        };
    }

    //--------- 校正陆地chunk块在地形数组中的行索引和列索引
    public static int reviseLandChunkRow(int n) {
        int resultRow = n;
        int maxNumber = kLandformArray[kLandformIndex].length;
        if (n > maxNumber) {
            resultRow = n;
        } else if (n < 0) {
            resultRow = 0;
        }
        return resultRow;
    }

    public static int reviseLandChunkCol(int n) {
        int resultCol = n;
        int maxNumber = kLandformArray[kLandformIndex][0].length;
        if (n > maxNumber) {
            resultCol = maxNumber;
        } else if (n < 0) {
            resultCol = 0;
        }
        return resultCol;
    }

    //--------- 物体对象相关
    public static final float kObjectPosArray[][][] = new float[6][][];     // 物体位置数组

    public static final float kObjectTreeWidth  = 130.0f;       // 树对象的宽度
    public static final float kObjectTreeHeight = 150.0f;       // 树对象的高度

    public static void initObjectInfo() {              // 初始化物体对象信息

        // 初始化物体位置数组
        kObjectPosArray[0] = new float[][] {
            {   // 树所在位置
                 2.50f,  2.25f,  2.25f,  2.00f,  2.25f,  3.00f,  1.00f,  2.75f,  3.50f, 16.25f,  4.50f, 13.50f,  4.25f, 13.50f, 13.00f,  9.50f,
                11.00f,  6.25f, 11.50f,  6.25f, 12.0f,   6.50f, 12.50f,  7.00f, 13.00f,  9.50f, 13.50f,  9.75f, 12.25f, 11.00f, 12.25f, 11.75f,
                10.75f, 11.25f, 10.75f, 11.75f, 15.25f,  9.00f, 15.75f,  9.00f, 15.25f, 11.00f, 10.50f, 14.50f, 12.50f, 14.50f, 10.50f, 15.50f,
                10.50f, 18.50f,  9.50f,  8.50f, 17.50f,  8.50f,  1.50f,  9.50f,  1.50f, 10.50f,  2.50f,  9.50f,  3.00f,  9.50f,  4.50f,  8.50f,
                 5.00f,  8.00f,  5.50f,  7.50f
            },
            {   // 茶壶所在位置
                2.50f,  0.25f
            },
            {   // 圆环所在位置
                11.50f, 16.50f
            },
            {   // 球所在位置
                1.00f,  3.75f
            },
        };
        kObjectPosArray[1] = new float[][] {
            {   // 树所在位置
                 2.00f, 16.00f,  2.25f, 16.00f,  2.50f, 16.00f,  2.75f, 16.00f,  3.00f, 16.00f,  4.125f, 15.00f,  4.125f, 15.50f,  4.125f, 16.50f,
                 2.00f, 17.00f,  4.00f, 13.00f,  4.25f, 13.00f,  5.00f,  9.00f,  5.25f,  9.00f,  3.00f,   7.00f,  3.50f,   7.00f,  4.75f,   6.50f,
                 7.00f,  7.00f,  7.00f,  6.00f,  8.25f,  2.25f,  8.75f,  2.75f,  9.50f,  1.50f, 10.00f,   2.00f, 10.50f,   2.00f, 13.50f,   3.50f,
                15.00f,  4.00f, 16.00f,  5.00f, 15.50f,  5.00f, 15.50f,  6.50f, 16.50f,  6.50f, 19.0f,    5.00f, 15.50f,   7.50f, 16.00f,   9.00f,
                18.00f, 12.00f, 17.50f, 11.50f, 13.50f, 12.50f, 12.50f, 15.50f, 10.50f, 18.50f,  9.00f,  10.00f, 10.00f,  10.00f,  9.00f,  12.00f
            },
            {  // 茶壶所在位置
                8.25f, 7.25f
            },
            {  // 圆环所在位置
                14.50f, 3.50f
            },
            {  // 球所在位置
                3.25f,  7.00f
            },
        };
    }

    //--------- 天空穹相关
    public static final float kSkyBallRadius = kLandChunkUnitSize * (kLandHeightmapWidth - 1) * (kLandChunkDrawSize / 2.0f);    // 半径
    public static float kSkyBallRotateAngle = 0.0f;      // 旋转角度（天空穹在做自旋转）

    //--------- 水面相关
    public static final int kWaterRows = 200;    // 水面的行数
    public static final int kWaterCols = 200;    // 水面的列数

    public static final float kWaterSpan = 100.0f;  // 水面格子的单位间隔

    public static final float kWaterSide = kWaterRows * kWaterSpan;     // 水面边长

    //--------- 摄像机参数
    public static float[] kCameraRotateMatrix;

    public static float kCameraPosX;            // 摄像机的X位置
    public static float kCameraPosY;            // 摄像机的Y位置
    public static float kCameraPosZ;            // 摄像机的Z位置

    public static float kCameraTargetX;         // 摄像机目标点的X位置
    public static float kCameraTargetY;         // 摄像机目标点的Y位置
    public static float kCameraTargetZ;         // 摄像机目标点的Z位置

    public static float kCameraRotateAngleX;    // 摄像机绕X轴旋转角度
    public static float kCameraRotateAngleY;    // 摄像机绕Y轴旋转角度
    public static float kCameraRotateAngleZ;    // 摄像机绕Z轴旋转角度

    public static float kCameraRotateStep;      // 摄像机旋转步值（仅左右旋转，即沿Y轴旋转）
    public static float kCameraDistance;        // 摄像机距目标点的距离

    public static float kCameraMoveSpan;        // 摄像机每次向前移动的距离

    public static void initCameraInfo() {

        // 以防万一，加个锁
        synchronized (lock) {

            kCameraRotateMatrix = new float[16];
            android.opengl.Matrix.setIdentityM(kCameraRotateMatrix, 0);

            // 初始化摄像机距离、移动步长
            kCameraRotateStep = 2.0f;
            kCameraDistance = 180.0f;
            kCameraMoveSpan = 0.0f;

            // 初始摄像机目标位置
            kCameraTargetX = 0.0f;       // 摄像机目标点的X位置
            kCameraTargetY = 330.0f;  // 摄像机目标点的Y位置
            kCameraTargetZ = 0.0f;       // 摄像机目标点的Z位置

            // 初始摄像机旋转角度
            kCameraRotateAngleX = 0.0f;
            kCameraRotateAngleY = 0.0f;
            kCameraRotateAngleZ = 0.0f;

            // 计算摄像机位置
            kCameraPosX = (float) (kCameraTargetX + Math.sin(Math.toRadians(kCameraRotateAngleY)) * kCameraDistance);
            kCameraPosY = kCameraTargetY;
            kCameraPosZ = (float) (kCameraTargetZ + Math.cos(Math.toRadians(kCameraRotateAngleY)) * kCameraDistance);
        }
    }

    //--------- 前进按钮相关
    public static float   kForwardBtnWidth = 0.2f;     // 按钮宽度
    public static float   kForwardBtnHeight = 0.2f;    // 按钮高度

    public static float   kForwardBtnXOffset = 0.49f;  // 按钮X方向平移距离
    public static float   kForwardBtnYOffset = -0.6f; // 按钮Y方向平移距离

    public static float[] kForwardBtnArea;             // 按钮所在区域范围

    //--------- 后退按钮相关
    public static float   kBackBtnWidth = 0.2f;        // 按钮宽度
    public static float   kBackBtnHeight = 0.2f;       // 按钮高度

    public static float   kBackBtnXOffset = 0.49f;     // 按钮X方向平移距离
    public static float   kBackBtnYOffset = -0.85f;     // 按钮Y方向平移距离

    public static float[] kBackBtnArea;                // 按钮所在区域范围
}
