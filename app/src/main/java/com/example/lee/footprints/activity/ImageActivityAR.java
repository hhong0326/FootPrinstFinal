package com.example.lee.footprints.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.lee.footprints.R;
import com.example.lee.footprints.fragment.TimelineFragment;

public class ImageActivityAR extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 적당한 틀 가져옴...
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams= new WindowManager.LayoutParams();
        layoutParams.flags= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount= 0.7f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.activity_image_ar);

        Intent intent = getIntent();
        String picNum = intent.getStringExtra("PIC_NUM"); // 구글맵 아래에 스크롤바에서 몇번째 요소를 눌렀는지 알수있는 부분
        Log.e("PICNUM",picNum);
        Log.e("홍숑",picNum);
        ViewPager viewPager = (ViewPager) findViewById(R.id.image_view_pagerAR);
        ImageAdapterAR adapterAR = new ImageAdapterAR(this, TimelineFragment.picBox);
        viewPager.setAdapter(adapterAR);
        viewPager.setCurrentItem(0); // ex) 첫번째 썸네일을 눌렀으면 바로 첫번째 요소가 뜬다
    }

}