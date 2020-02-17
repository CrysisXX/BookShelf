package com.crysis.bookshelf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Editbook extends AppCompatActivity {
    //书架列表BookShelves
    public ArrayList<String> Bookshelves;
    //标签列表Labels
    public ArrayList<String> Labels;
    //选中的标签
    public int label_selected;
    public String[] labels;
    public AlertDialog.Builder alertBuilder;
    // ListOperator实例
    ListOperator listOperator = new ListOperator();


    public AlertDialog singleDialogForSelectLabel;

    Bookitem externbookitem;
    Toolbar toolbar;
    LinearLayout linearLayout;

    EditText editText_title;
    EditText editText_author;
    EditText editText_translator;
    EditText editText_publisher;
    EditText editText_Year;
    EditText editText_Month;
    EditText editText_ISBN;
    EditText editText_Website;
    EditText editText_Note;
    EditText editText_Label;
    ImageView imageView;
    Spinner spinner_reading_status;
    Spinner spinner_bookshelf;

    Button button_back;
    Button button_save;

    //保存图片
    Bitmap externbmp = null;
    //修改的bookitem的位置
    private int Edit_Postion = -10;
    //修改书籍信息时传来的byte[]
    byte[] Bitmap_byte;
    //是否是手动添加
    private int MANUAL_OR_NOT = 0;
    //是否从Detail跳转而来
    String Detail_or_not = "no";


    //在消息队列中实现对控件的更改
    @SuppressLint("HandlerLeak")
    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    System.out.println("111");
                    externbmp = (Bitmap)msg.obj;
                    imageView.setImageBitmap(externbmp);
                    Toast.makeText(Editbook.this, "图片加载成功" ,Toast.LENGTH_SHORT).show();
                    break;
            }
        };
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏书籍编辑界面的标题栏，采用新的标题栏
        getSupportActionBar().hide();
        setContentView(R.layout.activity_book_edit);

        //初始化书架数据
        initBookShelf();

        //初始化标签数据
        initLabel();

        //绑定组件
        editText_title = findViewById(R.id.book_title_edit_text);
        editText_author = findViewById(R.id.book_author_edit_text);
        editText_translator = findViewById(R.id.book_translator_edit_text);
        editText_publisher = findViewById(R.id.book_publisher_edit_text);
        editText_Year = findViewById(R.id.book_pubyear_edit_text);
        editText_Month = findViewById(R.id.book_pubmonth_edit_text);
        editText_ISBN = findViewById(R.id.book_isbn_edit_text);
        editText_Website = findViewById(R.id.book_website_edit_text);
        editText_Note = findViewById(R.id.book_notes_edit_text);
        editText_Label = findViewById(R.id.book_labels_edit_text);
        imageView = (ImageView)findViewById(R.id.book_cover_image_view);
        spinner_reading_status = findViewById(R.id.reading_status_spinner);
        spinner_bookshelf = findViewById(R.id.book_shelf_spinner);

        //绑定toolbar
        linearLayout = findViewById(R.id.book_edit_layout);
        toolbar = findViewById(R.id.bookedit_toolbar);

        //绑定按钮button
        button_back = findViewById(R.id.bookedit_toolbar_btn_back);
        button_save = findViewById(R.id.bookedit_toolbar_btn_save);

        //
        editText_Label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleDialogForSelectLabel();
            }
        });

        //创建ArrayAdapter对象
        final ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_list_item_edit, Bookshelves);
        spinner_bookshelf.setAdapter(spinner_adapter);
        spinner_bookshelf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //选中的书架为“添加书架”时
                if (position == (Bookshelves.size() - 1)) {
                    showInputDialogForBookshelf("请输入书架名称");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //// 获取由MainActivity/BookDetail传来的数据
        //获取意图对象
        final Intent intent = getIntent();
        //获取传递的值
        Bookitem bookitem_from_detail = new Bookitem();
        if(intent.getStringExtra("Manual") != null) {
            Toast.makeText(Editbook.this, intent.getStringExtra("Manual"), Toast.LENGTH_SHORT).show();
            if (intent.getStringExtra("Manual").toString().equals("111"))
                Detail_or_not = "Main";
            if (intent.getStringExtra("Manual").toString().equals("222"))
                Detail_or_not = "Detail";
        }
        //// 获取由BookDetail传来的数据
        if(intent.getSerializableExtra("bookitem_from_detail") != null && Detail_or_not.equals("Detail")) {
            externbookitem = bookitem_from_detail = (Bookitem) intent.getSerializableExtra("bookitem_from_detail");
            Edit_Postion = intent.getIntExtra("edit_position", -1);
            String position = String.valueOf(Edit_Postion);
            Toast.makeText(Editbook.this, position, Toast.LENGTH_LONG).show();
            Bitmap_byte = bookitem_from_detail.getBitmap_byte();
            Toast.makeText(Editbook.this, bookitem_from_detail.getTitle(), Toast.LENGTH_LONG).show();
            editText_title.setText(bookitem_from_detail.getTitle());
            editText_author.setText(bookitem_from_detail.getAuthor());
            editText_translator.setText(bookitem_from_detail.getTranslator());
            editText_publisher.setText(bookitem_from_detail.getPublisher());
            editText_Year.setText(bookitem_from_detail.getPubYear());
            editText_Month.setText(bookitem_from_detail.getPubMonth());
            editText_ISBN.setText(bookitem_from_detail.getISBN());
            editText_Website.setText(bookitem_from_detail.getWebsite());
            editText_Note.setText(bookitem_from_detail.getNotes());
            editText_Label.setText(bookitem_from_detail.getLabels());
            if(bookitem_from_detail.getBitmap_byte() != null) {
                byte[] image_res = bookitem_from_detail.getBitmap_byte();
                Bitmap2Byte bitmap2Byte = new Bitmap2Byte();
                Bitmap bitmap = bitmap2Byte.byte2Bitmap(image_res);
                imageView.setImageBitmap(bitmap);
            }
            else{
                imageView.setImageResource(bookitem_from_detail.getImageId());
            }

            //设置书架
            String Bookshelf = bookitem_from_detail.getBookShelfName();
            for(int i = 0; i < Bookshelves.size() - 1; i++){
                if (Bookshelves.get(i).equals(Bookshelf))
                {
                    spinner_bookshelf.setSelection(i);
                    break;
                }
            }



        }
        //// 获取由BookDetail传来的数据
        //// 获取由MainActivity传来的数据
        else if(intent.getStringExtra("Manual") != null && Detail_or_not.equals("Main")) {
            MANUAL_OR_NOT = 1;
            editText_title.setText("");
            editText_author.setText("");
            editText_translator.setText("");
            editText_publisher.setText("");
            editText_Year.setText("");
            editText_Month.setText("");
            editText_ISBN.setText("");
            editText_Website.setText("");
            imageView.setImageResource(R.drawable.book_cover_default);
        }
        else {
            String Title = intent.getStringExtra("Title");
            String Author = intent.getStringExtra("Author");
            String Translator = intent.getStringExtra("Translator");
            String Publisher = intent.getStringExtra("Publisher");
            String PubYear = intent.getStringExtra("PubYear");
            String PubMonth = intent.getStringExtra("PubMonth");
            String ISBN = intent.getStringExtra("ISBN");
            String Website = intent.getStringExtra("Website");
            String Bookshelf = intent.getStringExtra("bookShelfName");
            final String ImageURL = intent.getStringExtra("ImageURL");
            //图片处理
            //新建线程加载图片信息，发送到消息队列中
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Bitmap bmp = null;
                    if(ImageURL != null) {
                        bmp = getURLimage(ImageURL);
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = bmp;
                        System.out.println("000");
                        handle.sendMessage(msg);
                    }
                    else {
                    }
                }
            }).start();

            //设置值
            editText_title.setText(Title);
            editText_author.setText(Author);
            editText_translator.setText(Translator);
            editText_publisher.setText(Publisher);
            editText_Year.setText(PubYear);
            editText_Month.setText(PubMonth);
            editText_ISBN.setText(ISBN);
            editText_Website.setText(Website);
            for(int i = 0; i < Bookshelves.size() - 1; i++){
                if (Bookshelves.get(i) == Bookshelf)
                {
                    spinner_bookshelf.setSelection(i);
                    break;
                }
            }

        }
        //// 获取由MainActivity传来的数据



        // 设置返回按钮点击事件——直接返回主界面，不添加书籍
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNormalDialog();
            }
        });

        // 设置保存按钮点击事件
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Editbook.this, "点击了保存按钮" ,Toast.LENGTH_SHORT).show();
                //返回修改的
                if(Detail_or_not.equals("Detail")) {
                    Intent intent_detail = new Intent(Editbook.this, BookDetail.class);
                    Bookitem bookitem_change = new Bookitem();
                    bookitem_change = externbookitem;
                    bookitem_change.setTitle(editText_title.getText().toString());
                    bookitem_change.setAuthor(editText_author.getText().toString());
                    bookitem_change.setTranslator(editText_translator.getText().toString());
                    bookitem_change.setPublisher(editText_publisher.getText().toString());
                    bookitem_change.setPubYear(editText_Year.getText().toString());
                    bookitem_change.setPubMonth(editText_Month.getText().toString());
                    String Pubdate = editText_Year.getText().toString() + "-" + editText_Month.getText().toString();
                    bookitem_change.setPubDate(Pubdate);
                    bookitem_change.setISBN(editText_ISBN.getText().toString());
                    bookitem_change.setWebsite(editText_Website.getText().toString());
                    bookitem_change.setNotes(editText_Note.getText().toString());
                    bookitem_change.setLabels(editText_Label.getText().toString());
                    bookitem_change.setReadingStatus_Text(spinner_reading_status.getSelectedItem().toString());
                    bookitem_change.setBookShelfName(spinner_bookshelf.getSelectedItem().toString());
                    intent_detail.putExtra("change_bookitem", bookitem_change);
                    intent_detail.putExtra("change_position", Edit_Postion);
                    setResult(MainActivity.RESULT_ADD_CHANGE, intent_detail);
                    finish();
                }
                else if(Detail_or_not == "Main") {
                    //手动添加书籍
                    Intent intent_manual = new Intent(Editbook.this, MainActivity.class);
                    Bookitem bookitem_manual = new Bookitem();
                    bookitem_manual.setTitle(editText_title.getText().toString());
                    bookitem_manual.setAuthor(editText_author.getText().toString());
                    bookitem_manual.setTranslator(editText_translator.getText().toString());
                    bookitem_manual.setPublisher(editText_publisher.getText().toString());
                    bookitem_manual.setPubYear(editText_Year.getText().toString());
                    bookitem_manual.setPubMonth(editText_Month.getText().toString());
                    String Pubdate = editText_Year.getText().toString() + "-" + editText_Month.getText().toString();
                    bookitem_manual.setPubDate(Pubdate);
                    bookitem_manual.setISBN(editText_ISBN.getText().toString());
                    bookitem_manual.setWebsite(editText_Website.getText().toString());
                    bookitem_manual.setNotes(editText_Note.getText().toString());
                    bookitem_manual.setLabels(editText_Label.getText().toString());
                    bookitem_manual.setReadingStatus_Text(spinner_reading_status.getSelectedItem().toString());
                    bookitem_manual.setBookShelfName(spinner_bookshelf.getSelectedItem().toString());
                    intent_manual.putExtra("manual_bookitem", bookitem_manual);
                    setResult(MainActivity.RESULT_MANUAL_ADD_OK, intent_manual);
                    finish();
                }
                else{
                    //扫码添加的
                    Intent intent_save = new Intent(Editbook.this, MainActivity.class);
                    intent_save.putExtra("Title", editText_title.getText().toString());
                    intent_save.putExtra("Author", editText_author.getText().toString());
                    intent_save.putExtra("Translator", editText_translator.getText().toString());
                    intent_save.putExtra("Publisher", editText_publisher.getText().toString());
                    intent_save.putExtra("PubYear", editText_Year.getText().toString());
                    intent_save.putExtra("PubMonth", editText_Month.getText().toString());
                    intent_save.putExtra("ISBN", editText_ISBN.getText().toString());
                    intent_save.putExtra("Website", editText_Website.getText().toString());
                    intent_save.putExtra("Note", editText_Note.getText().toString());
                    intent_save.putExtra("Labels", editText_Label.getText().toString());
                    intent_save.putExtra("ReadingStatus_Text", spinner_reading_status.getSelectedItem().toString());
                    String ReadingStatus = "-1";
                    if(spinner_reading_status.getSelectedItem().toString().equals("未读"))
                        ReadingStatus = "0";
                    if(spinner_reading_status.getSelectedItem().toString().equals("在读"))
                        ReadingStatus = "1";
                    if(spinner_reading_status.getSelectedItem().toString().equals("已读"))
                        ReadingStatus = "2";
                    intent_save.putExtra("ReadingStatus", ReadingStatus.toString());
                    intent_save.putExtra("Bookshelf", spinner_bookshelf.getSelectedItem().toString());
                    // 压缩图片
                    ImageUtils imageUtils = new ImageUtils();
                    Bitmap bitmap_send = imageUtils.comp(externbmp);
                    // 压缩图片
                    // 传输图片
                    Bundle bundle_image = new Bundle();
                    bundle_image.putParcelable("bmp", bitmap_send);
                    intent_save.putExtra("bmp", bundle_image);
                    // 传输图片
                    setResult(MainActivity.RESULT_ADD_OK, intent_save);
                    finish();
                }
            }
        });
    }

    //加载图片
    public Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    //初始化书架数据
    private void initBookShelf(){
        Bookshelves = listOperator.loadBookshelf(Editbook.this);
        //如果没有书架数据，则创建默认的书架及书架数据文件
        if (Bookshelves == null){
            Bookshelves = new ArrayList<>();
            Bookshelves.add("所有");
            Bookshelves.add("默认书架");
            Bookshelves.add("添加书架");
            listOperator.save(Editbook.this, Bookshelves, "Bookshelves.dat");
            //Toast.makeText(MainActivity.this, "加载书架数据失败", Toast.LENGTH_LONG).show();
        }
    }

    //初始化标签数据
    private void initLabel(){
        Labels = listOperator.loadLabel(Editbook.this);
        //如果没有标签数据，则创建默认的标签及标签数据文件
        if (Labels == null){
            Labels = new ArrayList<>();
            Labels.add("测试标签");
            Labels.add("默认标签");
            Labels.add("添加新标签");
            listOperator.save(Editbook.this, Labels, "Labels.dat");
            //Toast.makeText(MainActivity.this, "加载书架数据失败", Toast.LENGTH_LONG).show();
        }
    }

    // 手动输入对话框类For Bookshelf
    private void showInputDialogForBookshelf(String title){
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(Editbook.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(Editbook.this);
        inputDialog.setTitle(title.toString()).setView(editText);
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //将spinner_bookshelf的当前选中项设为点击“添加书架”之前选中的书架
//                spinner_bookshelf.setSelection(position);
            }
        });
        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input = editText.getText().toString().trim();

                //当前用户未输入书架名时，提示“用户书架名称不能为空，书架添加失败”
                //并将spinner_bookshelf的当前选中项设为点击“添加书架”之前选中的书架
                if (input.isEmpty())
                {
                    Toast.makeText(Editbook.this, "书架名称不能为空，书架添加失败", Toast.LENGTH_SHORT).show();
                }else{
                    String bookshelf_add = editText.getText().toString();
                    //当用户输入书架名称后，添加书架
                    //在spinner_bookshelf中添加新的书架
                    ArrayList<String> new_bookshelf = insertBookshelf(Bookshelves, bookshelf_add);
                    Bookshelves = new_bookshelf;
                    ArrayAdapter<String> spinner_new_adapter = new ArrayAdapter<String>(Editbook.this,R.layout.simple_spinner_list_item_edit, Bookshelves);
                    spinner_bookshelf.setAdapter(spinner_new_adapter);

                    //数据持久化，包括书架数据Bookshelves.dat和新建书架内的书籍数据的持久化
                    String file = "Bookshelves.dat" ;
                    listOperator.save(Editbook.this, Bookshelves, file);
                }
            }
        }).show();
    }
    //添加新书架
    private static ArrayList<String> insertBookshelf(ArrayList<String> old, String new_bookshelf){
        ArrayList<String> tem = old;
        tem.set(old.size() - 1, new_bookshelf);
        tem.add("添加书架");
        return tem;
    }

    private void showSingleDialogForSelectLabel(){
        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("选择标签");
        labels = new String[Labels.size()];
        for (int i = 0; i < Labels.size(); i++){
            labels[i] = Labels.get(i);
        }
        alertBuilder.setSingleChoiceItems(labels, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                if (index == Labels.size() - 1){
                    showInputDialogForAddingLabel();
                }

                label_selected = index;
            }
        });
        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                editText_Label.setText(labels[label_selected]);
                Toast.makeText(Editbook.this, labels[label_selected], Toast.LENGTH_SHORT).show();

            }
        });

        singleDialogForSelectLabel = alertBuilder.create();
        singleDialogForSelectLabel.show();

    }

    // 返回提示对话框
    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(Editbook.this);
        normalDialog.setTitle("提示");
        normalDialog.setMessage("确定要放弃修改或添加书籍?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Intent intent_back = new Intent(Editbook.this, MainActivity.class);
                        setResult(RESULT_CANCELED, intent_back);
                        finish();
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do Nothing
                    }
                });
        // 显示
        normalDialog.show();
    }

    // 手动输入对话框类ForAddingLabel
    private void showInputDialogForAddingLabel(){
        String title = "添加新标签";
        final EditText editText = new EditText(Editbook.this);
        editText.setHint("输入新的标签的名称");
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(Editbook.this);
        inputDialog.setTitle(title).setView(editText);
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = editText.getText().toString().trim();

                        //当前用户未输入新标签名时，提示“标签名称不能为空，添加失败”
                        if (input.isEmpty())
                        {
                            Toast.makeText(Editbook.this, "标签名称不能为空，添加失败", Toast.LENGTH_SHORT).show();
                        }else{
                            String label_add = editText.getText().toString();
                            ArrayList<String> new_label = insertLabel(Labels, label_add);
                            Labels = new_label;

                            //数据持久化，包括书架数据Bookshelves.dat和新建书架内的书籍数据的持久化
                            String file = "Labels.dat" ;
                            listOperator.save(Editbook.this, Labels, file);
                            labels = new String[Labels.size()];
                            for (int i = 0; i < Labels.size(); i++){
                                labels[i] = Labels.get(i);
                            }
                            alertBuilder.setSingleChoiceItems(labels, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int index) {
                                    if (index == Labels.size() - 1){
                                        showInputDialogForAddingLabel();
                                    }

                                    label_selected = index;
                                }
                            });
                            singleDialogForSelectLabel.dismiss();
                            singleDialogForSelectLabel = alertBuilder.create();
                            singleDialogForSelectLabel.show();
                        }
                    }
                }).show();
    }
    //添加新标签
    private static ArrayList<String> insertLabel(ArrayList<String> old, String new_Label){
        ArrayList<String> tem = old;
        tem.set(old.size() - 1, new_Label);
        tem.add("添加新标签");
        return tem;
    }

}
