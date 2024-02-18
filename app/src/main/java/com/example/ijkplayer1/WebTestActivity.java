package com.example.ijkplayer1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class WebTestActivity extends AppCompatActivity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_test);
        webView=findViewById(R.id.web_test_view_web);
        webView.loadUrl("https://www.ccyy6.cc/hanguoju/xiaoxinyageziyingsheng/");
    }
}