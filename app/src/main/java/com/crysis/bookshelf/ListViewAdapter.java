package com.crysis.bookshelf;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<Bookitem> {
    private int resourceId;
    public ListViewAdapter(Context context, int textViewResourceId, ArrayList<Bookitem> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Bookitem bookitem = getItem(position);//获取当前项的Bookitem实例
        View view;
        // ViewHolder存储自定义布局,进一步对listview效率进行优化
        ViewHolder viewHolder = null;

        // 提高listview效率,如果convertView为null,则使用LayoutInflater去加载布局，如果不为null则直接对convertView重用
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);//false参数表示只让我们在父布局中声明的layout属性生效
            viewHolder = new ViewHolder();
            viewHolder.imageView = view.findViewById(R.id.list_cover_image_view);
            viewHolder.textView_title = view.findViewById(R.id.list_title_text_view);
            viewHolder.textView_publisher = view.findViewById(R.id.list_publisher_text_view);
            viewHolder.textView_pubdate = view.findViewById(R.id.list_pubtime_text_view);
            view.setTag(viewHolder);
        }
        else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();//重新获取ViewHolder
        }

        // Byte[]转Bitmap再进行图片资源设置
        Bitmap2Byte bitmap2Byte = new Bitmap2Byte();
        Bitmap bitmap = null;
        if(bookitem.getBitmap_byte() != null) {
            bitmap = bitmap2Byte.byte2Bitmap(bookitem.getBitmap_byte());
            viewHolder.imageView.setImageBitmap(bitmap);
        }
        else
            viewHolder.imageView.setImageResource(bookitem.getImageId());

        viewHolder.textView_title.setText(bookitem.getTitle());
        viewHolder.textView_publisher.setText(bookitem.getAuthor_Publisher());
        viewHolder.textView_pubdate.setText(bookitem.getPubDate());

        return view;
    }

    class ViewHolder{
        ImageView imageView;
        TextView textView_title;
        TextView textView_publisher;
        TextView textView_pubdate;
    }
}
