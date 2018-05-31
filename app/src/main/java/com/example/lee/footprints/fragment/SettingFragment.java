package com.example.lee.footprints.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//껍데기

import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lee.footprints.R;
import com.example.lee.footprints.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {
    final int REQUEST_IMAGE=100;
    // 갤러리관련
    private Uri fileUri;
    private String filePath;

    ImageView imageView;
    private UploadProfile uploadProfile; // accept시 처리
    private Reset reset; // 서버에 있는 내정보 갖고옴
    TextView email;
    EditText nickname;
    EditText intro;
    Button acceptBtn;
    Button resetBtn;
    Button logoutBtn;
    Bitmap resetProfile;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.activity_setting, container, false);

        email = (TextView)layout.findViewById(R.id.email);
        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        nickname = (EditText)layout.findViewById(R.id.nickname);
        intro = (EditText)layout.findViewById(R.id.intro);

        acceptBtn = (Button)layout.findViewById(R.id.accept);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfile = null;
                uploadProfile = new UploadProfile(getActivity());
                uploadProfile.execute(filePath);
            }
        });

        resetBtn = (Button)layout.findViewById(R.id.reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset = null;
                reset = new Reset(getActivity());
                reset.execute();
            }
        });

        logoutBtn = (Button)layout.findViewById(R.id.logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("FIREBASELOGOUT",FirebaseAuth.getInstance().getCurrentUser().getEmail());
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("Login", false);
                startActivity(intent);
                getActivity().finish();
            }
        });

        imageView = (ImageView)layout.findViewById(R.id.profileimg);

        imageView.setImageResource(R.drawable.profile);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 갤러리 여는
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
        // reset을 한번 돌려서 서버에 있는 내정보 갖고와서 초기에 표시해준다
        reset = new Reset(getActivity());
        reset.execute();
        // Inflate the layout for this fragment
        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_IMAGE) {
            if(resultCode == Activity.RESULT_OK) {
                try {
                    fileUri = data.getData();
                    filePath = getPath(fileUri);
                    Bitmap image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());

                    // imageview에 맞춰서 가공
                    Bitmap resized = Bitmap.createScaledBitmap(image, imageView.getWidth(), imageView.getHeight(), true);

                    RoundedBitmapDrawable result = RoundedBitmapDrawableFactory.create(getResources(), resized);
                    result.setCircular(true);
                    imageView.setImageDrawable(result);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // 이미지 회전 함수
    /*
    public Bitmap rotateImage(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),src.getHeight(), matrix, true);
    }
    */

    public String getPath(Uri uri){
        if (uri == null){
            return null;
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        if (cursor != null){
            int column_idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_idx);
        }

        return uri.getPath();
    }

    // 프로필 정보를 update 하는 쓰레드 - > UploadProfile을 그대로 갖다 쓰면 계정도 추가할 수 있다!
    public class UploadProfile extends AsyncTask<String, Integer, Void> {

        public static final int DOWNLOAD_PROGRESS = 0;

        private final String url_Address = "http://footprints.gonetis.com:8080/moo/profileupdate";

        private ProgressDialog dialog;
        Context mContext;

        public UploadProfile(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(String... psth){
            HttpClient httpClient = null;

            try{
                httpClient = new DefaultHttpClient();
                httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

                HttpPost httpPost = new HttpPost(url_Address);
                httpPost.setHeader("Content-type", "multipart/form-data;boundary=-------------");

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setBoundary("-------------");
                builder.setCharset(Charset.forName("UTF-8"));
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                builder.addTextBody("username", URLEncoder.encode(nickname.getText().toString(),"UTF-8"));
                builder.addTextBody("intro", URLEncoder.encode(intro.getText().toString(),"UTF-8"));
                builder.addTextBody("user_account", email.getText().toString());

                try {
                    File file = new File(psth[0]);
                    builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
                } catch(Exception e) {}


                HttpEntity entity = builder.build();
                httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost);
            }catch (IOException e){
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
            try{
                Toast.makeText(getContext(), "저장 완료", Toast.LENGTH_SHORT).show();
            }catch (Exception e){

            }
        }
    }
    public class Reset extends AsyncTask<String, Integer, Void> {

        public static final int DOWNLOAD_PROGRESS = 0;

        private final String url_Address = "http://footprints.gonetis.com:8080/moo/usersearch";
        private final String url_Address2 = "http://footprints.gonetis.com:8080/moo/resources/profilePic/";

        private ProgressDialog dialog;
        Context mContext;
        String name;
        String intro0;
        String profilePic;
        Bitmap image;

        public Reset(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... psth){
            HttpClient httpClient = null;

            try{
                httpClient = new DefaultHttpClient();
                httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

                HttpPost httpPost = new HttpPost(url_Address);
                httpPost.setHeader("Content-type", "multipart/form-data;boundary=-------------");

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setBoundary("-------------");
                builder.setCharset(Charset.forName("UTF-8"));
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                builder.addTextBody("user_account", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                HttpEntity entity = builder.build();
                httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost);

                //서버에서 받은 response 값 저장
                String body = EntityUtils.toString(response.getEntity());
                JSONArray jsonArray = new JSONArray(body);
                StringBuffer sb = new StringBuffer();

                //데이터 뽑는 부분
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                name = jsonObject.getString("name");
                intro0 = jsonObject.getString("intro");
                profilePic = jsonObject.getString("pic_profile");
                try {
                    image = BitmapFactory.decodeStream((InputStream) new URL(url_Address2 + profilePic).getContent());
                } catch (Exception e) {
                    BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.profile);
                    image = drawable.getBitmap();
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
        }

        @Override
        protected void onPostExecute(Void result){
            // 서버에서 가져온 정보들 투입~
            nickname.setText(name);
            intro.setText(intro0);
            RoundedBitmapDrawable result2 = RoundedBitmapDrawableFactory.create(getResources(), image);
            resetProfile = image;
            result2.setCircular(true);
            imageView.setImageDrawable(result2);
        }
    }
}