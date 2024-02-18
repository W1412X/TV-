package com.example.ijkplayer1;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private String video_url;
    private Handler handler;
    private String total_time="null";
    private String now_time="null";
    private TextView time_view;
    private IjkMediaPlayer player; // 声明为成员变量，以便在整个Activity中可访问
    private Button next_button,pause_button;
    private Button tool_bar_button;
    private LinearLayout tool_bar;
    private SeekBar seekBar;
    private Handler time_handler;
    private boolean if_live;
    public static String convertMicrosecondsToTime(long microseconds) {
        long seconds = (microseconds/1000);
        long minutes = (seconds/60)%60;
        long hours = (seconds/3600);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds%60);
        }
    }
    private void update_progress(){
        time_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(player!=null){
                    try {
                        long t=player.getDuration();
                        total_time=convertMicrosecondsToTime(t);
                        long t_n=player.getCurrentPosition();
                        now_time=convertMicrosecondsToTime(t_n);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(Math.round(100 * (Float.valueOf(t_n) / Float.valueOf(t))));
                                time_view.setText(now_time+"/"+total_time);
                            }
                        });
                        update_progress();
                    }catch (Exception e){

                    }
                }
            }
        },1000);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        time_handler=new Handler();
        time_view=findViewById(R.id.player_time_text);
        handler = new Handler();
        Intent intent = getIntent();
        surfaceView = findViewById(R.id.surface_view);
        video_url = intent.getStringExtra("url");
        if_live=intent.getBooleanExtra("live",false);
        pause_button=findViewById(R.id.player_pause_button);
        next_button=findViewById(R.id.player_next_button);
        tool_bar=findViewById(R.id.player_tool_view);
        tool_bar_button=findViewById(R.id.player_tool_bar_button);
        seekBar=findViewById(R.id.player_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress()*player.getDuration()/100);
                player.start();
            }
        });
        tool_bar_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tool_bar.getVisibility()==View.VISIBLE){
                    tool_bar.setVisibility(View.GONE);
                }else{
                    tool_bar.setVisibility(View.VISIBLE);
                }
            }
        });

        pause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pause_button.getText().equals("〓")||player.isPlaying()){
                    pause_button.setText("▲");
                    player.pause();
                }else if(player.isPlayable()){
                    player.start();
                    pause_button.setText("〓");
                }
            }
        });
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PlayerActivity.this,PlayerActivity.class);
                String new_video_url=incrementM3u8Url(video_url);
                intent.putExtra("url",new_video_url);
                startActivity(intent);
            }
        });
        try {
            player = new IjkMediaPlayer(); // 初始化IjkMediaPlayer对象
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    player.setDisplay(holder);
                    player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder=new AlertDialog.Builder(PlayerActivity.this);
                                    builder.setTitle("播放错误");
                                    builder.setMessage("播放资源出错，请更换播放源");
                                    builder.setPositiveButton("OK",null);
                                    builder.show();
                                }
                            });
                            return false;
                        }
                    });
                    player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(IMediaPlayer iMediaPlayer) {
                            Toast.makeText(PlayerActivity.this,"OK",Toast.LENGTH_SHORT).show();
                        }
                    });
                    player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(IMediaPlayer iMediaPlayer) {
                            if(if_live){
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Dialog dialog=new Dialog(PlayerActivity.this);
                                    dialog.setTitle("播放完毕");
                                    dialog.show();
                                }
                            });
                        }
                    });
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                }
            });
            try {

                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"framedrop",5);
                player.setDataSource(video_url);
                player.prepareAsync();
            } catch (IOException e) {
                Log.e("error", e.getMessage().toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Dialog dialog = new Dialog(PlayerActivity.this);
                        dialog.setTitle("资源解析错误");
                        dialog.show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e("e", e.getMessage().toString());
        }
        update_progress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop(); // 停止播放
            player.release(); // 释放资源
        }
    }
    public String incrementM3u8Url(String m3u8Url) {
        try{
            m3u8Url= URLDecoder.decode(m3u8Url, "UTF-8");
        }catch (Exception e){

        }
        // Split the URL by slashes to get the individual directories
        String[] directories = m3u8Url.split("/");

        // Get the last directory
        String lastDirectory = directories[directories.length - 2];
        // Use a regular expression to extract the number from the directory
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lastDirectory);
        matcher.find();
        String numberString = matcher.group();

        // Increment the number and replace it in the directory
        String newDirectory = "第"+addOneToNumberString(numberString)+"集";

        // Build the new URL with the updated directory
        directories[directories.length - 2] = newDirectory;
        String newUrl = String.join("/", directories);

        return newUrl;
    }
    public String addOneToNumberString(String numStr) {
        // 去除数字字符串前面的0，保留有效部分
        int startIndex = 0;
        for (int i = 0; i < numStr.length(); i++) {
            if (numStr.charAt(i) != '0') {
                startIndex = i;
                break;
            }
        }
        String validNumStr = numStr.substring(startIndex);

        // 将有效部分转换为整数，并加1
        int num = Integer.parseInt(validNumStr) + 1;

        // 格式化输出，保留前导0
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numStr.length() - String.valueOf(num).length(); i++) {
            result.append("0");
        }
        result.append(num);

        return result.toString();
    }
    public void init_player(){
        try {
            player = new IjkMediaPlayer(); // 初始化IjkMediaPlayer对象
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    player.setDisplay(holder);
                    player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder=new AlertDialog.Builder(PlayerActivity.this);
                                    builder.setTitle("播放错误");
                                    builder.setMessage("播放资源出错，请更换播放源");
                                    builder.setPositiveButton("OK",null);
                                    builder.show();
                                }
                            });
                            return false;
                        }
                    });
                    player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(IMediaPlayer iMediaPlayer) {
                            Toast.makeText(PlayerActivity.this,"OK",Toast.LENGTH_SHORT).show();
                        }
                    });
                    player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(IMediaPlayer iMediaPlayer) {
                            if(if_live){
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Dialog dialog=new Dialog(PlayerActivity.this);
                                    dialog.setTitle("播放完毕");
                                    dialog.show();
                                }
                            });
                        }
                    });
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                }
            });
            try {
                int bps = 2000; // 比特率，单位为 kbps

// 创建 MediaFormat 对象，并设置相关参数
                MediaFormat format = MediaFormat.createVideoFormat("video/avc", 600, 400);
                format.setInteger(MediaFormat.KEY_BIT_RATE, bps * 1024);
// 创建 MediaCodec 对象，并进行配置
                MediaCodec mediaCodec = MediaCodec.createEncoderByType("video/avc");
                mediaCodec.configure(format, surfaceView.getHolder().getSurface(),null,0);
// 获取设置比特率之后的参数
                MediaFormat outputFormat = mediaCodec.getOutputFormat();
                int outputBitrate = outputFormat.getInteger(MediaFormat.KEY_BIT_RATE);

                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"framedrop",5);
                player.setDataSource(video_url);
                player.prepareAsync();
            } catch (IOException e) {
                Log.e("error", e.getMessage().toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Dialog dialog = new Dialog(PlayerActivity.this);
                        dialog.setTitle("资源解析错误");
                        dialog.show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e("e", e.getMessage().toString());
        }
    }
}
