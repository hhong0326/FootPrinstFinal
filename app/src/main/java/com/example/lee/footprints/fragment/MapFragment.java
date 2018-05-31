package com.example.lee.footprints.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.lee.footprints.MyClusterRenderer;
import com.example.lee.footprints.Picture;
import com.example.lee.footprints.R;
import com.example.lee.footprints.activity.AddActivity;
import com.example.lee.footprints.activity.FindActivity;
import com.example.lee.footprints.activity.FindPicActivity;
import com.example.lee.footprints.activity.ImageActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.places.Places;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "googlemap_example";
    private MapView mapView = null;
    private GoogleApiClient googleApiClient = null;
    private View layout;
    private LinearLayout linear; // 맵 아래 스크롤바의 레이아웃
    private GoogleMap mMap;
    private ClusterManager<Picture> mClusterManager;
    public static boolean flag = true;
    private RequestJson requestJson;

    private SetImageView setImageView;

    public static ArrayList<Picture> allPic = new ArrayList<Picture>(); // 현재 맵에 올라와있는 마커의 list -> 처음엔 전체를 가져오니 이걸 ARFragment에서 활용하면 될거같다

    private double currentLat;
    private double currentLng;

    private com.example.lee.footprints.Location currentLocation = null;
    private MyClusterRenderer myClusterRenderer;
    public static Vector<Picture> picCollection = new Vector<Picture>(); // 현재 맵 아래 스크롤에 올라와있는 것들의 list
    private int tag = -1; // 이걸로 스크롤 사진들의 순서를 매긴다

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
        {
            //화면에 실제로 보일때
        }
        else
        {
            //preload 될때(전페이지에 있을때)
            layout = null;
            requestJson = null;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.activity_map, null);

        currentLocation = currentLocation.getInstance();
        currentLat = currentLocation.getLocation().latitude;
        currentLng = currentLocation.getLocation().longitude;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            linear = (LinearLayout) layout.findViewById(R.id.linear);
        }

        mapView = (MapView) layout.findViewById(R.id.map);
        mapView.getMapAsync(this);

        EditText editText = (EditText)layout.findViewById(R.id.find_edit);

        editText.setCursorVisible(false);
        editText.setShowSoftInputOnFocus(false);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ASD", "검색 ㄱㄱㄱㄱ");
                Intent intent = new Intent(getContext(), FindActivity.class);
                startActivity(intent);
            }
        });

        ImageView imageView = (ImageView)layout.findViewById(R.id.board_comment);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ADD","추가");
                Intent intent = new Intent(getContext(), AddActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        if ( googleApiClient != null && googleApiClient.isConnected())
            googleApiClient.disconnect();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // 초기실행시, AddActivity에서 뒤로가기시, 검색창에서 아무것도 하지않고 뒤로가기시 flag는 true다 : 모든 사진 가져온다
        if(flag == true) {
            requestJson = null;
            requestJson = new RequestJson(getActivity());

            //백그라운드 작업 실행
            requestJson.execute();

            flag = false;
        }
        if ( googleApiClient != null)
            googleApiClient.connect();

        // 모든사진을 가져오든 뭘하든 마커를 재설정해야한다
        if(mClusterManager!=null) {
            mClusterManager.clearItems();
            for (int i = 0; i < allPic.size(); i++) {
                addItem(allPic.get(i));
            }

            mClusterManager.cluster();
        }
        Log.e("FLAG!!!!!!!!!!!!",Boolean.toString(flag));
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if ( googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
        if ( googleApiClient != null ) {
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);

            if ( googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될 때 실행되는 함수
        MapsInitializer.initialize(getActivity().getApplicationContext());

        if(mapView != null)
        {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //나침반이 나타나도록 설정
        mMap.getUiSettings().setCompassEnabled(true);
        // 매끄럽게 이동함
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat,currentLng),16));
        // 클러스터 매니저 생성
        mClusterManager = new ClusterManager<Picture>(getActivity(),mMap);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        myClusterRenderer = new MyClusterRenderer(getActivity(), mMap, mClusterManager);
        myClusterRenderer.setMinClusterSize(1);
        mClusterManager.setRenderer(myClusterRenderer);

        //한 개의 마커 누를 때!!
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Picture>() {
            @Override
            public boolean onClusterItemClick(Picture picture) {
                cleanImageView();

                tag++; // tag를 0으로 만든다
                picCollection.add(picture);
                setImageView = new SetImageView(picture.getImage());
                setImageView.execute();

                return false;
            }
        });

        //합쳐진(클러스터링된) 마커 누를 때!!
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Picture>() {
            @Override
            public boolean onClusterClick(Cluster<Picture> cluster) {
                cleanImageView();
                Iterator<Picture> itr = cluster.getItems().iterator();
                while(itr.hasNext()) {

                    tag++;
                    Picture pic = itr.next();
                    picCollection.add(pic);
                    setImageView = new SetImageView(pic.getImage());
                    setImageView.execute();
                }
                return false;
            }
        });
        if ( googleApiClient == null) {
            buildGoogleApiClient();
        }
        if ( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }

    }

    // 마커 add
    private void addItem(Picture picture) {
        mClusterManager.addItem(picture);
    }

    // 스크롤 이미지들을 비운다
    public void cleanImageView() {
        linear.removeAllViews();
        setImageView = null;
        picCollection.clear();

        tag = -1;
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int cause) {
        if ( cause ==  CAUSE_NETWORK_LOST )
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED )
            Log.e(TAG,"onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class RequestJson extends AsyncTask<Void, Integer, Void> {

        public static final int DOWNLOAD_PROGRESS = 0;

        private final String url_Address = "http://footprints.gonetis.com:8080/moo/jsonrequest";
        String thumb_url_Address = "http://footprints.gonetis.com:8080/moo/resources/thumbnails/";
        private ProgressDialog dialog;
        Context mContext;

        public RequestJson(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("로딩중입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            if(mClusterManager!=null) {
                mClusterManager.clearItems();
                allPic.clear();
            }
        }

        @Override
        protected Void doInBackground(Void... params){
            HttpClient httpClient = null;

            try{

                httpClient = new DefaultHttpClient();
                httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

                HttpPost httpPost = new HttpPost(url_Address);

                HttpResponse response = httpClient.execute(httpPost);

                //서버에서 받은 response 값 저장
                String body = EntityUtils.toString(response.getEntity());
                JSONArray jsonArray = new JSONArray(body);
                StringBuffer sb = new StringBuffer();

                //데이터 뽑는 부분
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // allPic에 모든 사진을 더한다
                    allPic.add(new Picture(jsonObject.getDouble("latitude"),jsonObject.getDouble("longitude"),
                            BitmapFactory.decodeStream((InputStream) new URL(thumb_url_Address + jsonObject.getString("thumbPicName")).getContent()),
                            jsonObject.getString("fileName"),jsonObject.getString("user_account"),jsonObject.getString("username"),jsonObject.getString("tags")));

                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(httpClient != null){
                    httpClient.getConnectionManager().shutdown();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress){
            super.onProgressUpdate(progress);
            dialog.setProgress((int)progress[0]);
        }

        @Override
        protected void onPostExecute(Void result){

            // 모든 사진을 가져오는게 끝나면 마커 더한다
            for (int i = 0; i < allPic.size(); i++) {
                addItem(allPic.get(i));
            }
            Log.e("addItem", "gogogogogo");
            mClusterManager.cluster();
            dialog.dismiss();

        }

    }

    // 마커를 눌렀을때 스크롤에 띄우는 쓰레드
    public class SetImageView extends AsyncTask<Void, Integer, Void> {

        Bitmap mImage;
        ImageView imageView;

        public SetImageView(Bitmap image) {
            mImage = image;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView = new ImageView(getActivity());
            imageView.setTag(tag); // 스크롤 이미지들에 순서를 매긴다
        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Void result) {
            imageView.setPadding(5,0,0,0);
            linear.addView(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);

            imageView.setImageBitmap(mImage);

            Log.e("IMAGES", "IMAGEVIEWS ADDED");
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ImageActivity.class);
                        intent.putExtra("PIC_NUM",(int)view.getTag()); // 자신이 누른 index를 보냄
                        startActivity(intent);
                    }
                });

        }
    }

}
