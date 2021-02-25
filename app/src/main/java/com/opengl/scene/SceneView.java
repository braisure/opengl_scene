package com.opengl.scene;
import static com.opengl.scene.Constant.*;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.opengl.scene.models.CommonModel;
import com.opengl.scene.models.ModelLoader;
import com.opengl.scene.objects.Landform;
import com.opengl.scene.objects.SeaWater;
import com.opengl.scene.objects.SkyBall;
import com.opengl.scene.objects.TextureRect;
import com.opengl.scene.objects.Tree;
import com.opengl.scene.utils.AABB3;
import com.opengl.scene.utils.IntersectantUtil;
import com.opengl.scene.utils.MatrixState;
import com.opengl.scene.utils.ShaderManager;
import com.opengl.scene.utils.ShaderUtil;
import com.opengl.scene.utils.Vector3f;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by bozhao on 2017/12/15.
 */

public class SceneView extends GLSurfaceView {
    public MainActivity     mActivity;   // 主Activity用用
    public KeyThread        mKeyThread;

    private SceneRenderer   mRenderer;  // 场景渲染器

    private boolean mIsLoadedCompleted = false; // 是否加载完成的标志位
    private int mLoadStep = 0;  // 加载资源的步数

    //--------- 当然帧的数值
    private float mCurrCameraPosX;
    private float mCurrCameraPosY;
    private float mCurrCameraPosZ;

    private float mCurrCameraTargetX;
    private float mCurrCameraTargetY;
    private float mCurrCameraTargetZ;

    private float mCurrCameraRotateAngleX;
    private float mCurrCameraRotateAngleY;
    private float mCurrCameraRotateAngleZ;

    //--------- 透视投影的缩放比
    public float mRatio;

    //--------- 渲染对象
    TextureRect mLoadingView;       // 加载界面
    TextureRect mLoadProcessBar;    // 加载界面中的进度条
    SkyBall     mSkyBall;            // 天空穹
    // TextureRect mWater;              // 水面
    SeaWater    mWater;              // 水面
    Landform    mLandform[] = new Landform[kLandHeightmapNumber];     // 陆地
    TextureRect mTreeRect;           // 树
    TextureRect mForwardBtn;        // 前进按钮
    TextureRect mBackBtn;            // 后退按钮

    public static ArrayList<Tree> mTreeList = new ArrayList<Tree>(); // 树列表(列表中的元素本质是广告牌)

    CommonModel  mChModel;          // 茶壶模型
    CommonModel  mYhModel;          // 圆环模型
    CommonModel  mQtModel;          // 球模型

    //--------- 对象相关纹理
    private int mLoadingViewTexId;
    private int mLoadProcessBarTexId;
    private int mSkyBallTexId;           // 天空穹纹理
    private int mWaterTexId;              // 水面纹理
    private int mLandformTuCengTexId;   // 地形纹理 ----土层
    private int mLandformCaoDiTexId;    // 地形纹理 ----草地
    private int mLandformShiTouTexId;   // 地形纹理 ----石头
    private int mLandformShanDingTexId; // 地形纹理 ----山顶
    private int mTreeTexId;               // 树纹理
    private int mTree2TexId;
    private int mArrowTexId;              // 箭头图标纹理

    public SceneView(Context context) {
        super(context);
        mActivity = (MainActivity)context;

        float[] testMatrix = new float[16];
        Matrix.setIdentityM(testMatrix, 0);
       // Matrix.setLookAtM(testMatrix, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0);
        Matrix.translateM(testMatrix, 0, 1,1,1);

        setEGLContextClientVersion(2);    // 设置使用OPENGL ES2.0
        mRenderer = new SceneRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setKeepScreenOn(true);

        setOnTouchListener(new OnTouchListener() {
            float previousX, previousY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent != null) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        previousX = motionEvent.getX();
                        previousY = motionEvent.getY();

                        // 前进
                        if (previousX > kForwardBtnArea[0] && previousX < kForwardBtnArea[1] && previousY > kForwardBtnArea[2] && previousY < kForwardBtnArea[3]) {
                            mForwardBtn.mIsButtonDown = 1;
                            synchronized (lock) {
                                kCameraMoveSpan = 20.0f;
                            }
                        }

                        // 后退
                        if (previousX > kBackBtnArea[0] && previousX < kBackBtnArea[1] && previousY > kBackBtnArea[2] && previousY < kBackBtnArea[3]) {
                            mBackBtn.mIsButtonDown = 1;
                            synchronized (lock) {
                                kCameraMoveSpan = -20.0f;
                            }
                        }

                        // 计算仿射变换后AB两点的位置
                        float[] AB = IntersectantUtil.calculateABPosition
                        (
                            previousX,      // 触控点X坐标
                            previousY,      // 触控点Y坐标
                            kScreenWidth,    // 屏幕宽度
                            kScreenHeight,   // 屏幕长度
                            mRatio,         // 视角left、top值
                            1.0f,
                            3,        // 视角near、far值
                            40000
                        );
                        // 射线AB
                        Vector3f start = new Vector3f(AB[0], AB[1], AB[2]); // 起点
                        Vector3f end = new Vector3f(AB[3], AB[4], AB[5]);   // 终点
                        Vector3f dir = end.minus(start);  // 长度和方向

                        // 判断是否选中茶壶
                        AABB3 box = mChModel.getBoundBox();
                        float t = box.rayIntersect(start, dir, null);
                        if (t <= 1.0f) {
                            mChModel.changeColor();
                        }

                        // 判断是否选中圆环
                        box = mYhModel.getBoundBox();
                        t = box.rayIntersect(start, dir, null);
                        if (t <= 1.0f) {
                            mYhModel.changeColor();
                        }

                        // 判断是否选中球
                        box = mQtModel.getBoundBox();
                        t = box.rayIntersect(start, dir, null);
                        if (t <= 1.0f) {
                            mQtModel.changeColor();

                        }

                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        // 复位
                        mForwardBtn.mIsButtonDown = 0;
                        mBackBtn.mIsButtonDown = 0;
                        synchronized (lock) {
                            kCameraMoveSpan = 0.0f;
                        }
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        if (mActivity.mIsHandleTouchDrag) {
                            final float deltaX = motionEvent.getX() - previousX;
                            final float deltaY = motionEvent.getY() - previousY;
                            previousX = motionEvent.getX();
                            previousY = motionEvent.getY();
                            queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    handleTouchDrag(deltaX, deltaY);
                                }
                            });
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public void handleTouchDrag(float deltaX, float deltaY) {
        synchronized (lock) {
            // kCameraRotateAngleX += deltaY / 10f;
            kCameraRotateAngleY += deltaX / 10f;
        }
    }

    private class SceneRenderer implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(1.0f,0.0f,0.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_CULL_FACE);     // 打开背面剪裁
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);    // 打开深度检测

            // 初始化变换矩阵栈
            MatrixState.setInitStack();

            // 加载和编译着色器第一个视图着色器，即loading视图着色器
            ShaderManager.loadFistViewCodeFromFile(SceneView.this.getResources());
            ShaderManager.compileFistViewShader();

            // 加载界面相关
            mLoadingView = new TextureRect(2, 2, ShaderManager.getFirstViewShaderProgram());
            mLoadingViewTexId = ShaderUtil.loadTexture(SceneView.this.getResources(), R.drawable.loading, false);

            mLoadProcessBar = new TextureRect(2, 0.1f, ShaderManager.getFirstViewShaderProgram());
            mLoadProcessBarTexId = ShaderUtil.loadTexture(SceneView.this.getResources(), R.drawable.process, false);

            // 初始化摄像机信息
            initCameraInfo();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int i, int i1) {
            // 设置视窗大小及位置
            GLES20.glViewport(0, 0, i, i1);

            // 计算GLSurfaceView的宽高比
            mRatio = (float)i / i1;

            ConfigVirtualButtonArea();
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            if (!mIsLoadedCompleted) {
                drawOrthLoadingView();
                loadResource();     // 加载资源
            } else {
                drawPerspective();
                drawVirtualIcon();
            }
        }

        /**
         * 正交投影绘制加载界面
         */
        public void drawOrthLoadingView() {
            MatrixState.setProjectOrtho(-1, 1, -1, 1, 1, 10);
            MatrixState.setCamera(0,0,1,0,0,-1,0,1,0);
            //MatrixState.copyMVMatrix();

            //  因为开启了深度测试，这里先绘制进度条，在会在loading界面
            // 绘制精度条
            MatrixState.pushMatrix();
            MatrixState.translate(-2 + 2 * mLoadStep / (float)22, -1 + 0.05f, 0f);
            mLoadProcessBar.drawSelf(mLoadProcessBarTexId);
            MatrixState.popMatrix();

            // 绘制loading界面
            MatrixState.pushMatrix();
            mLoadingView.drawSelf(mLoadingViewTexId);
            MatrixState.popMatrix();
        }
    }

    /**
     * 透视投影绘制场景
     */
    public void drawPerspective() {
        // MatrixState.setProjectPerspective(45.0f, mRatio, 3, 40000);
        MatrixState.setProjectFrustum(-mRatio, mRatio, -1, 1, 3, 40000);

        MatrixState.pushMatrix();
        synchronized (lock)
        {
            mCurrCameraPosX = kCameraPosX;
            mCurrCameraPosY = kCameraPosY;
            mCurrCameraPosZ = kCameraPosZ;
            mCurrCameraTargetX = kCameraTargetX;
            mCurrCameraTargetY = kCameraTargetY;
            mCurrCameraTargetZ = kCameraTargetZ;
            mCurrCameraRotateAngleX = kCameraRotateAngleX;
            mCurrCameraRotateAngleY = kCameraRotateAngleY;
            mCurrCameraRotateAngleZ = kCameraRotateAngleZ;

            /*float[] testMatrix = new float[16];
            Matrix.setRotateM(testMatrix, 0, mCurrCameraRotateAngleY, 0,1,0);
            float[] testVector = new float[4];
            testVector[0] = (float)(Math.sin(Math.toRadians(-mCurrCameraRotateAngleZ)));
            testVector[1] = (float)(Math.cos(Math.toRadians(-mCurrCameraRotateAngleZ)) * Math.cos(Math.toRadians(mCurrCameraRotateAngleX)));
            testVector[2] = (float)(Math.cos(Math.toRadians(-mCurrCameraRotateAngleZ)) * Math.sin(Math.toRadians(mCurrCameraRotateAngleX)));
            testVector[3] = 1;
            float[] resultUp = new float[4];
            Matrix.multiplyMV(resultUp, 0, testMatrix, 0, testVector, 0);
            MatrixState.setCamera(mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ, mCurrCameraTargetX, mCurrCameraTargetY, mCurrCameraTargetZ,
                    resultUp[0], resultUp[1], resultUp[2]);*/
            MatrixState.setCamera(mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ, mCurrCameraTargetX, mCurrCameraTargetY, mCurrCameraTargetZ, 0,1,0);
            MatrixState.setLightLocation(100, 100, 100);
        }
        drawAllObject();
        MatrixState.popMatrix();
    }

    /**
     * 正交投影绘制虚拟图标
     */
    public void drawVirtualIcon() {
        MatrixState.setProjectOrtho(-mRatio, mRatio,-1f,1f,1,10);
        MatrixState.setCamera(0, 0, 0, 0, 0,-1, 0, 1, 0);
        //MatrixState.copyMVMatrix();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // 绘制前进按钮
        MatrixState.pushMatrix();
        MatrixState.translate(kForwardBtnXOffset, kForwardBtnYOffset, -2.0f);
        MatrixState.rotate(-90,0,0,1);
        mForwardBtn.drawSelf(mArrowTexId);
        MatrixState.popMatrix();

        // 绘制后退按钮
        MatrixState.pushMatrix();
        MatrixState.translate(kBackBtnXOffset, kBackBtnYOffset, -2.0f);
        MatrixState.rotate(90,0,0,1);
        mBackBtn.drawSelf(mArrowTexId);
        MatrixState.popMatrix();

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    /**
     * 加载所有的资源
     */
    public void loadResource() {
        switch (mLoadStep) {
            case 0:
                loadShader();
                mLoadStep++;
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                loadAllTexture(mLoadStep);
                mLoadStep++;
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
                loadAllObject(mLoadStep);
                mLoadStep++;
                break;
            case 21:
                mIsLoadedCompleted = true;
                mLoadingView = null;
                mLoadProcessBar = null;
                break;
        }
    }

    private void loadShader() {
        ShaderManager.loadCodeFromFile(getResources());
        ShaderManager.compileShader();
    }

    private void loadAllTexture(int step) {
        switch (step) {
            case 1:
                mSkyBallTexId = ShaderUtil.loadTexture(getResources(), R.drawable.sky, false);
                break;
            case 2:
                mWaterTexId = ShaderUtil.loadTexture(getResources(), R.drawable.water, false);
                break;
            case 3:
                mLandformTuCengTexId = ShaderUtil.loadTexture(getResources(), R.drawable.tuceng, false);
                mLandformCaoDiTexId = ShaderUtil.loadTexture(getResources(), R.drawable.caodi, false);
                //mLandformCaoDiTexId = ShaderUtil.loadTexture(getResources(), R.drawable.grass, false);
                mLandformShiTouTexId = ShaderUtil.loadTexture(getResources(), R.drawable.shitou, false);
                mLandformShanDingTexId = ShaderUtil.loadTexture(getResources(), R.drawable.stone, false);
                break;
            case 4:
                mTreeTexId = ShaderUtil.loadTexture(getResources(), R.drawable.tree,false);
                mTree2TexId =ShaderUtil.loadTexture(getResources(), R.drawable.tree2,false);
                break;
            case 5:
                mArrowTexId = ShaderUtil.loadTexture(getResources(), R.drawable.arrow, false);
                break;
            default:
                break;
        }
    }

    private void loadAllObject(int step) {
        switch (step) {
            case 11:
                mSkyBall = new SkyBall(kSkyBallRadius, ShaderManager.getOnlyTextureShaderProgram(), 0, 0, 0);
                mWater = new SeaWater(kWaterSide, kWaterSide, ShaderManager.getWaterShaderProgram());
                break;
            case 12:
                // 加载地形信息
                Constant.initLandformInfo(getResources());
                for (int i = 0;i < kLandHeightmapNumber; ++i) {
                    mLandform[i] = new Landform(i, ShaderManager.getLandformShaderProgram());
                }
                break;
            case 13:
                // 加载树信息
                Constant.initObjectInfo();
                mTreeRect = new TextureRect(kObjectTreeWidth, kObjectTreeHeight, ShaderManager.getOnlyTextureShaderProgram());
                for(int i = 0; i < kObjectPosArray[kLandformIndex][0].length / 4; ++i) {

                    mTreeList.add(new Tree(mTreeRect, mTree2TexId,
                            kObjectPosArray[kLandformIndex][0][i * 4] * kLandChunkWidth,
                            kLandformPlainHeight + kObjectTreeHeight / 2.0f - 5.0f,
                            kObjectPosArray[kLandformIndex][0][i * 4 + 1] * kLandChunkHeight,
                            (int)kObjectPosArray[kLandformIndex][0][i * 4 + 1],
                            (int)kObjectPosArray[kLandformIndex][0][i * 4])) ;

                    mTreeList.add(new Tree(mTreeRect, mTreeTexId,
                            kObjectPosArray[kLandformIndex][0][i * 4 + 2] * kLandChunkWidth,
                            kLandformPlainHeight + kObjectTreeHeight / 2.0f - 5.0f,
                            kObjectPosArray[kLandformIndex][0][i * 4 + 3] * kLandChunkHeight,
                            (int)kObjectPosArray[kLandformIndex][0][i * 4 + 3],
                            (int)kObjectPosArray[kLandformIndex][0][i * 4 + 2]));
                }
                break;
            case 14:
                mChModel = ModelLoader.loadFromObjFile(getResources(), "ch.obj", ShaderManager.getModelShaderProgram());
                mYhModel = ModelLoader.loadFromObjFile(getResources(), "yh.obj", ShaderManager.getModelShaderProgram());
                mQtModel = ModelLoader.loadFromObjFile(getResources(), "qt.obj", ShaderManager.getModelShaderProgram());
                break;
            case 15:
                mForwardBtn = new TextureRect(kForwardBtnWidth, kForwardBtnHeight, ShaderManager.getButtonShaderProgram(), 1);
                mBackBtn =  new TextureRect(kBackBtnWidth, kBackBtnHeight, ShaderManager.getButtonShaderProgram(), 1);
                break;
            case 16:
                mKeyThread = new KeyThread(this);
                mKeyThread.start();
                break;
            default:
                break;
        }
    }

    private void drawAllObject() {

        int beginRow = 0;         // 起始行列
        int beginCol = 0;

        int endRow = kLandChunkDrawSize;  // 终止行列
        int endCol = kLandChunkDrawSize;

        int maxRow = kLandformArray[kLandformIndex].length;     // 最大行列
        int maxCol = kLandformArray[kLandformIndex][0].length;

        int currRow = (int)(mCurrCameraTargetZ / kLandChunkHeight); // 当前所在的行列
        int currCol = (int)(mCurrCameraTargetX / kLandChunkWidth);

        int rcCount = 2;    // 前进时后面要绘制的地块数

        mCurrCameraRotateAngleY %= 360;
        if (mCurrCameraRotateAngleY < 0) {
            mCurrCameraRotateAngleY = 360 + mCurrCameraRotateAngleY;
        }
        if((mCurrCameraRotateAngleY >= 0 && mCurrCameraRotateAngleY <= 45) || (mCurrCameraRotateAngleY >= 315 && mCurrCameraRotateAngleY <= 360)) {
            if (mCurrCameraTargetZ < 0) {  // 离开陆地
                beginRow = 0;
                endRow   = 1;
                beginCol = 0;
                endCol   = maxCol;
            } else if (mCurrCameraTargetZ > maxRow * kLandChunkHeight) {   // 进入陆地，全绘制
                beginRow = reviseLandChunkRow( maxRow - kLandChunkDrawSize / 2);
                endRow   = maxRow;
                beginCol = 0;
                endCol   = maxCol;
            } else {  // 在陆地中间，就只绘制前面的部分
                beginRow = reviseLandChunkRow( currRow - kLandChunkDrawSize / 2);
                endRow   = reviseLandChunkRow( currRow + rcCount);
                beginCol = reviseLandChunkCol( currCol - kLandChunkDrawSize / 2);
                endCol   = reviseLandChunkCol( currCol + kLandChunkDrawSize / 2);
            }
        } else if (mCurrCameraRotateAngleY >= 45 && mCurrCameraRotateAngleY < 135) {
            if (mCurrCameraTargetX < 0) {  // 离开陆地
                beginRow = 0;
                endRow   = maxRow;
                beginCol = 0;
                endCol   = 1;
            } else if (mCurrCameraTargetX >= maxCol * kLandChunkWidth) {  // 进入陆地，全绘制
                beginRow = 0;
                endRow   = maxRow;
                beginCol = reviseLandChunkCol( maxCol - kLandChunkDrawSize / 2);
                endCol   = maxCol;
            } else {   // 在陆地中间，只绘制前面的部分
                beginRow = reviseLandChunkRow( currRow - kLandChunkDrawSize / 2);
                endRow   = reviseLandChunkRow( currRow + kLandChunkDrawSize / 2);
                beginCol = reviseLandChunkCol( currCol - kLandChunkDrawSize / 2);
                endCol   = reviseLandChunkCol( currCol + rcCount);
            }
        } else if (mCurrCameraRotateAngleY >= 135 && mCurrCameraRotateAngleY < 225) {
            if (mCurrCameraTargetZ < 0) {  // 进入陆地，全绘制
                beginRow = 0;
                endRow   = kLandChunkDrawSize / 2;
                beginCol = 0;
                endCol   = maxCol;
            } else if (mCurrCameraTargetZ > maxRow * kLandChunkHeight) {  // 离开陆地
                beginRow = maxRow - 1;
                endRow   = maxRow;
                beginCol = 0;
                endCol   = maxCol;
            } else {  // 在陆地中间，只绘制前面的部分
                beginRow = reviseLandChunkRow( currRow - rcCount);
                endRow   = reviseLandChunkRow( currRow + kLandChunkDrawSize / 2);
                beginCol = reviseLandChunkCol( currCol - kLandChunkDrawSize / 2);
                endCol   = reviseLandChunkCol( currCol + kLandChunkDrawSize / 2);
            }
        } else {
            if (mCurrCameraTargetX < 0) {      // 进入陆地，全绘制
                beginRow = 0;
                endRow   = maxRow;
                beginCol = 0;
                endCol   = kLandChunkDrawSize / 2;
            } else if (mCurrCameraTargetX > maxCol * kLandChunkWidth) {   // 离开陆地
                beginRow = 0;
                endRow   = maxRow;
                beginCol = 0;
                endCol   = 1;
            } else {    // 在陆地中间，只绘制前面的部分
                beginRow = reviseLandChunkRow( currRow - kLandChunkDrawSize / 2);
                endRow   = reviseLandChunkRow( currRow + kLandChunkDrawSize / 2);
                beginCol = reviseLandChunkCol( currCol - rcCount);
                endCol   = reviseLandChunkCol( currCol + kLandChunkDrawSize / 2);
            }
        }
        drawSky();                                          // 绘制天空
        drawLandform(beginRow, beginCol, endRow, endCol);   // 绘制陆地
        // drawLandform(0, 0, 20, 20);
        drawWater();                                        // 绘制水面
        drawModel(beginRow, beginCol, endRow, endCol);      // 绘制模型
        drawTree(beginRow, beginCol, endRow, endCol);       // 绘制树(本质是广告牌)
    }

    private void drawSky() {
        mSkyBall.drawSelf(mSkyBallTexId, mCurrCameraTargetX, 0, mCurrCameraTargetZ, kSkyBallRotateAngle);
    }

    public void drawWater() {
        MatrixState.pushMatrix();
        MatrixState.translate(-kWaterSide / 2,0,-kWaterSide / 2);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mWater.drawSelf(mWaterTexId);
        GLES20.glDisable(GLES20.GL_BLEND);
        MatrixState.popMatrix();
    }

    public void drawLandform(int rowi, int colj, int rowT, int colT) {
        for (int i = rowi; i < rowT; ++i) {
            for (int j = colj; j < colT; ++j) {
                MatrixState.pushMatrix();
                MatrixState.translate(j * kLandChunkWidth, 0, i * kLandChunkHeight);
                try {
                    drawLandformUnit(kLandformArray[kLandformIndex][i][j]); // 根据编号绘制
                } catch(Exception e) {
                    e.printStackTrace();
                }
                MatrixState.popMatrix();
            }
        }
    }

    public void drawLandformUnit(int number) {
        switch (number) {
            case 0:
            case 1:
            case 2:
                mLandform[number].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 3:
                mLandform[number].drawSelf(1, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformHillSpan1, kLandformHillSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 4:
                MatrixState.translate(0, 0, kLandChunkHeight);
                MatrixState.rotate(90, 0, 1, 0);
                mLandform[0].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 5:
                MatrixState.translate(kLandChunkWidth, 0, kLandChunkHeight);
                MatrixState.rotate(180, 0, 1, 0);
                mLandform[0].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 6:
                MatrixState.translate(kLandChunkWidth, 0, 0);
                MatrixState.rotate(270, 0, 1, 0);
                mLandform[0].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 7:
                MatrixState.translate(0, 0, kLandChunkHeight);
                MatrixState.rotate(90, 0, 1, 0);
                mLandform[1].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 8:
                MatrixState.translate(kLandChunkWidth, 0, kLandChunkHeight);
                MatrixState.rotate(180, 0, 1, 0);
                mLandform[1].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 9:
                MatrixState.translate(kLandChunkWidth, 0, 0);
                MatrixState.rotate(270, 0, 1, 0);
                mLandform[1].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 10:
                MatrixState.translate(0, 0, kLandChunkHeight);
                MatrixState.rotate(90, 0, 1, 0);
                mLandform[2].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 11:
                MatrixState.translate(kLandChunkWidth, 0, kLandChunkHeight);
                MatrixState.rotate(180, 0, 1, 0);
                mLandform[2].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 12:
                MatrixState.translate(kLandChunkWidth, 0, 0);
                MatrixState.rotate(270, 0, 1, 0);
                mLandform[2].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 13:
                mLandform[7].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformPlainSpan1, kLandformPlainSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 15:
                mLandform[4].drawSelf(0, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformHillWaterSpan1, kLandformHillWaterSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 16:
                mLandform[5].drawSelf(1, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformHillSpan1, kLandformHillSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 17:
                mLandform[6].drawSelf(1, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformHillSpan1, kLandformHillSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 18:
                MatrixState.translate(kLandChunkWidth, 0, 0);
                MatrixState.rotate(270, 0, 1, 0);
                mLandform[3].drawSelf(1, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformHillSpan1, kLandformHillSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 19:
                MatrixState.translate(kLandChunkWidth, 0, 0);
                MatrixState.rotate(270, 0, 1, 0);
                mLandform[6].drawSelf(1, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformHillSpan1, kLandformHillSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;
            case 20:
                MatrixState.translate(kLandChunkWidth, 0, 0);
                MatrixState.rotate(270, 0, 1, 0);
                mLandform[5].drawSelf(1, mLandformShanDingTexId, mLandformTuCengTexId, mLandformCaoDiTexId,
                        mLandformShiTouTexId, kLandformHillSpan1, kLandformHillSpan2, mCurrCameraPosX, mCurrCameraPosY, mCurrCameraPosZ);
                break;

        }
    }

    public void drawModel(int rowi, int colj, int rowT, int colT) {

        // 绘制茶壶
        int rowNum = (int)kObjectPosArray[kLandformIndex][1][1];
        int colNum = (int)kObjectPosArray[kLandformIndex][1][0];
        if (rowNum >= rowi && rowNum <= rowT && colNum >= colj && colNum <= colT) {
            MatrixState.pushMatrix();
            MatrixState.translate(
                    kObjectPosArray[kLandformIndex][1][0] * kLandChunkWidth,
                    kLandformPlainHeight,
                    kObjectPosArray[kLandformIndex][1][1] * kLandChunkHeight);
            MatrixState.scale(12.0f, 12.0f, 12.0f);
            mChModel.drawSelf();
            MatrixState.popMatrix();
        }

        // 绘制圆环
        rowNum = (int)kObjectPosArray[kLandformIndex][2][1];
        colNum = (int)kObjectPosArray[kLandformIndex][2][0];
        if (rowNum >= rowi && rowNum <= rowT && colNum >= colj && colNum <= colT) {
            MatrixState.pushMatrix();
            MatrixState.translate(
                    kObjectPosArray[kLandformIndex][2][0] * kLandChunkWidth,
                    kLandformPlainHeight,
                    kObjectPosArray[kLandformIndex][2][1] * kLandChunkHeight);
            MatrixState.scale(12.0f, 12.0f, 12.0f);
            mYhModel.drawSelf();
            MatrixState.popMatrix();
        }

        // 绘制球体
        rowNum = (int)kObjectPosArray[kLandformIndex][3][1];
        colNum = (int)kObjectPosArray[kLandformIndex][3][0];
        if (rowNum >= rowi && rowNum <= rowT && colNum >= colj && colNum <= colT) {
            MatrixState.pushMatrix();
            MatrixState.translate(
                    kObjectPosArray[kLandformIndex][3][0] * kLandChunkWidth,
                    kLandformPlainHeight,
                    kObjectPosArray[kLandformIndex][3][1] * kLandChunkHeight);
            MatrixState.scale(12.0f, 12.0f, 12.0f);
            mQtModel.drawSelf();
            MatrixState.popMatrix();
        }
    }

    public void drawTree(int rowi, int colj, int rowT, int colT) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        for (int i = 0; i < mTreeList.size(); ++i) {
            mTreeList.get(i).drawSelf(rowi, colj, rowT, colT);
        }
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void ConfigVirtualButtonArea() {
        // 前进按钮范围
        float leftEdge   = (float)(mRatio - kForwardBtnWidth / 2.0f + kForwardBtnXOffset) / (2.0f * mRatio) * kScreenWidth;
        float rightEdge  = (float)(mRatio + kForwardBtnWidth / 2.0f + kForwardBtnXOffset) / (2.0f * mRatio) * kScreenWidth;
        float topEdge    = (float)(1.0f - kForwardBtnHeight / 2.0f - kForwardBtnYOffset) / 2.0f * kScreenHeight;
        float bottomEdge = (float)(1.0f + kForwardBtnHeight / 2.0f - kForwardBtnYOffset) / 2.0f * kScreenHeight;
        kForwardBtnArea = new float[] { leftEdge, rightEdge, topEdge, bottomEdge };

        // 后退按钮范围
        leftEdge   = (float)(mRatio - kBackBtnWidth / 2.0f + kBackBtnXOffset) / (2.0f * mRatio) * kScreenWidth;
        rightEdge  = (float)(mRatio + kBackBtnWidth / 2.0f + kBackBtnXOffset) / (2.0f * mRatio) * kScreenWidth;
        topEdge    = (float)(1.0f - kBackBtnHeight / 2.0f - kBackBtnYOffset) / 2.0f * kScreenHeight;
        bottomEdge = (float)(1.0f + kBackBtnHeight / 2.0f - kBackBtnYOffset) / 2.0f * kScreenHeight;
        kBackBtnArea = new float[] {leftEdge, rightEdge, topEdge, bottomEdge};
    }
}
