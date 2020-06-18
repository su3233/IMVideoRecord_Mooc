package com.mooc.imvideorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author SuTs
 * @create 2020/6/18 17:03
 * @Describe 录制可视化
 */
public class VisualizerActivity extends BaseActivity {

    @Override
    void initView() {

    }

    @Override
    int initLayout() {
        return R.layout.activity_visualizer;
    }

    public static void show(Context context) {
        context.startActivity(new Intent(context, VisualizerActivity.class));
    }
}