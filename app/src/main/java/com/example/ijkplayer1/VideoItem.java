package com.example.ijkplayer1;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VideoItem extends LinearLayout{
    private String title;
    private ImageView imageView;
    private TextView titletextView;
    private TextView statustextView;
    private String status;
    private String img_url;
    private WebView help_web;
    private String web_url;
    public VideoItem(Context context, String name, String status, String img_url, String page_url){
        super(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.iten, this, true);
        imageView = view.findViewById(R.id.item_image_view);
        titletextView = view.findViewById(R.id.item_title);
        statustextView=view.findViewById(R.id.item_status);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //执行对应的操作，包括加载网
                Intent intent=new Intent(context, VideoPageActivity.class);
                intent.putExtra("url", VideoItem.this.web_url);
                intent.putExtra("title",VideoItem.this.title);
                intent.putExtra("img_url",VideoItem.this.img_url);
                intent.putExtra("status",VideoItem.this.status);
                context.startActivity(intent);
            }
        });
        int i=0;
        this.title=name;
        this.status=status;
        this.img_url=img_url;
        this.help_web=help_web;
        this.web_url=page_url;
        titletextView.setText(this.title);
        statustextView.setText(this.status);
        Picasso.get().load(img_url).into(imageView);
    }
}
