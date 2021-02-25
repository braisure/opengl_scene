package com.opengl.scene;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.opengl.scene.utils.RotateUtil;

import static com.opengl.scene.Constant.*;

public class MainActivity extends Activity implements SensorEventListener {

    SceneView     mSceneView;       // 场景视图
    SensorManager mSensorManager;  // 传感器管理器

    Sensor mOrientSensor;   // 方向传感器

    Sensor mGyroscopeSensor;    // 陀螺仪传感器
    private long mGyroscopeTimestamp = 0;   // 陀螺仪传感器时间间隔

    Sensor mLinearAcceleratorSensor;   // 线性加速计传感器
    private long mLinearAcceleratorTimestamp = 0;   // 线性加速计传感器时间间隔

    private int mScreenRotateFlag;  // 当前屏幕是否能够旋转的标识

    public boolean mIsHandleTouchDrag = false;  // 用于判断是否需要通知触摸拖拽屏幕控制

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 打开屏幕旋转
        mScreenRotateFlag = Settings.System.getInt(this.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0);
        if (0 == mScreenRotateFlag) {
            Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION,1);
        }

        mSceneView = new SceneView(MainActivity.this);
        setContentView(mSceneView);

        // 初始化屏幕
        initScreen();

        // 控制设置
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        // 采用加速度传感器控制移动（有问题）
        // mLinearAcceleratorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (1 == kControlType) {         // 通过屏幕倾斜控制,依赖传感器
            mOrientSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        } else if (2 == kControlType) { // 通过触摸屏幕控制
            mIsHandleTouchDrag = true;

        } else if (3 == kControlType) { // 通过手机旋转控制,依赖传感器
            mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAcceleratorSensor, SensorManager.SENSOR_DELAY_UI);
        if (1 == kControlType) {
            mSensorManager.registerListener(this, mOrientSensor, SensorManager.SENSOR_DELAY_UI);
        } else if (3 == kControlType) {
            mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (mLinearAcceleratorTimestamp != 0) {
                    // 时间间隔，将纳秒转换为秒
                    final double dT = (sensorEvent.timestamp - mLinearAcceleratorTimestamp) * kNS2SFactor;

                    // 获取加速计采集到线性加速度向量
                    float accelerateX = sensorEvent.values[0];
                    float accelerateY = sensorEvent.values[1];
                    float accelerateZ = sensorEvent.values[2];

                    // 计算移动距离，公式：s=1/2a*t*t
                    float distanceX = (float)(accelerateX * dT * dT / 2.0f) * 10000.0f;
                    float distanceY = (float)(accelerateY * dT * dT / 2.0f) * 10000.0f;
                    float distanceZ = (float)(accelerateZ * dT * dT / 2.0f) * 10000.0f;

                    // 这里只用到沿Z轴移动距离
                    float resultDistance = 0.0f;
                    if (distanceZ > 2.0f) {
                        resultDistance = 20.0f;
                    } else if (distanceZ < -2.0f) {
                        resultDistance = -20.0f;
                    }
                    synchronized (lock) {
                        kCameraMoveSpan = resultDistance;
                    }
                }
                mLinearAcceleratorTimestamp = sensorEvent.timestamp;
                break;
            case Sensor.TYPE_ORIENTATION:
                // 记录方向传感器的数据，directionDotXY[0]表示左右旋转,
                float[] directionDotXY = RotateUtil.getDirectionDot(
                    new double[] {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]}
                );
                float mTempRadio = Math.abs(directionDotXY[0] - 4.0f) / 10;     // 旋转速度的比例
                synchronized (lock) {
                    if (directionDotXY[0] > 4.0f) {          // 左转
                        kCameraRotateAngleY += kCameraRotateStep * mTempRadio;
                        kCameraRotateAngleZ = directionDotXY[0] * 0.9f;
                    } else if (directionDotXY[0] < -4.0f) {  // 右转
                        kCameraRotateAngleY -= kCameraRotateStep * mTempRadio;
                        kCameraRotateAngleZ = directionDotXY[0] * 0.9f;
                    } else {                                    // 相关数据复位
                        kCameraRotateAngleZ = 0.0f;
                    }
                    kCameraRotateAngleX %= 360.0f;
                    kCameraRotateAngleY %= 360.0f;
                    kCameraRotateAngleZ %= 360.0f;
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (mGyroscopeTimestamp != 0) {
                    // 时间间隔，将纳秒转换为秒
                    final double dT = (sensorEvent.timestamp - mGyroscopeTimestamp) * kNS2SFactor;

                    // 获取陀螺仪采集到的旋转角速度向量
                    float axisX = sensorEvent.values[0];
                    float axisY = sensorEvent.values[1];
                    float axisZ = sensorEvent.values[2];

                    //  如果旋转向量偏移值足够大，可以获得坐标值，则规范化旋转向量
                    float omegaMagnitude = (float)Math.sqrt((double)(axisX * axisX + axisY * axisY + axisZ * axisZ));
                    if (omegaMagnitude > 0.01f) {
                        axisX /= omegaMagnitude;
                        axisY /= omegaMagnitude;
                        axisZ /= omegaMagnitude;
                    }

                    // 为了得到此次取样间隔的旋转偏移量，需要把围绕坐标轴旋转的角速度与时间间隔合并表示。
                    double thetaOverTwo = omegaMagnitude * dT;
                    synchronized (lock) {
                        // kCameraRotateAngleX += (float) Math.toDegrees(Math.sin(thetaOverTwo) * axisX);
                        kCameraRotateAngleY += (float) Math.toDegrees(Math.sin(thetaOverTwo) * axisY);
                        // kCameraRotateAngleZ += (float) Math.toDegrees(Math.sin(thetaOverTwo) * axisZ);
                        kCameraRotateAngleX %= 360.0f;
                        kCameraRotateAngleY %= 360.0f;
                        kCameraRotateAngleZ %= 360.0f;
                    }
                }
                mGyroscopeTimestamp = sensorEvent.timestamp;
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * 初始化屏幕
     */
    public void initScreen() {
        // 全屏显示
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 设置横屏
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 获取屏幕宽度、高度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (dm.widthPixels > dm.heightPixels) {
            kScreenWidth = dm.heightPixels;
            kScreenHeight = dm.widthPixels;
        } else {
            kScreenWidth = dm.widthPixels;
            kScreenHeight = dm.heightPixels;
        }
    }
}
