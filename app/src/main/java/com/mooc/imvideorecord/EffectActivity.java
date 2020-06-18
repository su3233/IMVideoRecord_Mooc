package com.mooc.imvideorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
/**
 * @author SuTs
 * @create 2020/6/18 17:01
 * @Describe    播放特效
 */
public class EffectActivity extends BaseActivity {

    @Override
    void initView() {

    }

    @Override
    int initLayout() {
        return R.layout.activity_effect;
    }

    public static void show(Context context) {
        context.startActivity(new Intent(context, EffectActivity.class));
    }
}