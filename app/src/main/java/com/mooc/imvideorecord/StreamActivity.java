package com.mooc.imvideorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SuTs
 * @create 2020/6/18 17:03
 * @Describe 字节流形式录制，AudioRecord
 */
public class StreamActivity extends BaseActivity {
    private static final String TAG = "StreamActivity";

    private TextView tv_result;
    private Button btRecord;
    private volatile boolean isRecording;
    private ExecutorService service;
    private Handler mainHandler;
    private File audioFile;
    private long startTime, stopTime;
    private byte[] buffer;
    private static final int BUFFER_SIZE = 2048;
    private FileOutputStream fos;
    private AudioRecord audioRecord;

    @Override
    void initView() {
        buffer = new byte[BUFFER_SIZE];
        service = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        tv_result = findViewById(R.id.tv_stream_result);
        btRecord = findViewById(R.id.bt_start_op);
        btRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    btRecord.setText("开始");
                    isRecording = false;
//                    service.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (!stopRecord()) {
//                                recordFail();
//                            }
//                        }
//                    });
                } else {
                    btRecord.setText("结束");
                    isRecording = true;
                    service.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (!startRecord()) {
                                recordFail();
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean stopRecord() {
        //停止录音
        try {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            fos.close();
            //记录停止时间
            stopTime = System.currentTimeMillis();
            //只接受超过3秒的录音，在UI上线上出来
            final int second = (int) ((stopTime - startTime) / 1000);
            if (second > 3) {
                //主线程提示
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_result.setText("录音成功" + second + "秒");
                    }
                });
            }
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void recordFail() {
        audioRecord = null;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamActivity.this, "录音失败", Toast.LENGTH_SHORT).show();
                isRecording = false;
                btRecord.setText("开始");
            }
        });
    }

    private boolean startRecord() {
        //创建录音文件
        try {
            audioFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    "/audio/", System.currentTimeMillis() + ".pcm");
            audioFile.getParentFile().mkdirs();
            audioFile.createNewFile();

            //创建文件输出流
            fos = new FileOutputStream(audioFile);
            //配置AudioRecord
            int audioSource = MediaRecorder.AudioSource.MIC;
            int sampleRate = 44100;
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;//单声道
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;//所有安卓系统都支持
//计算AudioRecord内部buffer最小大小
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            audioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, Math.max(minBufferSize, BUFFER_SIZE));

            //开始录音
            audioRecord.startRecording();
            //记录时间
            startTime = System.currentTimeMillis();
            //循环读取数据，写到输出流中
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (read > 0) {//读取到文件中
                    fos.write(buffer, 0, read);
                } else {
                    return false;
                }
            }
            //退出循环，停止录音，释放资源
            return stopRecord();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (audioRecord != null) {
                audioRecord.release();
            }
        }
    }

    @Override
    int initLayout() {
        return R.layout.activity_stream;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdownNow();
    }

    public static void show(Context context) {
        context.startActivity(new Intent(context, StreamActivity.class));
    }
}