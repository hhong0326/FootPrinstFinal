package com.example.lee.footprints;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.Iterator;

// 마커의 모양을 정하는 코드
public class MyClusterRenderer extends DefaultClusterRenderer<Picture> {

    private final IconGenerator mIconGenerator;
    private final ImageView mImageView;

    public MyClusterRenderer(Context context, GoogleMap map, ClusterManager<Picture> clusterManager) {
        super(context, map, clusterManager);
        mIconGenerator = new IconGenerator(context);
        mImageView = new ImageView(context);
        mImageView.setMaxHeight(200);
        mImageView.setMaxWidth(200);
        mImageView.setBackground(new ShapeDrawable(new OvalShape()));
        mImageView.setClipToOutline(true);
        mIconGenerator.setContentView(mImageView);

    }

    // 단일 마커
    @Override
    protected void onBeforeClusterItemRendered(Picture picture, MarkerOptions markerOptions) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = 4;

        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mImageView.setAdjustViewBounds(true);

        mImageView.setImageBitmap(picture.getImage());

        // 내사진이라면 그린테두리
        if(picture.getUseraccount().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
            mImageView.setBackgroundResource(R.drawable.image_border);
        }
        else {
            mImageView.setBackgroundResource(0);
            mImageView.setPadding(0,0,0,0);
        }
        Log.e("이메일ㄹㄹㄹㄹㄹㄹㄹㄹ", picture.getUseraccount()+"|"+FirebaseAuth.getInstance().getCurrentUser().getEmail());
        Bitmap icon = mIconGenerator.makeIcon();

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));


    }

    @Override
    protected void onClusterItemRendered(Picture picture, Marker marker) {
        super.onClusterItemRendered(picture, marker);
    }

    // 합쳐졌을 때
    @Override
    protected void onBeforeClusterRendered(Cluster<Picture> cluster, MarkerOptions markerOptions){

        Iterator<Picture> itr = cluster.getItems().iterator();

        Picture pic = itr.next();

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = 4;

        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mImageView.setAdjustViewBounds(true);
        mImageView.setBackgroundResource(0);
        mImageView.setPadding(0,0,0,0);
        mImageView.setImageBitmap(pic.getImage());

        Bitmap icon = mIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

}