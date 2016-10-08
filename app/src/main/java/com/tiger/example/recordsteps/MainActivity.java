package com.tiger.example.recordsteps;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private TextView textView;
    private Button button;
    private int step = 0;   //步数
    private double lstValue = 0;  //上次的值
    private boolean motiveState = true;   //是否处于运动状态
    private boolean processState = false;   //标记当前是否已经在计步

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        textView = (TextView) findViewById(R.id.text_view_step);
        button = (Button) findViewById(R.id.button_start);
        button.setOnClickListener(this);
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
