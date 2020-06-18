package com.mooc.imvideorecord;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SuTs
 * @create 2020/6/18 17:02
 * @Describe 文件形式录制，MediaReocrder
 */
public class FileActivity extends BaseActivity {
    private static final String TAG = "FileActivity";
    private TextView tvResult;
    private TextView tv_record;
    private ExecutorService service;
    private MediaRecorder mediaRecorder;
    private File audilFile;
    private long startTime;
    private long stopTime;
    private Handler mainHandler;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    void initView() {
        service = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        tvResult = findViewById(R.id.tv_file_result);
        tv_record = findViewById(R.id.tv_file_record);
        tv_record.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecord();
                        break;

                    case MotionEvent.ACTION_UP:
                        stopRecord();
                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecord() {
        tv_record.setText("正在说话");
        tv_record.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        service.submit(new Runnable() {
            @Override
            public void run() {
                releaseRecorder();
                if (!doStart()) {
                    recordFail();
                }
            }
        });
    }

    private void stopRecord() {
        tv_record.setText("按住说话");
        tv_record.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        service.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStop()) {
                    recordFail();
                }
                releaseRecorder();
            }
        });
    }

    //停止录音
    private boolean doStop() {
        //停止录音
        try {
            mediaRecorder.stop();
            //记录停止时间
            stopTime = System.currentTimeMillis();
            //只接受超过3秒的录音，在UI上线上出来
            final int second = (int) ((stopTime - startTime) / 1000);
            if (second > 3) {
                //主线程提示
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText("录音成功" + second + "秒");
                    }
                });
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return true;
    }

    //启动录音
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean doStart() {
        //创建MediaRecorder
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioSamplingRate(44100);//所有安卓系统都支持的才样频率
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioSamplingRate(96000);//音质比较好的频率

        //创建录音文件
        try {
            audilFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    "/audio/", System.currentTimeMillis() + ".m4a");
            audilFile.getParentFile().mkdirs();
            audilFile.createNewFile();
            mediaRecorder.setOutputFile(audilFile.getAbsoluteFile());
            //开始录音
            mediaRecorder.prepare();
            mediaRecorder.start();
            //记录录音时长
            startTime = System.currentTimeMillis();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //录音错误
    private void recordFail() {
        mediaRecorder = null;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FileActivity.this, "录音失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //释放MediaRecorder
    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdownNow();
        releaseRecorder();
    }

    @Override
    int initLayout() {
        return R.layout.activity_file;
    }

    public static void show(Context context) {
        context.startActivity(new Intent(context, FileActivity.class));
    }


}