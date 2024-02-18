package com.example.ijkplayer1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomLinearLayout extends LinearLayout {
    private int max_row=5;
    private LinearLayout row_linear_layout;
    private LayoutParams layoutParams;
    public CustomLinearLayout(Context context) {
        super(context);
        row_linear_layout=new LinearLayout(context);
        setOrientation(LinearLayout.VERTICAL);
        layoutParams=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        row_linear_layout.setLayoutParams(layoutParams);
        row_linear_layout.setOrientation(HORIZONTAL);
        this.addView(row_linear_layout);
    }
    public void setMax_row(int max_row){
        this.max_row=max_row;
    }
    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        row_linear_layout=new LinearLayout(context);
        setOrientation(LinearLayout.VERTICAL);
        layoutParams=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        row_linear_layout.setLayoutParams(layoutParams);
        row_linear_layout.setOrientation(HORIZONTAL);
        this.addView(row_linear_layout);
    }

    public void addView(VideoItem view) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

// 设置控件的布局参数
        view.setLayoutParams(layoutParams);
        if(row_linear_layout.getChildCount()>max_row){
            row_linear_layout=new LinearLayout(getContext());
            row_linear_layout.setOrientation(HORIZONTAL);
            row_linear_layout.setLayoutParams(layoutParams);
            this.addView(row_linear_layout);
            row_linear_layout.addView(view);
        }else{
            row_linear_layout.addView(view);
        }
    }
    public void addView(VideoPageActivity.EpisodeButton view) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        layoutParams.setMargins(5,5,5,5);
// 设置控件的布局参数
        view.setLayoutParams(layoutParams);
        if(row_linear_layout.getChildCount()>max_row){
            row_linear_layout=new LinearLayout(getContext());
            row_linear_layout.setOrientation(HORIZONTAL);
            row_linear_layout.setLayoutParams(layoutParams);
            this.addView(row_linear_layout);
            row_linear_layout.addView(view);
        }else{
            row_linear_layout.addView(view);
        }
    }

}

