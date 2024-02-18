package com.example.ijkplayer1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoPageActivity extends AppCompatActivity {

    private String video_url;
    private  String pinyin;
    private String message;
    private String img_url;
    private String web_url;
    private String status;
    private String title;
    private LinearLayout video_container;
    private VideoItem videoItem;
    private TextView title_text_view;
    private TextView message_text_view;
    private Handler handler;
    private ProgressBar loading_view;
    private CustomLinearLayout episode_container;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_page);
        //初始化
        Intent intent=getIntent();
        web_url=intent.getStringExtra("url");
        img_url=intent.getStringExtra("img_url");
        status=intent.getStringExtra("status");
        title=intent.getStringExtra("title");
        video_container=findViewById(R.id.video_page_video_item_container);
        videoItem=new VideoItem(VideoPageActivity.this,title,status,img_url,web_url);
        videoItem.setClickable(false);
        video_container.addView(videoItem);
        title_text_view=findViewById(R.id.video_page_title_text_view);
        message_text_view=findViewById(R.id.video_page_msg_text_view);
        loading_view=findViewById(R.id.video_view_loading_view);
        title_text_view.setText(title);
        handler=new Handler();
        episode_container=findViewById(R.id.video_page_episode_button_list);
        episode_container.setMax_row(6);
        String[] parts = web_url.split("/");
        // 获取最后一个目录字符串
        pinyin = parts[parts.length - 1];
        //如果含有数字就很沙比
        if(title.matches(".*\\d+.*")){
            Toast.makeText(VideoPageActivity.this,"请使用手机App创建房间在电视上点播",Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        update_ui();
    }
    public class EpisodeButton extends androidx.appcompat.widget.AppCompatButton{
        private String m3u8_url;
        public EpisodeButton(Context context,String url,String text){
            super(context,null);
            this.m3u8_url=url;
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context,PlayerActivity.class);
                    intent.putExtra("url",m3u8_url);
                    startActivity(intent);
                }
            });
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            setText(text);
            setBackground(getResources().getDrawable(R.drawable.shape_episode_button));
        }
    }
    public void set_loading(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                loading_view.setVisibility(View.VISIBLE);
            }
        });
    }
    public void set_loading_finish(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                loading_view.setVisibility(View.GONE);
            }
        });

    }
    public void update_ui(){//使用javascript
        set_loading();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Document doc = Jsoup.connect(web_url)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                            .maxBodySize(0)
                            .timeout(600000)
                            .get()
                    ;
                    String liText="";
                    try{
                        Elements h3Elements = doc.select(".stui-vodlist__head h3");
                        for (Element h3 : h3Elements) {
                            String h3Text = h3.text(); // 获取h3标签的文本内容
                            if (h3Text.contains("暴云")) {
                                // 获取暴云相关的所有li标签的文本内容
                                Elements relatedLiElements = h3.nextElementSibling().select("li");
                                for (Element li : relatedLiElements) {
                                    Pattern pattern = Pattern.compile("\\d+");  // 匹配数字的正则表达式模式
                                    Matcher matcher = pattern.matcher(pinyin);
                                    pinyin = matcher.replaceAll("");
                                    String url="https://s5.bfbfvip.com/video/"+pinyin+"/"+li.text()+"/index.m3u8";
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e("WWWWWW","in addd");
                                            episode_container.addView(new EpisodeButton(VideoPageActivity.this,url,li.text()));
                                        }
                                    });
                                    Log.e("url",url);
                                }
                            }
                        }
                        Elements texts=doc.getElementsByClass("stui-content__detail");
                        String text=texts.text();
                        String[] fields = {"主演：", "导演：", "状态：", "地区：", "剧情：", "类型：", "清晰度："};
                        StringBuilder modifiedString = new StringBuilder(text);

                        for (String field : fields) {
                            int index = modifiedString.indexOf(field);
                            if (index != -1) {
                                modifiedString.insert(index, "\n");
                            }
                        }
                        message=modifiedString.toString();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                message_text_view.setText(message);
                            }
                        });
                        Log.e("eee",message);
                    }catch (Exception e){
                        Toast.makeText(VideoPageActivity.this,"资源错误",Toast.LENGTH_SHORT).show();
                    }
                    Log.e("tt",liText);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            set_loading_finish();
                        }
                    });
                    // 使用 |DIVIDE| 连接所有文本内容，并输出字符串
                    String outputString ="hhh";
                    Log.e("txt",outputString);
                }catch (IOException e){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoPageActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            set_loading_finish();
                            Toast.makeText(VideoPageActivity.this,"获取信息失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        thread.start();
    }
}