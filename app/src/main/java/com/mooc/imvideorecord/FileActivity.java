package com.mooc.imvideorecord;

import androidx.annotation.RequiresApi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
    private File audioFile;
    private long startTime;
    private long stopTime;
    private Handler mainHandler;
    private volatile boolean isPlaying;
    private MediaPlayer mediaPlayer;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    void initView() {
        service = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        tvResult = findViewById(R.id.tv_file_result);
        tv_record = findViewById(R.id.tv_file_record);
        findViewById(R.id.bt_file_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//检查当前状态
                if (audioFile != null && !isPlaying) {
                    isPlaying = true;
                    service.submit(new Runnable() {
                        @Override
                        public void run() {
                            doPlay(audioFile);
                        }
                    });
                }
            }
        });
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

    /**
     * 播放
     *
     * @param audioFile
     */
    private void doPlay(File audioFile) {
        //配置MediaPalyer
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //播放结束
                    stopPlay();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    //提示用户
                    playFail();
                    //释放播放器
                    stopPlay();
                    return true;
                }
            });
            //配置音量，是否循环
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setLooping(false);
            //准备开始
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            playFail();
            stopPlay();
        }

        //设置监听回调

        //异常处理
    }

    /**
     * 播放出错
     */
    private void playFail() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FileActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
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
            audioFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    "/audio/", System.currentTimeMillis() + ".m4a");
            audioFile.getParentFile().mkdirs();
            audioFile.createNewFile();
            mediaRecorder.setOutputFile(audioFile.getAbsoluteFile());
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
        stopPlay();
    }

    /**
     * 停止播放
     */
    private void stopPlay() {
        isPlaying = false;
        if (mediaPlayer != null) {
            //重置监听器
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnErrorListener(null);

            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    int initLayout() {
        return R.layout.activity_file;
    }

    public static void show(Context context) {
        context.startActivity(new Intent(context, FileActivity.class));
    }


}