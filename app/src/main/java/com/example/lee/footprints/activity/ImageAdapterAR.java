package com.example.lee.footprints.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lee.footprints.Picture;
import com.example.lee.footprints.R;

import java.util.Vector;

public class ImageAdapterAR extends PagerAdapter {
    Context context;
    Vector<Picture> picBox;

    String url_Address = "http://footprints.gonetis.com:8080/moo/resources/";

    ImageAdapterAR(Context context, Vector<Picture> picCollection){
        this.context=context;
        this.picBox = picCollection;

    }
    @Override
    public int getCount() {
        return picBox.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // 대강 weight를 넣어서 레이아웃 조절한 부분..

        ImageView imageView = new ImageView(context);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 5f));

        TextView nickname = new TextView(context);
        nickname.setText(picBox.get(position).getUsername());
        nickname.setTextColor(Color.WHITE);
        nickname.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView email = new TextView(context);
        email.setText(picBox.get(position).getUseraccount());
        email.setTextColor(Color.WHITE);
        email.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tags = new TextView(context);
        tags.setText(picBox.get(position).getTags());
        tags.setTextColor(Color.WHITE);
        tags.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Log.e("????????", picBox.get(position).getUsername()+picBox.get(position).getUseraccount()+picBox.get(position).getTags());

        Log.e("POSITION", Integer.toString(position));

        Glide.with(context)
                .load(url_Address + picBox.get(position).getURL()).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.loading) // 로딩중 어떤 화면을 띄울 것인가
                .error(R.drawable.loading_error).fitCenter() // 불러오지 못할시 어떤 화면을 띄울 것인가
                .into(imageView); // 어느 뷰에 넣을 것인가

        Log.e("GLIDE",Double.toString(imageView.getWidth())+","+Double.toString(imageView.getHeight()));


        Log.e("URL",url_Address + picBox.get(position).getURL());

        layout.addView(imageView);
        layout.addView(nickname);
        layout.addView(email);
        layout.addView(tags);

        ((ViewPager) container).addView(layout);

        return layout;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((LinearLayout) object);
    }

}