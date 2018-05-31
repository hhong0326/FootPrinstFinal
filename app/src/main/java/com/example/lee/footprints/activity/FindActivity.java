package com.example.lee.footprints.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lee.footprints.R;
import com.example.lee.footprints.adapter.ListAdapter;
import com.example.lee.footprints.fragment.MapFragment;
import com.example.lee.footprints.model.Tag;

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
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class FindActivity extends AppCompatActivity {

    private EditText editText;
    private ListView listView;
    private TextView textView;
    private LinearLayout layout;
    private ArrayList<Tag> pictures; //Tag란 리스트뷰에 들어갈 요소(검색된 태그와 해당 태그 사진개수 저장)
    private ListAdapter adapter;
    private String text;
    private String[] tagArray; // 검색된 태그
    private int[] tagcountArray; // 해당 태그 사진개수
    boolean flag = true; // 검색화면에서 그냥 나갔을땐 true(아무변화없음:모든사진불러오기) 검색된 항목을 눌러서 나갔을땐 false(모든사진불러오기 안함)

    FindPic findPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        editText = (EditText)findViewById(R.id.find_edit);
        textView = (TextView)findViewById(R.id.find_text);
        listView = (ListView)findViewById(R.id.find_list);
        layout = (LinearLayout)findViewById(R.id.find_boss);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                text = s.toString();
                tagArray = null;
                tagcountArray = null;
                findPic = null;
                pictures.clear();
                findPic = new FindPic(getApplicationContext());
                findPic.execute();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                Intent intent = new Intent(FindActivity.this, FindPicActivity.class);
                                                intent.putExtra("PIC_ID",((TextView)view.findViewById(R.id.tag_name)).getText().toString().substring(1)); //누른 태그를 '#' 빼고 보낸다
                                                startActivity(intent);
                                                flag = false; // FindActivity.flag 를 false 로
                                                finish();
                                            }
                                        });
        pictures = new ArrayList<Tag>();
        adapter = new ListAdapter(this,pictures,R.layout.item);
        listView.setAdapter(adapter);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 키보드 닫기
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 키보드 닫기
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                finish();
            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();
        // 중요) FindActivity.flag 가 검색화면에서 그냥 나갔을땐 true(아무변화없음:모든사진불러오기) 검색된 항목을 눌러서 나갔을땐 false(모든사진불러오기 안함 : 해당 태그 사진만 띄움)
        if(flag == false)
            MapFragment.flag = false;
        else MapFragment.flag = true;
    }

    public class FindPic extends AsyncTask<String, Integer, Void> {

        public static final int DOWNLOAD_PROGRESS = 0;

        private final String url_Address = "http://footprints.gonetis.com:8080/moo/tagsearch";

        private ProgressDialog dialog;
        Context mContext;

        public FindPic(Context context){
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

                builder.addTextBody("keyword", URLEncoder.encode(text,"UTF-8"));

                HttpEntity entity = builder.build();
                httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost);

                //서버에서 받은 response 값 저장
                String body = EntityUtils.toString(response.getEntity());
                JSONArray jsonArray = new JSONArray(body);
                StringBuffer sb = new StringBuffer();

                tagArray = new String[jsonArray.length()];
                tagcountArray = new int[jsonArray.length()];

                //데이터 뽑는 부분
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String tag = jsonObject.getString("tag");
                    int tagcount = jsonObject.getInt("int_tagCount");

                    tagArray[i] = tag;
                    tagcountArray[i] = tagcount;

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
            if(tagArray!=null && tagArray.length>0)
                for(int i=0;i<tagArray.length;i++) {
                    Log.e("TAG", tagArray[i] + tagcountArray[i]);
                    pictures.add(new Tag(tagArray[i], tagcountArray[i])); // 키워드 검색완료시 태그+갯수 를 넣어준다
                }
        }
    }

}