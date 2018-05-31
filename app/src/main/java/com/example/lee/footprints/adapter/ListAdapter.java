package com.example.lee.footprints.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lee.footprints.R;
import com.example.lee.footprints.model.Tag;

import org.w3c.dom.Text;

import java.util.ArrayList;

// 검색시 리스트뷰 어댑터
public class ListAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private ArrayList<Tag> data; // Tag(태그, 사진개수)에 대한 리스트
    private int layout;
    public ListAdapter(Context context, ArrayList<Tag> data, int layout){
        this.inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data=data;
        this.layout=layout;
    }
    @Override
    public int getCount(){return data.size();}
    @Override
    public String getItem(int position){return data.get(position).getId();}
    @Override
    public long getItemId(int position){return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        // 리스트 하나하나에 표시할 뷰
        if(convertView==null){
            convertView=inflater.inflate(R.layout.item, null);
        }
        Tag item=data.get(position);
        TextView name=(TextView)convertView.findViewById(R.id.tag_name);
        name.setText(item.getId());
        TextView count = (TextView)convertView.findViewById(R.id.tag_count);
        count.setText(Integer.toString(item.getNum())+"개");
        return convertView;
    }

}
