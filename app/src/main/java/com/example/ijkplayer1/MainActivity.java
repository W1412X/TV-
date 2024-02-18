package com.example.ijkplayer1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private String video_url;
    private Button live_video_button,video_button;
    private ScrollView live_scrollview,video_scrollview,line_select_scrollview;
    private Button search_button;
    private Button enter_room_button;
    private Handler handler;
    private ProgressBar loading_view;
    private WebView help_web;
    private EditText text_input;
    private LinearLayout live_line_select_view;
    private CustomLinearLayout video_container;
    private LinearLayout live_container;
    private LinearLayout video_search_part;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler=new Handler();
        //初始化控件
        video_search_part=findViewById(R.id.main_view_linearlayout1);
        live_line_select_view=findViewById(R.id.main_view_live_line_select_button_container);
        line_select_scrollview=findViewById(R.id.main_view_dialog_select_line_scrollview);
        live_video_button=findViewById(R.id.main_view_live_video_button);
        video_button=findViewById(R.id.main_view_video_button);
        search_button=findViewById(R.id.main_view_search_button);
        enter_room_button=findViewById(R.id.main_view_enter_room_button);
        text_input=findViewById(R.id.main_view_input_text);
        loading_view=findViewById(R.id.main_view_loading_view);
        video_container=findViewById(R.id.main_view_video_item_container);
        live_container=findViewById(R.id.main_view_live_item_container);
        help_web=findViewById(R.id.main_view_help_web);
        live_scrollview=findViewById(R.id.main_view_live_scrollview);
        video_scrollview=findViewById(R.id.main_view_video_scrollview);
        help_web.setWebViewClient(new CustomWebViewClient());
        help_web.getSettings().setJavaScriptEnabled(true);
        help_web.getSettings().setDomStorageEnabled(true);
        help_web.getSettings().setUserAgentString("Mozilla/5.0");
        help_web.loadUrl("https://www.ccw5.cc/");
        init();
        init_live();
        //test
    }
    public void use_video(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                video_scrollview.setVisibility(View.VISIBLE);
                live_scrollview.setVisibility(View.GONE);
            }
        });
    }
    public void use_live(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                video_scrollview.setVisibility(View.GONE);
                live_scrollview.setVisibility(View.VISIBLE);
            }
        });
    }
    public void init(){
        //进入房间的逻辑
        enter_room_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("输入房间号");
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String code=input.getText().toString();
                        Thread thread=new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            loading_view.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    video_url=get_m3u8_url(code);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            loading_view.setVisibility(View.GONE);
                                            try{
                                                if(video_url.equals("error")){
                                                    Toast.makeText(MainActivity.this,"房间码错误",Toast.LENGTH_SHORT).show();
                                                }else if(video_url==null){
                                                    Toast.makeText(MainActivity.this,"未知错误",Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(MainActivity.this,"获取成功",Toast.LENGTH_SHORT).show();
                                                    Intent intent=new Intent(MainActivity.this,PlayerActivity.class);
                                                    intent.putExtra("url",video_url);
                                                    startActivity(intent);
                                                }
                                            }catch(Exception e){
                                                Toast.makeText(MainActivity.this,"error on handle",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }catch (Exception e){
                                    loading_view.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this,"ERROR",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        thread.start();
                    }
                });
                builder.show();
            }
        });
        //搜索按钮的逻辑
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search_content=text_input.getText().toString();
                search_in_web(search_content);
            }
        });
        //直播按钮的逻辑
        live_video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                use_live();
            }
        });
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                use_video();
            }
        });
    }
    public String get_m3u8_url(String code) throws IOException {
        String url="http://39.101.160.55/watch_html/"+code+".html";
        try{
            Document doc = Jsoup.connect(url).get();
            Pattern pattern = Pattern.compile("\"source\": \"(.*?)\"");
            Matcher matcher = pattern.matcher(doc.toString());
            if (matcher.find()) {
                String m3u8Url = matcher.group(1);
                return m3u8Url;
            } else {
                return "error";
            }
        }catch (Exception e){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });
        }
        return null;
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
    public void get_item(){
        try{
            video_container.removeAllViews();
            String code="e=document.getElementsByClassName('stui-vodlist__box');" +
                    "var result='';" +
                    "for (var i = 0; i < e.length; i++) {" +
                    "    var item = e[i];" +
                    "    var title=item.firstElementChild['title'];" +
                    "    result+=(title+'|INNERDIVIDE|');" +
                    "    var img_url=item.firstElementChild.dataset.original;" +
                    "  result+=(img_url+'|INNERDIVIDE|');" +
                    "    var status=item.outerText;" +
                    "    var index = status.indexOf('\\n');" +
                    "status = status.substring(0, index);" +
                    "  result+=status+'|INNERDIVIDE|';" +
                    "  result+=item.firstElementChild['href'];" +
                    "  if(i+1!=e.length){" +
                    "      result+='|OUTERDIVIDE|';" +
                    "    }" +
                    "}" +
                    "result;";
            help_web.evaluateJavascript(code, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.e("javascript",value);
                    // 解码 Unicode 编码的字符
                    String decodedValue = StringEscapeUtils.unescapeJava(value);
                    Log.e("javascript",decodedValue);
                    String[] items = decodedValue.toString().split("\\|OUTERDIVIDE\\|");
                    for(int i=0;i<items.length;i++){
                        final String [] details=items[i].split("\\|INNERDIVIDE\\|");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                VideoItem tmp;
                                try{
                                    tmp=new VideoItem(MainActivity.this,details[0],details[2],details[1],details[3]);
                                    video_container.addView(tmp);
                                }catch (Exception e){
                                }
                            }
                        });
                    }
                }
            });
        }catch (Exception e){
            Toast.makeText(MainActivity.this,"error",Toast.LENGTH_SHORT).show();
        }
    }
    public void add_item(){

    }
    public void search_in_web(String search_content){
        help_web.evaluateJavascript("search_bt=document.querySelector('.stui_header__user > li:nth-child(1) > a:nth-child(1)');" +
                "search_bt.click();" +
                "input=document.querySelector('#wd');" +
                "input['value']='"+search_content+
                "';inner_search_bt=document.querySelector('#searchbutton');" +
                "inner_search_bt.click();",null);
    }


    //CUSTOM WEBVIEW
    public class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            set_loading();
            try{
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        get_item();
                        set_loading_finish();
                    }
                },5000);
            }catch (Exception e){

            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 在这里可以对页面加载行为进行处理，如拦截特定的 URL，自定义页面跳转逻辑等
            // 返回 true 表示由应用程序处理该 URL，返回 false 表示由 WebView 加载该 URL
            return false;
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            set_loading_finish();
            get_item();
        }
    }
    public class LiveButton extends androidx.appcompat.widget.AppCompatButton{
        public LiveButton(Context context,String name,String url){
            super(context);
            this.setText(name);
            this.setTextColor(Color.parseColor("#ffffff"));
            this.setBackgroundColor(Color.parseColor("#cc6750a4"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            layoutParams.setMargins(5,5,5,5);
            this.setLayoutParams(layoutParams);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] set=url.split("\\|DIVIDE\\|");
                    live_line_select_view.removeAllViews();
                    line_select_scrollview.setVisibility(View.VISIBLE);
                    for(int i=1;i<=set.length;i++){
                        live_line_select_view.addView(new LineButton(MainActivity.this,"线路"+String.valueOf(i),set[i-1]));
                    }
                }
            });
        }
    }
    public class LineButton extends androidx.appcompat.widget.AppCompatButton{
        public LineButton(Context context,String name,String url){
            super(context);
            this.setText(name);
            this.setTextColor(Color.parseColor("#ffffff"));
            this.setBackgroundColor(Color.parseColor("#776750a4"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            layoutParams.setMargins(5,5,5,5);
            this.setLayoutParams(layoutParams);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    line_select_scrollview.setVisibility(View.GONE);
                    Intent intent=new Intent(MainActivity.this,PlayerActivity.class);
                    intent.putExtra("url",url);
                    intent.putExtra("live",true);
                    startActivity(intent);
                }
            });
        }
    }
    public void init_live(){
        live_container.addView(new LiveButton(MainActivity.this,"CCTV1综合", "http://tvpull.dxhmt.cn/tv/11081-2.m3u8|DIVIDE|http://221.213.43.82:8888/newlive/live/hls/2/live.m3u8|DIVIDE|http://222.81.86.42:8060/hls/ch3/ch3.m3u8|DIVIDE|http://ktwlplkf.f3322.org:9901/tsfile/live/0001_1.m3u8|DIVIDE|http://219.140.56.34:3333/tsfile/live/0001_1.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/1/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV2财经", "http://221.213.43.82:8888/newlive/live/hls/3/live.m3u8|DIVIDE|http://219.140.56.34:3333/tsfile/live/0002_1.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/2/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV3综艺", "http://223.95.111.98:5555/newlive/live/hls/3/live.m3u8|DIVIDE|http://219.140.56.34:3333/tsfile/live/0003_1.m3u8|DIVIDE|http://221.213.43.82:8888/newlive/live/hls/4/live.m3u8|DIVIDE|http://36.96.38.246:8888/newlive/live/hls/4/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV4中文国际", "http://221.213.43.82:8888/newlive/live/hls/5/live.m3u8|DIVIDE|http://219.140.56.34:3333/tsfile/live/1004_1.m3u8|DIVIDE|http://open.live-web.timetv.cn/live03/cctv4k.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV5 体育", "http://219.140.56.34:3333/tsfile/live/1005_1.m3u8|DIVIDE|http://221.213.43.82:8888/newlive/live/hls/6/live.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/5/live.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/5/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV5+ 体育赛事", "http://219.140.56.34:3333/tsfile/live/0016_1.m3u8|DIVIDE|http://221.213.43.82:8888/newlive/live/hls/7/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV6 电影", "http://219.140.56.34:3333/tsfile/live/1006_1.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/6/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV7 国防军事", "http://221.213.43.82:8888/newlive/live/hls/9/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV8 电视剧", "http://223.95.111.98:5555/newlive/live/hls/8/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV9 纪录", "http://221.213.43.82:8888/newlive/live/hls/11/live.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/9/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV10 科教", "http://223.95.111.98:5555/newlive/live/hls/10/live.m3u8|DIVIDE|http://221.213.43.82:8888/newlive/live/hls/12/live.m3u8|DIVIDE|http://123.183.24.86:6666/tsfile/live/0010_2.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV11 戏曲", "http://221.213.43.82:8888/newlive/live/hls/13/live.m3u8|DIVIDE|http://123.183.24.86:6666/tsfile/live/0011_1.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV12 社会与法", "http://223.95.111.98:5555/newlive/live/hls/12/live.m3u8|DIVIDE|http://221.213.43.82:8888/newlive/live/hls/14/live.m3u8|DIVIDE|http://123.183.24.86:6666/tsfile/live/0012_2.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV13 新闻", "http://221.213.43.82:8888/newlive/live/hls/15/live.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/13/live.m3u8|DIVIDE|http://anren.live/HK/BSJE.m3u8|DIVIDE|http://hls.hsrtv.cn/live/hsyouxian.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV14 少儿", "http://221.213.43.82:8888/newlive/live/hls/16/live.m3u8|DIVIDE|http://223.95.111.98:5555/newlive/live/hls/14/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CCTV15 音乐", "http://221.213.43.82:8888/newlive/live/hls/17/live.m3u8|DIVIDE|http://36.96.38.246:8888/newlive/live/hls/17/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"湖南卫视", "http://221.2.36.34:8888/newlive/live/hls/23/live.m3u8|DIVIDE|http://cfss.cc/api/ysp/hunws.m3u8|DIVIDE|http://36.96.38.246:8888/newlive/live/hls/23/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"东方卫视", "http://222.240.82.92:9901/tsfile/live/0107_1.m3u8|DIVIDE|http://36.96.38.246:8888/newlive/live/hls/20/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"延边卫视", "http://live.ybtvyun.com/video/s10006-44f040627ca1/index.m3u8|DIVIDE|http://live.ybtvyun.com/video/s10006-90fe76c52091/index.m3u8|DIVIDE|http://live.ybtvyun.com/video/s10006-44f040627ca1/index.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"凤凰中文", "http://playtv-live.ifeng.com/live/06OLEGEGM4G.m3u8|DIVIDE|https://playtv-live.ifeng.com/live/06OLEGEGM4G.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CGTN", "https://live.cgtn.com/1000/prog_index.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"CGTN纪录", "https://news.cgtn.com/resource/live/document/cgtn-doc.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"北京卫视", "http://36.96.38.246:8888/newlive/live/hls/19/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"天津卫视", "http://36.96.38.246:8888/newlive/live/hls/45/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"重庆卫视", "https://sjlivecdn9.cbg.cn/204912315959/app_2/_definst_/ls_2.stream/chunklist.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"河南卫视", "http://36.96.38.246:8888/newlive/live/hls/33/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"南方卫视", "http://111.207.22.10:8888/newlive/live/hls/53/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"荔枝台", "http://glive.grtn.cn/live/lizhi.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"浙江卫视", "http://123.183.24.86:6666/tsfile/live/0124_1.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"厦门卫视", "http://220.163.178.144:8888/tsfile/live/0115_1.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"云南卫视", "http://36.96.38.246:8888/newlive/live/hls/27/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"贵州卫视", "http://123.183.24.86:6666/tsfile/live/0120_1.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"四川卫视", "http://36.96.38.246:8888/newlive/live/hls/32/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"陕西卫视", "http://36.96.38.246:8888/newlive/live/hls/41/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"农林卫视", "http://36.96.38.246:8888/newlive/live/hls/52/live.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"青海卫视", "http://stream.qhbtv.com/qhws/playlist.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"香港卫视", "http://zhibo.hkstv.tv/livestream/mutfysrq/playlist.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"澳门卫视", "http://61.244.22.5/ch3/ch3.live/chunklist_w1228316132.m3u8"));
        live_container.addView(new LiveButton(MainActivity.this,"澳亚卫视", "https://live.mastvnet.com/lsdream/lY44pmm/2000/live.m3u8"));


    }
}