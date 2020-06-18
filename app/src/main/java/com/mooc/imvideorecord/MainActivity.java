package com.mooc.imvideorecord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        onPermission();
    }


    private void initView() {
        findViewById(R.id.bt_file_record_op).setOnClickListener(this);
        findViewById(R.id.bt_bytes_record_op).setOnClickListener(this);
        findViewById(R.id.bt_record_visibility).setOnClickListener(this);
        findViewById(R.id.bt_play_effets).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_file_record_op://文件形式录制MediaRecorder
                FileActivity.show(this);
                break;

            case R.id.bt_bytes_record_op://字节流形式录制AudioRecord
                StreamActivity.show(this);
                break;

            case R.id.bt_record_visibility://录音的可视化
                VisualizerActivity.show(this);
                break;

            case R.id.bt_play_effets://播放录音特效
                EffectActivity.show(this);
                break;
        }
    }

    private void onPermission() {
        if (ActivityCompat.checkSelfPermission(getApplication(), permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplication(), permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onRequestPermissionsResult: 申请成功");
        }
    }
}