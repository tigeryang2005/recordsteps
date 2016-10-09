package com.tiger.example.recordsteps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static String TAG = "mainActivity";
    private TextView textView;
    private Button button;
    private int step = 0;   //步数
    private double lstValue = 0;  //上次的值
    private boolean motiveState = true;   //是否处于运动状态
    private boolean processState = false;   //标记当前是否已经在计步
    private int x;
    private int y;
    private int picWidth;
    private int picHeight;
    private ImageView imageView;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        textView = (TextView) findViewById(R.id.text_view_step);
        button = (Button) findViewById(R.id.button_start);
        imageView = (ImageView) findViewById(R.id.imageView);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
        picWidth = bitmap.getWidth();
        picHeight = bitmap.getHeight();
        Log.d(TAG, "onCreate: 图片实际宽高" + picWidth + "  " + picHeight);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                //ImageView的宽和高
                Log.d("lxy", "iv_W = " + imageView.getWidth() + ", iv_H = " + imageView.getHeight());

                //获得ImageView中Image的真实宽高，
                int dw = imageView.getDrawable().getBounds().width();
                int dh = imageView.getDrawable().getBounds().height();
                Log.d("lxy", "drawable_X = " + dw + ", drawable_Y = " + dh);

                //获得ImageView中Image的变换矩阵
                Matrix m = imageView.getImageMatrix();
                float[] values = new float[10];
                m.getValues(values);

                //Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
                float sx = values[0];
                float sy = values[4];
                Log.d("lxy", "scale_X = " + sx + ", scale_Y = " + sy);

                //计算Image在屏幕上实际绘制的宽高
                int cw = (int) (dw * sx);
                int ch = (int) (dh * sy);
                Log.d("lxy", "caculate_W = " + cw + ", caculate_H = " + ch);
            }
        });
        button.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double range = 1;
        float[] value = sensorEvent.values;
        double curValue = magnitude(value[0], value[1], value[2]);
        TextView textViewX = (TextView) findViewById(R.id.text_view_x);
        TextView textViewY = (TextView) findViewById(R.id.text_view_y);
        TextView textViewZ = (TextView) findViewById(R.id.text_view_z);
        textViewX.setText("X: " + String.valueOf(value[0]));
        textViewY.setText("Y: " + String.valueOf(value[1]));
        textViewZ.setText("Z: " + String.valueOf(value[2]));
        if (motiveState) {
            if (curValue >= lstValue) {
                lstValue = curValue;
            } else {
                if (Math.abs(curValue - lstValue) > range) {
                    motiveState = false;
                }
            }
        } else {
            if (curValue <= lstValue) lstValue = curValue;
            else {
                if (Math.abs(curValue - lstValue) > range) {
                    //检测到一次峰值
                    if (processState) {
                        step++;  //步数 + 1
                        if (processState) {
                            textView.setText(String.valueOf(step));//读数更新
                        }
                    }
                    motiveState = true;
                }
            }
        }
        x = picWidth - imageView.getWidth();
        if (imageView.getScrollX() <= x && imageView.getScrollX() >= 0) {
            Log.d(TAG, "onSensorChanged: X轴移动 " + String.valueOf(Math.round(value[0])));
            imageView.scrollBy((0 - Math.round(value[0])) * 4, 0);
            if (imageView.getScrollX() > x) {
                imageView.setScrollX(x);
            }
            if (imageView.getScrollX() < 0) {
                imageView.setScrollX(0);
            }
        }
        y = picHeight - imageView.getHeight();
        if (imageView.getScrollY() <= y && imageView.getScrollY() >= 0) {
            Log.d(TAG, "onSensorChanged: Y轴移动 " + String.valueOf(Math.round(value[1])));
            imageView.scrollBy(0, Math.round(value[1]) * 4);
            if (imageView.getScrollY() > y) {
                imageView.setScrollY(y);
            }
            if (imageView.getScrollY() < 0) {
                imageView.setScrollY(0);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        step = 0;
        textView.setText(R.string.init_step);
        if (processState) {
            button.setText(R.string.start);
            processState = false;
        } else {
            button.setText(R.string.stop);
            processState = true;
        }
    }

    public double magnitude(float x, float y, float z) {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
