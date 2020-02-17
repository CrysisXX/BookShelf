package com.crysis.bookshelf;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JsonParser {

    public static Bookitem jsonToBook(String jsonSrc){
        Bookitem bookitem = null;
        int length_of_str = 0;
        String temp_str1 = "";
        String temp_str2 = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonSrc);
            String remark = jsonObject.getJSONObject("showapi_res_body").getString("remark");
            if(remark.equals("success")){
                JSONObject data = jsonObject.getJSONObject("showapi_res_body").getJSONObject("data");
                bookitem = new Bookitem();
                bookitem.setImageId(R.drawable.ic_bookshelf_primary);
                // 标题Title
                bookitem.setTitle(data.getString("title"));
                // 从原来author获取的内容中分离出作者和译者
                temp_str1 = data.getString("author");
                temp_str2 = data.getString("author");
                length_of_str = data.getString("author").length();
                if(temp_str1.contains("(著)")) {
                    String author = temp_str1.substring(0, temp_str1.indexOf("著") + 2);
                    String translator = temp_str2.substring(temp_str2.indexOf("著") + 2, length_of_str);
                    // 从原来author获取的内容中分离出作者和译者
                    bookitem.setAuthor(author);
                    bookitem.setTranslator(translator);
                }
                else{
                    bookitem.setAuthor(data.getString("author"));
                    bookitem.setTranslator(null);
                }
                // 出版时间Pubdate
                bookitem.setPubDate(data.getString("pubdate"));
                String pubdate = data.getString("pubdate");
                // 从原来pubdate中分离出pubyear和pubmonth
                if(pubdate.contains("-")){
                    bookitem.setPubYear(pubdate.substring(0, pubdate.indexOf("-")));
                    bookitem.setPubMonth(pubdate.substring(pubdate.indexOf("-")+1, pubdate.length()));
                }
                // 从原来pubdate中分离出pubyear和pubmonth
                // ISBN码以及出版社Publisher
                bookitem.setISBN(data.getString("isbn"));
                bookitem.setPublisher(data.getString("publisher"));
                // 来源网址Website
                bookitem.setWebsite("https://market.cloud.tencent.com/products/7494");
                // 书籍封皮图片URL
                bookitem.setImageURL(data.getString("img"));

                // Notes、Labels以及BookShelfName还未设置
//                Bitmap bitmap = ImageManager.GetImageInputStream(data.getString("img"));
//                ImageManager.SaveImage(context, bitmap, book.getUuid());
            }
        } catch (JSONException e) {
            Log.e("JsonParser", "jsonToBook: Json解析错误");
            e.printStackTrace();
        }

        return bookitem;
    }
}
